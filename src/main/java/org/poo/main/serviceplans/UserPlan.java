package org.poo.main.serviceplans;

public interface UserPlan {
    double FIRST_THRESHOLD = 100;
    double SECOND_THRESHOLD = 300;
    double THIRD_THRESHOLD = 500;

    /**
     * Method designed to be overridden by subclasses to calculate the commission
     * based on the amount spent in RON and the user plan.
     *
     * @param amount - the amount spent in RON
     * @return the commission calculated depending on the plan
     */
    double calculateCommission(double amount);

    /**
     * Method overridden in the subclasses to get the name of the
     * user plan(student, standard, silver, gold)
     *
     * @return the name of the current plan
     */
    String getPlanName();

    /**
     * Method meant to be overridden by subclasses to check if the user can upgrade the plan
     * based on the current plan.
     *
     * @param planName - the name of the current plan
     * @return true if the user can upgrade the plan, false otherwise
     */
    boolean canUpgradePlan(String planName);

    /**
     * Method used be overridden by subclasses to calculate the cashback percentage
     * depending on the amount spent and the user plan.
     *
     * @param amountSpent - the amount spent in RON
     * @return the cashback percentage
     */
    double getCashbackPercentage(double amountSpent);
}
