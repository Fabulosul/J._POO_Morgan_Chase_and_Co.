package org.poo.main.commands;

import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bankaccounts.BankAccount;
import org.poo.main.user.User;
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
     * It creates a SplitPaymentDetails object depending on the split payment type
     * and adds the participants to it by iterating through the accounts and users.
     * The SplitPaymentDetails object is then added to the bank's list of split payments.
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
