package org.poo.main.cashback;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.bank.Bank;
import org.poo.main.bank.BankAccount;
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

        if(commerciant.getType() == Commerciant.CashbackStrategy.NR_OF_TRANSACTIONS) {
            bankAccount.setNrOfTransactions(bankAccount.getNrOfTransactions() + 1);
            switch(bankAccount.getNrOfTransactions()) {
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