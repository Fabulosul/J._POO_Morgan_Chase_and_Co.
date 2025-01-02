package org.poo.main.commands;

import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bank.BankAccount;
import org.poo.main.bank.User;

@Getter
@Setter
public final class SetMinBalance extends Command implements CommandInterface {
    private Bank bank;

    public SetMinBalance(final Bank bank, final CommandInput command) {
        super(command);
        this.bank = bank;
    }

    /**
     * Method overridden from CommandInterface to set the minimum balance of a bank account.
     * It gets the user and the bank account by the account number and sets the minimum balance
     * by calling the setMinBalance method from the bank account.
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
        bankAccount.setMinBalance(getMinBalance());
    }
}
