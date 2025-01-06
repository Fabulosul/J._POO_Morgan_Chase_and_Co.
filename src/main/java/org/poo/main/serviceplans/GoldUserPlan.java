package org.poo.main.serviceplans;

public class GoldUserPlan implements UserPlan{

    @Override
    public double calculateCommission(double amount) {
        double commission = 0;
        return amount * commission;
    }

    @Override
    public String getPlanName() {
        return "gold";
    }

    @Override
    public boolean canUpgradePlan(String planName) {
        return false;
    }

    @Override
    public double getCashbackPercentage(double amountSpent) {
        if (amountSpent >= 100 && amountSpent < 300) {
            return 0.005;
        }
        if (amountSpent >= 300 && amountSpent < 500) {
            return 0.0055;
        }
        if (amountSpent >= 500) {
            return 0.007;
        }
        return 0;
    }
}