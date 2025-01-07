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

    public void acceptPayment(User user) {
        for (Participant participant : participants) {
            if (participant.getUser().equals(user)) {
                participant.setPaymentStatus(Participant.PaymentStatus.ACCEPTED);
            }
        }
        if(isPaymentComplete()) {
            makeSplitPayment();
        }
    }

    public void rejectPayment(User user) {
        for (Participant participant : participants) {
            if (participant.getUser().equals(user)) {
                participant.setPaymentStatus(Participant.PaymentStatus.REJECTED);
                break;
            }
        }
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

            return;
        } else {
            for (Participant participant : participants) {
                BankAccount bankAccount = participant.getBankAccount();
                bankAccount.payOnline(bank, amount, currency);
            }
            addSuccessfulTransaction();
        }
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
        List<String> accounts = new ArrayList<>();
        List<Double> amountPerUser = new ArrayList<>();
        for (Participant participant : participants) {
            amountPerUser.add(participant.getAmount());
            accounts.add(participant.getBankAccount().getIban());
        }
        for (Participant participant : participants) {
            BankAccount bankAccount = participant.getBankAccount();
            bankAccount.payOnline(bank, amount, currency);
            String formattedAmount = String.format("%.2f", amount);
            Transaction transaction = new Transaction
                    .TransactionBuilder(timestamp, "Split payment of "
                    + formattedAmount + " " + currency)
                    .amountForUsers(amountPerUser)
                    .currency(getCurrency())
                    .currencyWithoutAmount(true)
                    .splitPaymentType(paymentType == SplitPaymentType.EQUAL ? "equal" : "custom")
                    .involvedAccounts(accounts)
                    .build();
            User user = participant.getUser();
            if (user == null) {
                return;
            }
            user.addTransaction(transaction);
        }
    }

}