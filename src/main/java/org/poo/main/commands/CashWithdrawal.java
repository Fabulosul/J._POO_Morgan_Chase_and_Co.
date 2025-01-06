package org.poo.main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.*;

@Getter
@Setter
public class CashWithdrawal extends Command implements CommandInterface {
    private final String withdrawalCurrency = "RON";
    private Bank bank;
    private ArrayNode output;

    public CashWithdrawal(final Bank bank, final CommandInput command, ArrayNode output) {
        super(command);
        this.bank = bank;
        this.output = output;
    }


    @Override
    public void execute() {
        User user = bank.getUserByMail(getEmail());
        if (user == null) {
            addErrorToOutput("User not found");
            return;
        }
        Card card = bank.getCardByCardNr(getCardNumber());
        if (card == null) {
            addErrorToOutput("Card not found");
            return;
        }
        BankAccount bankAccount = bank.getAccountByCardNr(getCardNumber());
        if (bankAccount == null) {
            registerTransactionError(user, "Account not found");
            return;
        }

        double amountWithCommission = bankAccount.calculateAmountWithCommission(bank, getAmount());
        if(bankAccount.hasSufficientFunds(amountWithCommission, withdrawalCurrency, bank)) {
            bankAccount.payOnline(bank, getAmount(), withdrawalCurrency);
            Transaction transaction = new Transaction
                    .TransactionBuilder(getTimestamp(),
                    "Cash withdrawal of " + getAmount())
                    .amount(getAmount())
                    .build();
            user.addTransaction(transaction);
        } else {
            registerTransactionError(user, "Insufficient funds");
        }
    }

    public void registerTransactionError(User user, String description) {
        Transaction transaction = new Transaction
                .TransactionBuilder(getTimestamp(),
                description)
                .build();
        user.addTransaction(transaction);
    }

    public void addErrorToOutput(String description) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("command", "cashWithdrawal");

        ObjectNode outputNode = mapper.createObjectNode();
        outputNode.put("description", description);
        outputNode.put("timestamp", getTimestamp());

        objectNode.set("output", outputNode);
        objectNode.put("timestamp", getTimestamp());
        output.add(objectNode);
    }
}