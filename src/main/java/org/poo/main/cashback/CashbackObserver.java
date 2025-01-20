package org.poo.main.cashback;

import org.poo.main.bankaccounts.BankAccount;
import org.poo.main.bankaccounts.BusinessAccount;

import java.util.Iterator;
import java.util.List;

public interface CashbackObserver {
    /**
     * Method overridden by the classes that implement this interface
     * to add cashback to the account based on the payment details.
     *
     * @param paymentDetails -> the details of the payment
     */
    void update(PaymentDetails paymentDetails);

    /**
     * Method used to check if any of the available vouchers can be used
     * for the current payment and if so, to add the cashback to the account.
     *
     * @param bankAccount -> the bank account of the user
     * @param paymentDetails -> the details of the payment
     * @param commerciant -> the commerciant to which the payment was made
     */
    default List<Voucher> processVouchers(BankAccount bankAccount, PaymentDetails paymentDetails,
                                          Commerciant commerciant) {
        Iterator<Voucher> iterator = bankAccount.getCashbackVouchers().iterator();
        while (iterator.hasNext()) {
            Voucher voucher = iterator.next();
            if (voucher.getCategory() == commerciant.getCategory()) {
                bankAccount.addMoney(paymentDetails.getAmount() * voucher.getPercentage());
                iterator.remove();
            }
        }
        return bankAccount.getCashbackVouchers();
    }

    /**
     * Helper method used to process the payment details and update the commerciant's
     * number of transactions and amount spent.
     * If the bank account is a business account, it also checks if the user is an owner
     * and if not, it adds the user to the commerciant's list of users.
     * The amount spend and the users lists are used for the business reports feature
     * and must not contain the payments made by the owner.
     *
     * @param paymentDetails -> the details of the payment
     * @param commerciant -> the commerciant to which the payment was made
     * @param convertedAmount -> the amount converted to RON
     * @param bankAccount -> the bank account of the user
     *
     * @see org.poo.main.commands.BusinessReport for more details
     */
    default void processPayment(PaymentDetails paymentDetails, Commerciant commerciant,
                                double convertedAmount, BankAccount bankAccount) {
        commerciant.setNrOfTransactions(commerciant.getNrOfTransactions() + 1);
        if (!bankAccount.getAccountType().equals("business")) {
            return;
        }

        BusinessAccount businessAccount = (BusinessAccount) bankAccount;

        if (businessAccount.getUserRole(paymentDetails.getUser())
                == BusinessAccount.UserRole.OWNER) {
            return;
        }

        commerciant.setAmountSpent(commerciant.getAmountSpent() + convertedAmount);
        commerciant.addUser(paymentDetails.getUser());
    }
}
