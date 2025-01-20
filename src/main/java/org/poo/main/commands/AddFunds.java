package org.poo.main.commands;

import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bankaccounts.BankAccount;
import org.poo.main.bankaccounts.BusinessAccount;
import org.poo.main.transaction.Transaction;
import org.poo.main.user.User;

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
     * However, if the account is a business account, it checks if the user is an employee
     * and if the deposit limit is exceeded. If the user is not an employee or the deposit
     * limit is exceeded, the method is terminated without adding the funds.
     * If no user or bank account is found, the method is terminated.
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
        if (bankAccount.getAccountType().equals("business")) {
            BusinessAccount.UserRole userRole = ((BusinessAccount) bankAccount).getUserRole(user);
            if (userRole == null) {
                return;
            }
            if (userRole.equals(BusinessAccount.UserRole.EMPLOYEE)
            && ((BusinessAccount) bankAccount).getDepositLimit() < getAmount()) {
                return;
            }
            Transaction transaction = new Transaction
                    .TransactionBuilder(getTimestamp(), "deposit")
                    .amount(getAmount())
                    .username(user.getLastName() + " " + user.getFirstName())
                    .build();
            ((BusinessAccount) bankAccount).addBusinessTransaction(transaction);
        }
        bankAccount.addMoney(getAmount());
    }
}
