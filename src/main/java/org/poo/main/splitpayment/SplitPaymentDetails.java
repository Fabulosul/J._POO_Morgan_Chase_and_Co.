package org.poo.main.splitpayment;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.bank.Bank;
import org.poo.main.bankaccounts.BankAccount;
import org.poo.main.transaction.Transaction;
import org.poo.main.user.User;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public final class SplitPaymentDetails {
    public enum SplitPaymentType {
        EQUAL,
        CUSTOM
    }

    private Bank bank;
    private double amount;
    private SplitPaymentType paymentType;
    private List<Participant> participants;
    private String currency;
    private int timestamp;

    public SplitPaymentDetails(final Bank bank, final double amount,
                               final SplitPaymentType paymentType,
                               final String currency, final int timestamp) {
        this.bank = bank;
        this.amount = amount;
        this.paymentType = paymentType;
        this.participants = new ArrayList<>();
        this.currency = currency;
        this.timestamp = timestamp;
    }

    /**
     * Methos used to add a participant to the split payment.
     *
     * @param user -> The user that will participate in the split payment.
     * @param bankAccount -> The bank account of the user.
     * @param splitAmount -> The amount that the user will pay.
     */
    public void addParticipant(final User user, final BankAccount bankAccount,
                               final double splitAmount) {
        participants.add(new Participant(user, bankAccount, splitAmount));
    }

    /**
     * Method used to accept the payment of a user.
     * It will change the payment status of the user to ACCEPTED
     * and then check if the payment is complete by calling isPaymentComplete().
     * If the payment is complete, it will call makeSplitPayment().
     *
     * @param user -> The user that will accept the payment.
     * @return true if the payment is complete, false otherwise.
     */
    public boolean acceptPayment(final User user) {
        for (Participant participant : participants) {
            if (participant.getUser().equals(user)) {
                participant.setPaymentStatus(Participant.PaymentStatus.ACCEPTED);
            }
        }
        if (isPaymentComplete()) {
            makeSplitPayment();
            return true;
        }
        return false;
    }

    /**
     * Method used to reject the payment of a user.
     * It will change the payment status of the user to REJECTED
     * and then call rejectSplitPayment().
     *
     * @param user -> The user that will reject the payment.
     */
    public void rejectPayment(final User user) {
        for (Participant participant : participants) {
            if (participant.getUser().equals(user)) {
                participant.setPaymentStatus(Participant.PaymentStatus.REJECTED);
                break;
            }
        }
        rejectSplitPayment();
    }

    /**
     * Method used to check if the payment is complete.
     * It will iterate through all the participants and check if their payment status is ACCEPTED.
     * @return true if the payment is complete, false otherwise.
     */
    public boolean isPaymentComplete() {
        for (Participant participant : participants) {
            if (participant.getPaymentStatus() != Participant.PaymentStatus.ACCEPTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Method used to make a split payment.
     * It will check if there is an invalid account by calling findInvalidAccount().
     * If there is an invalid account, it will call addTransactionError() with the invalid account.
     * Otherwise, it will iterate through all the participants and make the payment
     * by calling payWithoutCommission() method from the bank account class.
     * Finally, it will call addSuccessfulTransaction() to add the successful transaction
     * to all the users.
     */
    public void makeSplitPayment() {
        BankAccount invalidAccount = findInvalidAccount();
        if (invalidAccount != null) {
            addTransactionError(invalidAccount.getIban(), "insufficientFunds");
        } else {
            for (Participant participant : participants) {
                BankAccount bankAccount = participant.getBankAccount();
                bankAccount.payWithoutCommission(bank, participant.getAmount(), currency);
            }
            addSuccessfulTransaction();
        }
    }

    /**
     * Method used to add an error transaction to all the users.
     */
    public void rejectSplitPayment() {
        addTransactionError(null, "rejectSplitPayment");
    }

    /**
     * Method used to find an invalid account.
     * It will iterate through all the participants and check if the bank account
     * of each user has sufficient funds for the payment.
     *
     * @return the invalid bank account, null otherwise.
     */
    private BankAccount findInvalidAccount() {
        for (Participant participant : participants) {
            BankAccount bankAccount = participant.getBankAccount();
            if (!bankAccount.hasSufficientFunds(participant.getAmount(), currency, bank)) {
                return bankAccount;
            }
        }
        return null;
    }

    /**
     * Method used to add a successful transaction to all the users.
     */
    private void addSuccessfulTransaction() {
        for (Participant participant : participants) {
            Transaction transaction = createTransaction(null, "success");
            User user = participant.getUser();
            if (user == null) {
                return;
            }
            user.addTransaction(transaction);
        }
    }

    /**
     * Method used to add an error transaction to all the users.
     *
     * @param invalidAccount -> The invalid account.
     * @param message -> The message of the error transaction.
     */
    private void addTransactionError(final String invalidAccount, final String message) {
        List<User> users = new ArrayList<>();
        for (Participant participant : participants) {
            Transaction transaction = createTransaction(invalidAccount, message);
            User user = participant.getUser();
            if (user == null) {
                return;
            }
            if (!users.contains(user)) {
                users.add(user);
                user.addTransaction(transaction);
            }
        }
    }

    /**
     * Method used to create a transaction with a specific message.
     * Depending on the message and if the invalidAccount is null or not,
     * it will create a different transaction.
     *
     * @param invalidAccount -> the account that canceled the payment.
     * @param message -> the message of the transaction.
     * @return the transaction with the given message.
     */
    private Transaction createTransaction(final String invalidAccount, final String message) {
        List<String> accounts = new ArrayList<>();
        List<Double> amountPerUser = new ArrayList<>();
        for (Participant participant : participants) {
            amountPerUser.add(participant.getAmount());
            accounts.add(participant.getBankAccount().getIban());
        }

        String formattedAmount = String.format("%.2f", amount);
        Transaction.TransactionBuilder builder = new Transaction
                .TransactionBuilder(timestamp, "Split payment of "
                + formattedAmount + " " + currency)
                .currency(getCurrency())
                .involvedAccounts(accounts);

        if (paymentType == SplitPaymentType.CUSTOM) {
            builder.amountForUsers(amountPerUser)
                    .currencyWithoutAmount(true)
                    .splitPaymentType("custom");
        } else {
            builder.amount(amount / participants.size())
                    .separateAmountAndCurrency(true)
                    .splitPaymentType("equal");
        }

        if (invalidAccount != null) {
            builder.error("Account " + invalidAccount
                    + " has insufficient funds for a split payment.");
        }
        if (message.equals("rejectSplitPayment")) {
            builder.error("One user rejected the payment.");
        }

        return builder.build();
    }
}
