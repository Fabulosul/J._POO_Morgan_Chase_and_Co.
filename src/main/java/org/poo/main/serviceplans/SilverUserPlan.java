package org.poo.main.serviceplans;

public final class SilverUserPlan implements UserPlan {
    private static final double FIRST_LEVEL_CASHBACK = 0.003;
    private static final double SECOND_LEVEL_CASHBACK = 0.004;
    private static final double THIRD_LEVEL_CASHBACK = 0.005;
    private static final double SILVER_PLAN_COMMISSION = 0.001;

    @Override
    public double calculateCommission(final double amount) {
        double commission = amount < THIRD_THRESHOLD ? 0 : SILVER_PLAN_COMMISSION;
        return amount * commission;
    }

    @Override
    public String getPlanName() {
        return "silver";
    }

    @Override
    public boolean canUpgradePlan(final String planName) {
        return planName.equals("gold");
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
