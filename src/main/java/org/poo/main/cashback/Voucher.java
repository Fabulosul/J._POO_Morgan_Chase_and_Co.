package org.poo.main.cashback;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class Voucher {
    private double percentage;
    private Commerciant.Category category;

    public Voucher(final double percentage, final Commerciant.Category category) {
        this.percentage = percentage;
        this.category = category;
    }
}
