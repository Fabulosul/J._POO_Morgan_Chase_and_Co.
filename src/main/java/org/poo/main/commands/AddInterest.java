package org.poo.main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bankaccounts.BankAccount;
import org.poo.main.bankaccounts.SavingsBankAccount;
import org.poo.main.transaction.Transaction;
import org.poo.main.user.User;

@Getter
@Setter
public final class AddInterest extends Command implements CommandInterface {
    private Bank bank;
    private ArrayNode output;

    public AddInterest(final Bank bank, final CommandInput command,
                       final ArrayNode output) {
        super(command);
        this.bank = bank;
        this.output = output;
    }

    /**
     * Method overridden from the CommandInterface to add interest to a savings account.
     * It gets the user and the account by using the given iban and then checks
     * if the account is a savings account. If it is, it adds the interest to the account.
     * If it is not, it adds an error to the output.
     * However, if the user or the account is not found, it does nothing, it simply terminates.
     */
    @Override
    public void execute() {
        User user = bank.getUserByAccount(getAccount());
        if (user == null) {
            return;
        }
        BankAccount bankAccount = user.getAccountByIban(getAccount());
        if (bankAccount == null) {
            return;
        }
        if (bankAccount.getAccountType().equals("savings")) {
            double interestIncome = ((SavingsBankAccount) bankAccount).addInterest();
            String currency = bankAccount.getCurrency();
            registerSuccessfulTransaction(bankAccount, user, interestIncome, currency);
        } else {
            addErrorToOutput();
        }
    }

    /**
     * Method that adds an error message to the output ArrayNode to
     * signal that the account is not a savings account and therefore
     * the "addInterest" command cannot be executed.
     */
    private void addErrorToOutput() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode errorNode = mapper.createObjectNode();

        errorNode.put("command", "addInterest");

        ObjectNode outputNode = mapper.createObjectNode();
        outputNode.put("description", "This is not a savings account");
        outputNode.put("timestamp", getTimestamp());

        errorNode.set("output", outputNode);
        errorNode.put("timestamp", getTimestamp());
        output.add(errorNode);
    }

    /**
     * Helper method that creates a new transaction stating that the interest rate has been
     * added to the account.
     * If the bankAccount is not null, it also adds the transaction
     * to the bankAccount's transaction list.
     *
     * @param bankAccount -> the bank account to which the transaction is added
     * @param user -> the user to which the transaction is added
     * @param amount -> the amount of the transaction (interest rate)
     * @param currency -> the currency of the transaction
     */
    private void registerSuccessfulTransaction(final BankAccount bankAccount, final User user,
                                               final double amount, final String currency) {
        Transaction transaction = new Transaction
                .TransactionBuilder(getTimestamp(), "Interest rate income")
                .amount(amount)
                .currency(currency)
                .separateAmountAndCurrency(true)
                .build();
        user.addTransaction(transaction);
        if (bankAccount != null) {
            bankAccount.addTransaction(transaction);
        }
    }
}
