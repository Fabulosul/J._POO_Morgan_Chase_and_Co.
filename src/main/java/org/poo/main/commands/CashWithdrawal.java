package org.poo.main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bankaccounts.BankAccount;
import org.poo.main.card.Card;
import org.poo.main.user.User;
import org.poo.main.transaction.Transaction;

@Getter
@Setter
public final class CashWithdrawal extends Command implements CommandInterface {
    private Bank bank;
    private ArrayNode output;
    private static final String WITHDRAWAL_CURRENCY = "RON";

    public CashWithdrawal(final Bank bank, final CommandInput command, final ArrayNode output) {
        super(command);
        this.bank = bank;
        this.output = output;
    }

    /**
     * Method used to do a cash withdrawal from a bank account.
     * It calculates the amount with the commission and checks if the account has sufficient funds.
     * If the account has sufficient funds, it withdraws the amount and adds
     * a transaction to the user.
     * Also, if the card is a one-time, it regenerates it.
     *
     */
    @Override
    public void execute() {
        if (getEmail() == null) {
            return;
        }
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
        if (bankAccount.hasSufficientFunds(amountWithCommission, WITHDRAWAL_CURRENCY, bank)) {
            bankAccount.payWithCommission(bank, getAmount(), WITHDRAWAL_CURRENCY);
            if (card.isOneTimeCard() && bankAccount.getBalance() == 0) {
                bankAccount.removeCard(card);
                Card newCard = new Card("oneTime");
                bankAccount.addCard(newCard);
            }
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

    /**
     * Helper method used to register a transaction error based on a description.
     *
     * @param user -> the user to which the transaction is added
     * @param description -> the description of the transaction
     */
    public void registerTransactionError(final User user, final String description) {
        Transaction transaction = new Transaction
                .TransactionBuilder(getTimestamp(),
                description)
                .build();
        user.addTransaction(transaction);
    }

    /**
     * Helper method used to add an error to the output.
     * It creates an object node with the command, the description and the timestamp
     * and adds it to the output array.
     *
     * @param description -> the description of the error
     */
    public void addErrorToOutput(final String description) {
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
