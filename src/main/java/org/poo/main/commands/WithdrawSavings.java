package org.poo.main.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bank.BankAccount;
import org.poo.main.bank.Transaction;
import org.poo.main.bank.User;

@Getter
@Setter
public class WithdrawSavings extends Command implements CommandInterface {
    private Bank bank;
    private ArrayNode output;


    public WithdrawSavings(final Bank bank, final CommandInput command, ArrayNode output) {
        super(command);
        this.bank = bank;
        this.output = output;
    }

    @Override
    public void execute() {
        User user = bank.getUserByAccount(getAccount());
        if (user == null) {
            return;
        }
        if (user.getAge() < 21) {
            registerTransactionError(null, user, "You don't have the minimum age required.");
            return;
        }
        BankAccount bankAccount = user.getAccountByIban(getAccount());
        if (bankAccount == null) {
            registerTransactionError(null, user, "Account not found");
            return;
        }
        if (!bankAccount.getAccountType().equals("savings")) {
            registerTransactionError(bankAccount, user, "Account is not of type savings.");
            return;
        }
        BankAccount classicAccount = user.findClassicAccountByCurrency(getCurrency());
        if (classicAccount == null) {
            registerTransactionError(bankAccount, user, "You do not have a classic account.");
            return;
        }
        if (!bankAccount.hasSufficientFunds(getAmount(), getCurrency(), bank)) {
            registerTransactionError(bankAccount, user, "Insufficient funds");
            return;
        }

        bankAccount.sendMoneyWithoutCommission(bank, classicAccount, getAmount());
        Transaction transaction = new Transaction
                .TransactionBuilder(getTimestamp(), "Savings withdrawal")
                .amount(getAmount())
                .classicAccountIban(classicAccount.getIban())
                .savingsAccountIban(bankAccount.getIban())
                .build();
        user.addTransaction(transaction);
        user.addTransaction(transaction);
    }

    public void registerTransactionError(BankAccount bankAccount, User user, String message) {
        Transaction transaction = new Transaction
                .TransactionBuilder(getTimestamp(), message)
                .build();
        user.addTransaction(transaction);
        if (bankAccount != null) {
            bankAccount.addTransaction(transaction);
        }
    }
}