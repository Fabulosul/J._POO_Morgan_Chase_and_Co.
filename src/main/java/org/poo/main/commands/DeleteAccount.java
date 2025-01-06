package org.poo.main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bank.BankAccount;
import org.poo.main.bank.Transaction;
import org.poo.main.bank.User;
import org.poo.main.cashback.CashbackObserver;

@Getter
@Setter
public final class DeleteAccount extends Command implements CommandInterface {
    private Bank bank;
    private ArrayNode output;

    public DeleteAccount(final Bank bank, final CommandInput command,
                         final ArrayNode output) {
        super(command);
        this.bank = bank;
        this.output = output;
    }

    /**
     * Method overridden from the CommandInterface to delete the account with the given iban.
     * It gets the user that has the account and the bank account associated with it
     * and then checks if the account can be deleted(its balance of the account is 0).
     * If it can be deleted, it removes the account and adds a success message to the output.
     * If it can't be deleted, it registers a transaction error and adds an error message
     * to the output.
     * Also, if the user or the account is not found, it returns without doing anything.
     */
    @Override
    public void execute() {
        User user = bank.getUserByMail(getEmail());
        if (user == null) {
            return;
        }
        BankAccount bankAccount = user.getAccountByIban(getAccount());
        if (bankAccount == null) {
            return;
        }

        if (bankAccount.getBalance() == 0) {
            // remove observers
            for (int i = 0; i < bankAccount.getCashbackObservers().size(); i++) {
                CashbackObserver observer = bankAccount.getCashbackObservers().get(i);
                bankAccount.removeCashbackObserver(observer);
            }
            user.removeBankAccount(bankAccount);
            addMessageToOutput("success");
        } else {
            registerTransaction(user);
            addMessageToOutput("error");
        }
    }

    /**
     * Method used to add a failure or success message to the output.
     * Depending on the state, it adds a success message or an error message to the output
     * by creating a new object node and adding it to the output array.
     *
     * @param state -> the state of the account deletion(success or error)
     */
    private void addMessageToOutput(final String state) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("command", "deleteAccount");

        ObjectNode outputNode = mapper.createObjectNode();
        if (state.equals("success")) {
            outputNode.put("success", "Account deleted");
        } else {
            outputNode.put("error",
                    "Account couldn't be deleted - see org.poo.transactions for details");
        }
        outputNode.put("timestamp", getTimestamp());

        objectNode.set("output", outputNode);
        objectNode.put("timestamp", getTimestamp());

        output.add(objectNode);
    }

    /**
     * Method used to add the transaction error to the user's transactions.
     * It creates a new transaction with the current timestamp and the error message
     * "Account couldn't be deleted - there are funds remaining" and adds it to the user's
     * transactions ArrayNode.
     *
     * @param user -> the user that has the account that couldn't be deleted
     */
    private void registerTransaction(final User user) {
        Transaction transaction = new Transaction
                .TransactionBuilder(getTimestamp(),
                "Account couldn't be deleted - there are funds remaining")
                .build();
        user.addTransaction(transaction);
    }
}
