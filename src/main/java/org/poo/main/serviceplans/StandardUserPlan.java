package org.poo.main.serviceplans;

public class StandardUserPlan implements UserPlan{

    @Override
    public double calculateCommission(double amount) {
        double commission = 0.002;
        return amount * commission;
    }

    @Override
    public String getPlanName() {
        return "standard";
    }

    @Override
    public boolean canUpgradePlan(String planName) {
        return planName.equals("silver") || planName.equals("gold");
    }

    @Override
    public double getCashbackPercentage(double amountSpent) {
        if (amountSpent >= 100 && amountSpent < 300) {
            return 0.001;
        }
        if (amountSpent >= 300 && amountSpent < 500) {
            return 0.002;
        }
        if (amountSpent >= 500) {
            return 0.0025;
        }
        return 0;
    }
}