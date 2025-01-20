package org.poo.main.commands;

import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bankaccounts.BankAccount;
import org.poo.main.bankaccounts.BusinessAccount;
import org.poo.main.user.User;
import org.poo.main.businessusers.Owner;

@Getter
@Setter
public final class AddNewBusinessAssociate extends Command implements CommandInterface {
    private Bank bank;

    public AddNewBusinessAssociate(final Bank bank, final CommandInput command) {
        super(command);
        this.bank = bank;
    }

    /**
     * Method used to find the owner of the business account and add a new associate to it
     * by calling the addNewAssociate method from the Owner class.
     *
     * @see Owner#addNewAssociate(User, String) for more details about the implementation.
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
        if (!businessAccount.getAccountType().equals("business")) {
            return;
        }
        Owner owner = ((BusinessAccount) businessAccount).getOwner();
        owner.addNewAssociate(user, getRole());
    }
}
