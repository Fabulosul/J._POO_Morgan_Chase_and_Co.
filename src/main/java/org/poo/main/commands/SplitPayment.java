package org.poo.main.commands;

import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bank.BankAccount;
import org.poo.main.bank.User;
import org.poo.main.splitpayment.SplitPaymentDetails;

@Getter
@Setter
public final class SplitPayment extends Command implements CommandInterface {
    private Bank bank;

    public SplitPayment(final Bank bank, final CommandInput command) {
        super(command);
        this.bank = bank;
    }

    /**
     * Method overridden from CommandInterface that does a split payment between
     * multiple accounts.
     * It calculates the amount to be paid to each account and checks if every account
     * have sufficient funds for the payment.
     * Then, it checks if the accounts are valid and if they have sufficient funds
     * by calling the findInvalidAccount method.
     * If an account does not have sufficient funds, it adds an error message to that
     * particular account stating that it has insufficient funds for the payment.
     * If all accounts have sufficient funds, it calls the addTransactions method to
     * make the payment and add the transaction to the accounts.
     */
    @Override
    public void execute() {
        String splitPaymentType = getSplitPaymentType();
        SplitPaymentDetails splitPayment;
        if (splitPaymentType.equals("custom")) {
            splitPayment = new SplitPaymentDetails(bank, getAmount(),
                    SplitPaymentDetails.SplitPaymentType.CUSTOM, getCurrency(), getTimestamp());
        } else {
            splitPayment = new SplitPaymentDetails(bank, getAmount(),
                    SplitPaymentDetails.SplitPaymentType.EQUAL, getCurrency(), getTimestamp());
        }
        for (int i = 0; i < getAccounts().size(); i++) {
            BankAccount bankAccount = bank.findAccountByIban(getAccounts().get(i));
            if (bankAccount == null) {
                return;
            }
            User user = bank.getUserByAccount(bankAccount.getIban());
            if (user == null) {
                return;
            }
            if (splitPaymentType.equals("custom")) {
                splitPayment.addParticipant(user, bankAccount, getAmountForUsers().get(i));
            } else {
                splitPayment.addParticipant(user, bankAccount, getAmount() / getAccounts().size());
            }
        }
        bank.addSplitPayment(splitPayment);
    }
}
