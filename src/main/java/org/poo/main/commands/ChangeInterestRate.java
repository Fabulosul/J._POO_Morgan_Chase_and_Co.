package org.poo.main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bank.BankAccount;
import org.poo.main.bank.SavingsBankAccount;
import org.poo.main.bank.Transaction;
import org.poo.main.bank.User;

@Getter
@Setter
public final class ChangeInterestRate extends Command implements CommandInterface {
    private Bank bank;
    private ArrayNode output;

    public ChangeInterestRate(final Bank bank, final CommandInput command,
                              final ArrayNode output) {
        super(command);
        this.bank = bank;
        this.output = output;
    }

    /**
     * Method overridden from the CommandInterface to change the interest rate of a
     * savings account.
     * It gets the user and bank account by the account number and then checks if the account
     * is a savings account. If it is, it changes the interest rate and registers the transaction.
     * If not, it adds an error to the output.
     * Also, if the account is not found, it adds a specific error to the output to indicate that.
     */
    @Override
    public void execute() {
        User user = bank.getUserByAccount(getAccount());
        if (user == null) {
            addErrorToOutput("Account not found");
            return;
        }
        BankAccount bankAccount = user.getAccountByIban(getAccount());
        if (bankAccount == null) {
            return;
        }
        if (bankAccount.getAccountType().equals("savings")) {
            ((SavingsBankAccount) bankAccount).changeInterestRate(getInterestRate());
            registerTransaction(bankAccount, user);
        } else {
            addErrorToOutput("This is not a savings account");
        }
    }

    /**
     * Method used to add an error to the output ArrayNode.
     * It takes a description as a parameter and creates an error node depending on the
     * description. If the description is "Account not found", it creates an error node
     * indicating that the account was not found. If the description is "This is not a
     * savings account", it creates an error node outlining that the account is not a
     * savings account and therefore the operation is not supported.
     *
     * @param description -> the description of the error to indicate the type of error
     *                    that needs to be added to the output
     */
    private void addErrorToOutput(final String description) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode errorNode = mapper.createObjectNode();
        if (description.equals("Account not found")) {
            errorNode.put("command", "report");

            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("description", "Account not found");
            outputNode.put("timestamp", getTimestamp());

            errorNode.set("output", outputNode);
            errorNode.put("timestamp", getTimestamp());
        } else {
            errorNode.put("command", "changeInterestRate");

            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("description", "This is not a savings account");
            outputNode.put("timestamp", getTimestamp());

            errorNode.set("output", outputNode);
            errorNode.put("timestamp", getTimestamp());
        }
        output.add(errorNode);
    }

    /**
     * Method used to register an interest rate change transaction.
     * It takes a user as a parameter and creates a transaction with the current timestamp
     * and a description indicating that the interest rate of the account was changed to the new
     * interest rate and then adds it to the user's transaction list.
     *
     * @param user -> the user for which the transaction needs to be registered
     */
    private void registerTransaction(final BankAccount bankAccount, final User user) {
        Transaction transaction = new Transaction
                .TransactionBuilder(getTimestamp(),
                "Interest rate of the account changed to " + getInterestRate())
                .build();
        user.addTransaction(transaction);
        if (bankAccount != null) {
            bankAccount.addTransaction(transaction);
        }
    }
}
