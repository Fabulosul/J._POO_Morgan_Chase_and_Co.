package org.poo.main.serviceplans;

public class SilverUserPlan implements UserPlan {

    @Override
    public double calculateCommission(double amount) {
        double commission = amount < 500 ? 0 : 0.001;
        return amount * commission;
    }

    @Override
    public String getPlanName() {
        return "silver";
    }

    @Override
    public boolean canUpgradePlan(String planName) {
        return planName.equals("gold");
    }

    @Override
    public double getCashbackPercentage(double amountSpent) {
        if (amountSpent >= 100 && amountSpent < 300) {
            return 0.003;
        }
        if (amountSpent >= 300 && amountSpent < 500) {
            return 0.004;
        }
        if (amountSpent >= 500) {
            return 0.005;
        }
        return 0;
    }
}