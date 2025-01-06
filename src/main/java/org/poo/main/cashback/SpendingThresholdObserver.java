package org.poo.main.cashback;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.bank.Bank;
import org.poo.main.bank.BankAccount;
import org.poo.main.bank.User;

@Getter
@Setter
public class SpendingThresholdObserver implements CashbackObserver {
    private Bank bank;
    private BankAccount bankAccount;

    public SpendingThresholdObserver(Bank bank, BankAccount bankAccount) {
        this.bank = bank;
        this.bankAccount = bankAccount;
    }

    @Override
    public void update(PaymentDetails paymentDetails) {
        Commerciant commerciant = paymentDetails.getCommerciant();
        if(commerciant == null) {
            return;
        }

        double convertedAmount = bank.convertCurrency(paymentDetails.getAmount(),
                paymentDetails.getCurrency(), "RON");
        if(commerciant.getType() == Commerciant.CashbackStrategy.SPENDING_THRESHOLD) {
            bankAccount.setNrOfTransactions(bankAccount.getNrOfTransactions() + 1);
            commerciant.setAmountSpent(commerciant.getAmountSpent() + convertedAmount);
            User user = bank.getUserByAccount(bankAccount.getIban());
            double cashbackPercentage = user.getServicePlan().getCashbackPercentage(convertedAmount);
            double cashback = convertedAmount * cashbackPercentage;
            bankAccount.addMoney(bank.convertCurrency(cashback, "RON", bankAccount.getCurrency()));
        }
    }
}