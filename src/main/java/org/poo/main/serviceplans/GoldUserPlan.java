package org.poo.main.serviceplans;

public final class GoldUserPlan implements UserPlan {
    private static final double FIRST_LEVEL_CASHBACK = 0.05;
    private static final double SECOND_LEVEL_CASHBACK = 0.0055;
    private static final double THIRD_LEVEL_CASHBACK = 0.007;

    @Override
    public double calculateCommission(final double amount) {
        double commission = 0;
        return amount * commission;
    }

    @Override
    public String getPlanName() {
        return "gold";
    }

    @Override
    public boolean canUpgradePlan(final String planName) {
        return false;
    }

    @Override
    public double getCashbackPercentage(final double amountSpent) {
        if (amountSpent >= FIRST_THRESHOLD && amountSpent < SECOND_THRESHOLD) {
            return FIRST_LEVEL_CASHBACK;
        }
        if (amountSpent >= SECOND_THRESHOLD && amountSpent < THIRD_THRESHOLD) {
            return SECOND_LEVEL_CASHBACK;
        }
        if (amountSpent >= THIRD_THRESHOLD) {
            return THIRD_LEVEL_CASHBACK;
        }
        return 0;
    }
}
