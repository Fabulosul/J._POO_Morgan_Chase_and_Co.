package org.poo.main.cashback;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.bank.Bank;
import org.poo.main.bankaccounts.BankAccount;
import org.poo.main.user.User;

@Getter
@Setter
public final class SpendingThresholdObserver implements CashbackObserver {
    private Bank bank;
    private BankAccount bankAccount;

    public SpendingThresholdObserver(final Bank bank, final BankAccount bankAccount) {
        this.bank = bank;
        this.bankAccount = bankAccount;
    }

    /**
     * Method used to add cashback to the account based on the amount spent
     * on certain categories of commerciants.
     * It starts by processing the vouchers that can be used for the payment
     * and then updates the amount spent by the user and the number of transactions
     * made to the commerciant.
     * Finally, it adds the cashback to the user's account based on the amount spent
     * if it is the case.
     *
     * @param paymentDetails -> the details of the payment
     */
    @Override
    public void update(final PaymentDetails paymentDetails) {
        Commerciant commerciant = paymentDetails.getCommerciant();
        if (commerciant == null) {
            return;
        }

        processVouchers(bank, bankAccount, paymentDetails, commerciant);

        double convertedAmount = bank.convertCurrency(paymentDetails.getAmount(),
                paymentDetails.getCurrency(), "RON");
        if (commerciant.getCashbackStrategy() == Commerciant.CashbackStrategy.SPENDING_THRESHOLD) {
            processPayment(paymentDetails, commerciant, convertedAmount, bankAccount);

            User user = bank.getUserByAccount(bankAccount.getIban());
            if (user == null) {
                return;
            }
            bankAccount.setSpendingThresholdAmount(bankAccount.getSpendingThresholdAmount()
                    + convertedAmount);
            double thresholdAmount = bankAccount.getSpendingThresholdAmount();
            double cashbackPercentage = user.getServicePlan()
                    .getCashbackPercentage(thresholdAmount);
            double cashback = convertedAmount * cashbackPercentage;
            bankAccount.addMoney(bank.convertCurrency(cashback, "RON",
                    bankAccount.getCurrency()));
        }
    }
}
