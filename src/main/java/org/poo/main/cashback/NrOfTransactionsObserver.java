package org.poo.main.cashback;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.bank.Bank;
import org.poo.main.bank.BankAccount;
import org.poo.main.bank.BusinessAccount;

import java.util.Iterator;

import static org.poo.main.cashback.Commerciant.Category.CLOTHES;
import static org.poo.main.cashback.Commerciant.Category.FOOD;
import static org.poo.main.cashback.Commerciant.Category.TECH;

@Getter
@Setter
public final class NrOfTransactionsObserver implements CashbackObserver {
    private Bank bank;
    private BankAccount bankAccount;
    private static final int TRANSACTION_THRESHOLD_FOOD = 2;
    private static final int TRANSACTION_THRESHOLD_CLOTHES = 5;
    private static final int TRANSACTION_THRESHOLD_TECH = 10;
    private static final double FOOD_CASHBACK = 0.02;
    private static final double CLOTHES_CASHBACK = 0.05;
    private static final double TECH_CASHBACK = 0.10;

    public NrOfTransactionsObserver(final Bank bank, final BankAccount bankAccount) {
        this.bank = bank;
        this.bankAccount = bankAccount;
    }

    @Override
    public void update(final PaymentDetails paymentDetails) {
        Commerciant commerciant = paymentDetails.getCommerciant();
        if (commerciant == null) {
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
        if (commerciant.getType() == Commerciant.CashbackStrategy.NR_OF_TRANSACTIONS) {
            commerciant.setNrOfTransactions(commerciant.getNrOfTransactions() + 1);
            if (bankAccount.getAccountType().equals("business")) {
                BusinessAccount businessAccount = (BusinessAccount) bankAccount;
                if (businessAccount.getUserRole(paymentDetails.getUser())
                        != BusinessAccount.UserRole.OWNER) {
                    commerciant.setAmountSpent(commerciant.getAmountSpent() + convertedAmount);
                    commerciant.addUser(paymentDetails.getUser());
                }
            }
            switch (commerciant.getNrOfTransactions()) {
                case TRANSACTION_THRESHOLD_FOOD:
                    bankAccount.addVoucher(new Voucher(FOOD_CASHBACK, FOOD));
                    break;
                case TRANSACTION_THRESHOLD_CLOTHES:
                    bankAccount.addVoucher(new Voucher(CLOTHES_CASHBACK, CLOTHES));
                    break;
                case TRANSACTION_THRESHOLD_TECH:
                    bankAccount.addVoucher(new Voucher(TECH_CASHBACK, TECH));
                    break;
                default:
                    break;
            }
        }
    }
}
