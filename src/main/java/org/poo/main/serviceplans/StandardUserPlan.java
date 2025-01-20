package org.poo.main.serviceplans;

public final class StandardUserPlan implements UserPlan {
    private static final double FIRST_LEVEL_CASHBACK = 0.001;
    private static final double SECOND_LEVEL_CASHBACK = 0.002;
    private static final double THIRD_LEVEL_CASHBACK = 0.0025;
    private static final double STANDARD_PLAN_COMMISSION = 0.002;

    @Override
    public double calculateCommission(final double amount) {
        return amount * STANDARD_PLAN_COMMISSION;
    }

    @Override
    public String getPlanName() {
        return "standard";
    }

    @Override
    public boolean canUpgradePlan(final String planName) {
        return planName.equals("silver") || planName.equals("gold");
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
