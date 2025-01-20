package org.poo.main.commands;

import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bankaccounts.BankAccount;
import org.poo.main.bankaccounts.BusinessAccount;
import org.poo.main.bankaccounts.SavingsBankAccount;
import org.poo.main.transaction.Transaction;
import org.poo.main.user.User;
import org.poo.main.cashback.NrOfTransactionsObserver;
import org.poo.main.cashback.SpendingThresholdObserver;

@Getter
@Setter
public final class AddAccount extends Command implements CommandInterface {
    private Bank bank;

    public AddAccount(final Bank bank, final CommandInput command) {
        super(command);
        this.bank = bank;
    }

    /**
     * Method overridden from the CommandInterface to add a new account to a given user.
     * It gets the user by email, creates a new account with the given type and adds it
     * to the user.
     * It also creates two observers for the cashback system, one for the number of transactions
     * and one for the spending threshold and adds them to the bank account.
     * At the end, it registers this transaction to the user and the bank account.
     */
    @Override
    public void execute() {
        User user = bank.getUserByMail(getEmail());

        BankAccount bankAccount = switch (getAccountType()) {
            case "savings" -> new SavingsBankAccount(bank, getCurrency(), getInterestRate());
            case "business" -> new BusinessAccount(bank, getCurrency(), user);
            case "classic" -> new BankAccount(bank, getCurrency());
            default -> null;
        };
        if (bankAccount == null) {
            return;
        }
        SpendingThresholdObserver spendingThresholdObserver =
                new SpendingThresholdObserver(bank, bankAccount);
        bankAccount.addCashbackObserver(spendingThresholdObserver);
        NrOfTransactionsObserver nrOfTransactionsObserver =
                new NrOfTransactionsObserver(bank, bankAccount);
        bankAccount.addCashbackObserver(nrOfTransactionsObserver);
        user.addBankAccount(bankAccount);

        registerTransaction(user, bankAccount);
    }

    /**
     * Method that creates a "New account created" transaction and adds it to the user and
     * to the bank account given as parameters.
     *
     * @param user -> the user to which the transaction is added
     * @param bankAccount -> the bank account to which the transaction is added
     */
    private void registerTransaction(final User user, final BankAccount bankAccount) {
        Transaction transaction = new Transaction
                .TransactionBuilder(getTimestamp(), "New account created").build();
        user.addTransaction(transaction);
        bankAccount.addTransaction(transaction);
    }
}
