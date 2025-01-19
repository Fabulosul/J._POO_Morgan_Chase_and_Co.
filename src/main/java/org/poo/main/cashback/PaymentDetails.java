package org.poo.main.cashback;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.bank.User;


@Getter
@Setter
public class PaymentDetails {
    private double amount;
    private String currency;
    private Commerciant commerciant;
    private User user;

    public PaymentDetails(double amount, String currency, Commerciant commerciant, User user) {
        this.amount = amount;
        this.currency = currency;
        this.commerciant = commerciant;
        this.user = user;
    }
}