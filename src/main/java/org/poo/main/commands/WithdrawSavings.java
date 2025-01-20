package org.poo.main.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bankaccounts.BankAccount;
import org.poo.main.transaction.Transaction;
import org.poo.main.user.User;

@Getter
@Setter
public final class WithdrawSavings extends Command implements CommandInterface {
    private Bank bank;
    private ArrayNode output;
    private static final int MINIMUM_AGE = 21;


    public WithdrawSavings(final Bank bank, final CommandInput command, final ArrayNode output) {
        super(command);
        this.bank = bank;
        this.output = output;
    }

    /**
     * Method used to withdraw money from a savings account and deposit it into a classic account.
     * If all the conditions are met, the money is transferred from the savings account to
     * the classic account and a transaction is added to the user's transaction list.
     * To find the classic account, the method findClassicAccountByCurrency is called
     * from the User class.
     * If the user does not have a classic account, does not have the minimum age required,
     * the account is not of type savings, the account is not found or the user does not
     * have enough money, an error transaction is added to the user's transaction list.
     *
     * @implNote The method uses the sendMoneyWithoutCommission method from the BankAccount
     * class to transfer the money from the savings account to the classic account because
     * no commission is applied.
     */
    @Override
    public void execute() {
        User user = bank.getUserByAccount(getAccount());
        if (user == null) {
            return;
        }
        if (user.getAge() < MINIMUM_AGE) {
            registerTransactionError(null, user, "You don't have the minimum age required.");
            return;
        }
        BankAccount bankAccount = user.getAccountByIban(getAccount());
        if (bankAccount == null) {
            registerTransactionError(null, user, "Account not found");
            return;
        }
        if (!bankAccount.getAccountType().equals("savings")) {
            registerTransactionError(bankAccount, user, "Account is not of type savings.");
            return;
        }
        BankAccount classicAccount = user.findClassicAccountByCurrency(getCurrency());
        if (classicAccount == null) {
            registerTransactionError(bankAccount, user, "You do not have a classic account.");
            return;
        }
        if (!bankAccount.hasSufficientFunds(getAmount(), getCurrency(), bank)) {
            registerTransactionError(bankAccount, user, "Insufficient funds");
            return;
        }

        bankAccount.sendMoneyWithoutCommission(bank, classicAccount, getAmount());
        Transaction transaction = new Transaction
                .TransactionBuilder(getTimestamp(), "Savings withdrawal")
                .amount(getAmount())
                .classicAccountIban(classicAccount.getIban())
                .savingsAccountIban(bankAccount.getIban())
                .build();
        user.addTransaction(transaction);
        user.addTransaction(transaction);
    }

    /**
     * Method used to register an error transaction based on a given message.
     */
    public void registerTransactionError(final BankAccount bankAccount, final User user,
                                         final String message) {
        Transaction transaction = new Transaction
                .TransactionBuilder(getTimestamp(), message)
                .build();
        user.addTransaction(transaction);
        if (bankAccount != null) {
            bankAccount.addTransaction(transaction);
        }
    }
}
