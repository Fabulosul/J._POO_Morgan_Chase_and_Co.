package org.poo.main.splitpayment;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.bank.BankAccount;
import org.poo.main.bank.User;

@Getter
@Setter
public class Participant {
    public enum PaymentStatus {
        PENDING,
        ACCEPTED,
        REJECTED
    }
    private User user;
    private BankAccount bankAccount;
    private double amount;
    private PaymentStatus paymentStatus;

    public Participant(final User user, final BankAccount bankAccount, final double amount) {
        this.user = user;
        this.bankAccount = bankAccount;
        this.amount = amount;
        this.paymentStatus = PaymentStatus.PENDING;
    }
}
