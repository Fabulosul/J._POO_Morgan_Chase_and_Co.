package org.poo.main.cashback;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.bank.Bank;
import org.poo.main.bank.BankAccount;
import org.poo.main.bank.BusinessAccount;
import org.poo.main.bank.User;

import java.util.Iterator;

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

        Iterator<Voucher> iterator = bankAccount.getCashbackVouchers().iterator();
        while (iterator.hasNext()) {
            Voucher voucher = iterator.next();
            if (voucher.getCategory() == commerciant.getCategory()) {
                double convertedAmount = bank.convertCurrency(paymentDetails.getAmount(),
                        paymentDetails.getCurrency(), bankAccount.getCurrency());
                bankAccount.addMoney(convertedAmount * voucher.getPercentage());
                iterator.remove();
            }
        }

        double convertedAmount = bank.convertCurrency(paymentDetails.getAmount(),
                paymentDetails.getCurrency(), "RON");
        if(commerciant.getType() == Commerciant.CashbackStrategy.SPENDING_THRESHOLD) {
            commerciant.setNrOfTransactions(commerciant.getNrOfTransactions() + 1);
            if(bankAccount.getAccountType().equals("business")) {
                BusinessAccount businessAccount = (BusinessAccount) bankAccount;
                if(businessAccount.getUserRole(paymentDetails.getUser()) != BusinessAccount.UserRole.OWNER) {
                    commerciant.setAmountSpent(commerciant.getAmountSpent() + convertedAmount);
                    commerciant.addUser(paymentDetails.getUser());
                }
            }

            User user = bank.getUserByAccount(bankAccount.getIban());
            bankAccount.setSpendingThresholdAmount(bankAccount.getSpendingThresholdAmount() + convertedAmount);
            double cashbackPercentage = user.getServicePlan().getCashbackPercentage(bankAccount.getSpendingThresholdAmount());
            double cashback = convertedAmount * cashbackPercentage;
            bankAccount.addMoney(bank.convertCurrency(cashback, "RON", bankAccount.getCurrency()));
        }
    }
}