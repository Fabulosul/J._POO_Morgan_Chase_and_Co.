package org.poo.main.cashback;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class PaymentDetails {
    private double amount;
    private String currency;
    private Commerciant commerciant;

    public PaymentDetails(double amount, String currency, Commerciant commerciant) {
        this.amount = amount;
        this.currency = currency;
        this.commerciant = commerciant;
    }
}