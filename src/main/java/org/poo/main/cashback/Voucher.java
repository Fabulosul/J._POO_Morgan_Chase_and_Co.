package org.poo.main.cashback;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Voucher {
    private double percentage;
    private Commerciant.Category category;

    public Voucher(double percentage, Commerciant.Category category) {
        this.percentage = percentage;
        this.category = category;
    }
}