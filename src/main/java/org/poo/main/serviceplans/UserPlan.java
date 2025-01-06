package org.poo.main.serviceplans;

public interface UserPlan {
    double calculateCommission(double amount);
    String getPlanName();
    boolean canUpgradePlan(String planName);
    double getCashbackPercentage(double amountSpent);
}