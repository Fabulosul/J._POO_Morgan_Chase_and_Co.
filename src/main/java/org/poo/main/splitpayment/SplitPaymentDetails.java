package org.poo.main.splitpayment;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.bank.Bank;
import org.poo.main.bank.BankAccount;
import org.poo.main.bank.Transaction;
import org.poo.main.bank.User;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SplitPaymentDetails {
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

    public SplitPaymentDetails(Bank bank, double amount, SplitPaymentType paymentType,
                               String currency, int timestamp) {
        this.bank = bank;
        this.amount = amount;
        this.paymentType = paymentType;
        this.participants = new ArrayList<>();
        this.currency = currency;
        this.timestamp = timestamp;
    }

    public void addParticipant(User user, BankAccount bankAccount, double amount) {
        participants.add(new Participant(user, bankAccount, amount));
    }

    public void removeParticipant(User user) {
        participants.removeIf(participant -> participant.getUser().equals(user));
    }

    public boolean acceptPayment(User user) {
        for (Participant participant : participants) {
            if (participant.getUser().equals(user)) {
                participant.setPaymentStatus(Participant.PaymentStatus.ACCEPTED);
            }
        }
        if(isPaymentComplete()) {
            makeSplitPayment();
            return true;
        }
        return false;
    }

    public void rejectPayment(User user) {
        for (Participant participant : participants) {
            if (participant.getUser().equals(user)) {
                participant.setPaymentStatus(Participant.PaymentStatus.REJECTED);
                break;
            }
        }
        rejectSplitPayment();
    }

    public boolean isPaymentComplete() {
        for (Participant participant : participants) {
            if (participant.getPaymentStatus() != Participant.PaymentStatus.ACCEPTED) {
                return false;
            }
        }
        return true;
    }

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

    public void rejectSplitPayment() {
        addTransactionError(null, "rejectSplitPayment");
    }

    private BankAccount findInvalidAccount() {
        for (Participant participant : participants) {
            BankAccount bankAccount = participant.getBankAccount();
            if (!bankAccount.hasSufficientFunds(participant.getAmount(), currency, bank)) {
                return bankAccount;
            }
        }
        return null;
    }

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

    private void addTransactionError(String invalidAccount, String message) {
        List<User> users = new ArrayList<>();
        for (Participant participant : participants) {
            Transaction transaction = createTransaction(invalidAccount, message);
            User user = participant.getUser();
            if (user == null) {
                return;
            }
            if(!users.contains(user)) {
                users.add(user);
                user.addTransaction(transaction);
            }
        }
    }

    private Transaction createTransaction(String invalidAccount, String message) {
        List<String> accounts = new ArrayList<>();
        List<Double> amountPerUser = new ArrayList<>();
        for (Participant participant : participants) {
            amountPerUser.add(participant.getAmount());
            accounts.add(participant.getBankAccount().getIban());
        }

        String formattedAmount = String.format("%.2f", amount);
        Transaction.TransactionBuilder builder = new Transaction
                .TransactionBuilder(timestamp, "Split payment of " +
                formattedAmount + " " + currency)
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
            builder.error("Account " + invalidAccount +
                    " has insufficient funds for a split payment.");
        }
        if (message.equals("rejectSplitPayment")) {
            builder.error("One user rejected the payment.");
        }

        return builder.build();
    }



}