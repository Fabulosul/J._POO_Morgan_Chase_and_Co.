package org.poo.main.bank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class SavingsBankAccount extends BankAccount {
    private double interestRate;

    public SavingsBankAccount(final Bank bank, final String currency, final double interestRate) {
        super(bank, currency);
        this.interestRate = interestRate;
        setAccountType(AccountType.SAVINGS);
    }

    /**
     * Helper method used to add interest to the account balance.
     */
    public double addInterest() {
        double interestIncome = getBalance() * interestRate;
        setBalance(getBalance() + getBalance() * interestRate);
        return interestIncome;
    }


    /**
     * Helper method used to change the interest rate of the account.
     *
     * @param newInterestRate -> the new interest rate
     */
    public void changeInterestRate(final double newInterestRate) {
        this.interestRate = newInterestRate;
    }
}
