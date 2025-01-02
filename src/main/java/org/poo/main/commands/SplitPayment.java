package org.poo.main.commands;

import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bank.BankAccount;
import org.poo.main.bank.Transaction;
import org.poo.main.bank.User;

@Getter
@Setter
public final class SplitPayment extends Command implements CommandInterface {
    private Bank bank;

    public SplitPayment(final Bank bank, final CommandInput command) {
        super(command);
        this.bank = bank;
    }

    /**
     * Method overridden from CommandInterface that does a split payment between
     * multiple accounts.
     * It calculates the amount to be paid to each account and checks if every account
     * have sufficient funds for the payment.
     * Then, it checks if the accounts are valid and if they have sufficient funds
     * by calling the findInvalidAccount method.
     * If an account does not have sufficient funds, it adds an error message to that
     * particular account stating that it has insufficient funds for the payment.
     * If all accounts have sufficient funds, it calls the addTransactions method to
     * make the payment and add the transaction to the accounts.
     */
    @Override
    public void execute() {
        double splitAmount = getAmount() / getAccounts().size();
        BankAccount invalidAccount = findInvalidAccount(splitAmount);
        String invalidAccountIban = invalidAccount != null
                ? invalidAccount.getIban() : null;

        if (invalidAccount != null) {
            addTransactionErrors(invalidAccountIban, splitAmount);
        } else {
            addTransactions(splitAmount);
        }
    }

    /**
     * Method used to find out if there is an account in the list of accounts
     * that are supposed to make the split payment that does not have sufficient funds
     * or does not exist.
     * It iterates through the list of accounts and checks if each account has sufficient
     * funds for the split payment. If an account does not have sufficient funds, it returns
     * that account.
     *
     * @param splitAmount -> the amount to be paid to each account
     * @return the account that does not have sufficient funds for the split payment and exist
     * or null if all accounts have sufficient funds
     */
    private BankAccount findInvalidAccount(final double splitAmount) {
        BankAccount invalidAccount = null;
        for (String account : getAccounts()) {
            BankAccount bankAccount = bank.findAccountByIban(account);
            if (bankAccount == null) {
                return null;
            }
            if (!bankAccount.hasSufficientFunds(splitAmount, getCurrency(), bank)) {
                invalidAccount = bankAccount;
            }
        }
        return invalidAccount;
    }

    /**
     * Method used to add a transaction error to all accounts that are supposed to make
     * the split payment stating that the account with the invalidAccountIban does not have
     * sufficient funds for the split payment.
     * It iterates through the list of accounts, creates a transaction error for each account
     * and adds it to the account transactions.
     *
     * @param invalidAccountIban -> the account that does not have sufficient funds
     *                           for the split payment
     * @param splitAmount -> the amount that should have been paid by each account
     */
    private void addTransactionErrors(final String invalidAccountIban, final double splitAmount) {
        for (String account : getAccounts()) {
            BankAccount bankAccount = bank.findAccountByIban(account);
            if (bankAccount == null) {
                return;
            }
            User user = bank.getUserByAccount(account);
            if (user == null) {
                return;
            }
            String formattedAmount = String.format("%.2f", getAmount());
            Transaction transaction = new Transaction
                    .TransactionBuilder(getTimestamp(), "Split payment of "
                    + formattedAmount + " " + getCurrency())
                    .amount(splitAmount)
                    .currency(getCurrency())
                    .separateAmountAndCurrency(true)
                    .involvedAccounts(getAccounts())
                    .error("Account " + invalidAccountIban
                            + " has insufficient funds for a split payment.")
                    .build();
            bankAccount.addTransaction(transaction);
            user.addTransaction(transaction);
        }
    }

    /**
     * Method used to make the split payment between the accounts.
     * It iterates through the list of accounts, calls the payOnline method from the bank
     * to make the payment, creates a transaction for each account and adds it to the account
     * transactions.
     *
     * @param splitAmount -> the amount to be deducted from each account
     */
    private void addTransactions(final double splitAmount) {
        for (String account : getAccounts()) {
            BankAccount bankAccount = bank.findAccountByIban(account);
            if (bankAccount == null) {
                return;
            }
            bankAccount.payOnline(bank, splitAmount, getCurrency());
            String formattedAmount = String.format("%.2f", getAmount());
            Transaction transaction = new Transaction
                    .TransactionBuilder(getTimestamp(), "Split payment of "
                    + formattedAmount + " " + getCurrency())
                    .amount(splitAmount)
                    .currency(getCurrency())
                    .separateAmountAndCurrency(true)
                    .involvedAccounts(getAccounts())
                    .build();
            User user = bank.getUserByAccount(account);
            if (user == null) {
                return;
            }
            user.addTransaction(transaction);
        }
    }
}
