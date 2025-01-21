package org.poo.main.cashback;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.bank.Bank;
import org.poo.main.bankaccounts.BankAccount;

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

    /**
     * Method that applies the cashback strategy based on the number of transactions.
     * It firstly checks if any previously obtained vouchers can be used.
     * Then it updates the spending amount of the account and the number of transactions.
     * Finally, if the current transaction is eligible for a voucher, it adds it to the account.
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
        if (commerciant.getCashbackStrategy() == Commerciant.CashbackStrategy.NR_OF_TRANSACTIONS) {
            processPayment(paymentDetails, commerciant, convertedAmount, bankAccount);

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
