package org.poo.main.commands;

import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bank.BankAccount;
import org.poo.main.bank.User;

@Getter
@Setter
public final class AddFunds extends Command implements CommandInterface {
    private Bank bank;

    public AddFunds(final Bank bank, final CommandInput command) {
        super(command);
        this.bank = bank;
    }

    /**
     * Method overridden from the CommandInterface to add funds to a bank account.
     * It gets the user and the bank account by the account number and adds the amount
     * to the account.
     * If no user or bank account is found, the method is terminated.
     */
    @Override
    public void execute() {
        User user = bank.getUserByAccount(getAccount());
        if (user == null) {
            return;
        }
        BankAccount bankAccount = user.getAccountByIban(getAccount());
        if (bankAccount == null) {
            return;
        }
        bankAccount.addMoney(getAmount());
    }
}
