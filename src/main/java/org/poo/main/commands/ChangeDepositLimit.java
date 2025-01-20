package org.poo.main.commands;

import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bankaccounts.BankAccount;
import org.poo.main.bankaccounts.BusinessAccount;
import org.poo.main.user.User;
import org.poo.main.businessusers.BusinessUser;

@Getter
@Setter
public final class ChangeDepositLimit extends Command implements CommandInterface {
    private Bank bank;

    public ChangeDepositLimit(final Bank bank, final CommandInput command) {
        super(command);
        this.bank = bank;
    }

    /**
     * Method used to change the deposit limit of a business user.
     * It checks is the account is a business account and retrieves the business user
     * by the name of the user. Then calls the changeDepositLimit method from the business user
     * class.
     *
     * @see BusinessUser#changeDepositLimit(double)
     */
    @Override
    public void execute() {
        User user = bank.getUserByMail(getEmail());
        if (user == null) {
            return;
        }
        BankAccount businessAccount = bank.findAccountByIban(getAccount());
        if (businessAccount == null) {
            return;
        }
        if (businessAccount.getAccountType().equals("business")) {
            String username = user.getLastName() + " " + user.getFirstName();
            BusinessUser businessUser = ((BusinessAccount) businessAccount)
                    .getBusinessUserByName(username);
            businessUser.changeDepositLimit(getAmount());
        }
    }
}
