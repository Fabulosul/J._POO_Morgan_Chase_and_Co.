package org.poo.main.serviceplans;

public interface UserPlan {
    double FIRST_THRESHOLD = 100;
    double SECOND_THRESHOLD = 300;
    double THIRD_THRESHOLD = 500;

    double calculateCommission(double amount);
    String getPlanName();
    boolean canUpgradePlan(String planName);
    double getCashbackPercentage(double amountSpent);
}
