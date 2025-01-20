package org.poo.main.commands;

import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bankaccounts.BankAccount;
import org.poo.main.user.User;

@Getter
@Setter
public final class SetAlias extends Command implements CommandInterface {
    private Bank bank;

    public SetAlias(final Bank bank, final CommandInput command) {
        super(command);
        this.bank = bank;
    }

    /**
     * Method overridden from the CommandInterface to set an alias for a bank account
     * It gets the user by email and the bank account by iban and adds the alias in
     * the aliasToAccountMap by calling the addAccountByAlias method from the User class.
     * If the user or the bank account can't be found, the method terminates.
     */
    @Override
    public void execute() {
        User user = bank.getUserByMail(getEmail());
        if (user == null) {
            return;
        }
        BankAccount bankAccount = bank.findAccountByIban(getAccount());
        if (bankAccount == null) {
            return;
        }
        user.addAccountByAlias(getAlias(), bankAccount);
    }
}
