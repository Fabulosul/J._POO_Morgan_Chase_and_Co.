package org.poo.main.cashback;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.bank.Bank;
import org.poo.main.bank.BankAccount;
import org.poo.main.bank.BusinessAccount;
import org.poo.main.bank.User;

import java.util.Iterator;

import static org.poo.main.cashback.Commerciant.Category.*;

@Getter
@Setter
public class NrOfTransactionsObserver implements CashbackObserver {
    private Bank bank;
    private BankAccount bankAccount;

    public NrOfTransactionsObserver(Bank bank, BankAccount bankAccount) {
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
                bankAccount.addMoney(paymentDetails.getAmount() * voucher.getPercentage());
                iterator.remove();
            }
        }

        double convertedAmount = bank.convertCurrency(paymentDetails.getAmount(),
                paymentDetails.getCurrency(), "RON");
        if(commerciant.getType() == Commerciant.CashbackStrategy.NR_OF_TRANSACTIONS) {
            commerciant.setNrOfTransactions(commerciant.getNrOfTransactions() + 1);
            if(bankAccount.getAccountType().equals("business")) {
                BusinessAccount businessAccount = (BusinessAccount) bankAccount;
                if(businessAccount.getUserRole(paymentDetails.getUser()) != BusinessAccount.UserRole.OWNER) {
                    commerciant.setAmountSpent(commerciant.getAmountSpent() + convertedAmount);
                    commerciant.addUser(paymentDetails.getUser());
                }
            }
            switch(commerciant.getNrOfTransactions()) {
                case 2:
                    bankAccount.addVoucher(new Voucher(0.02, FOOD));
                    break;
                case 5:
                    bankAccount.addVoucher(new Voucher(0.05, CLOTHES));
                    break;
                case 10:
                    bankAccount.addVoucher(new Voucher(0.10, TECH));
                    break;
                default:
                    break;
            }
        }
    }
}