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

    public PaymentDetails(final double amount, final String currency,
                          final Commerciant commerciant, final User user) {
        this.amount = amount;
        this.currency = currency;
        this.commerciant = commerciant;
        this.user = user;
    }
}
