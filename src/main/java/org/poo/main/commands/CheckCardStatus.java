package org.poo.main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bankaccounts.BankAccount;
import org.poo.main.card.Card;
import org.poo.main.user.User;
import org.poo.main.transaction.Transaction;

@Getter
@Setter
public final class CheckCardStatus extends Command implements CommandInterface {
    private Bank bank;
    private ArrayNode output;

    public CheckCardStatus(final Bank bank, final CommandInput command,
                           final ArrayNode output) {
        super(command);
        this.bank = bank;
        this.output = output;
    }

    /**
     * Method overridden from the CommandInterface to check the card status and freeze
     * it if necessary.
     * It gets the card number from the command input and searches for the card in the bank.
     * If the card is not found, an error message is added to the output to indicate that.
     * Then, it gets the bank account and the user associated with the card.
     * If the bank account balance is less than the minimum balance, the card is frozen
     * using the freezeCard method from the Card class and a transaction is registered for the user.
     * If the bank account or the user is not found, the method terminates.
     */
    @Override
    public void execute() {
        Card card = bank.getCardByCardNr(getCardNumber());
        if (card == null) {
            addErrorToOutput();
            return;
        }
        BankAccount bankAccount = bank.getAccountByCardNr(getCardNumber());
        if (bankAccount == null) {
            return;
        }
        User user = bank.getUserByAccount(bankAccount.getIban());
        if (user == null) {
            return;
        }

        if (bankAccount.getMinBalance() >= bankAccount.getBalance()) {
            card.freezeCard();
            registerTransaction(user);
        }
    }

    /**
     * Method used to add an error message to the output in case the card is not found.
     * It creates an object node with the required fields and adds it to the output array.
     */
    private void addErrorToOutput() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();

        ObjectNode outputNode = mapper.createObjectNode();
        outputNode.put("description", "Card not found");
        outputNode.put("timestamp", getTimestamp());

        objectNode.put("command", "checkCardStatus");
        objectNode.set("output", outputNode);
        objectNode.put("timestamp", getTimestamp());
        output.add(objectNode);
    }

    /**
     * Method used to register a "checkCardStatus" transaction for the user.
     * It creates a transaction with the current timestamp and a message indicating that the
     * bank account has reached the minimum balance and the current card will be frozen.
     *
     * @param user the user for which the transaction is registered
     */
    private void registerTransaction(final User user) {
        Transaction transaction = new Transaction
                .TransactionBuilder(getTimestamp(),
                "You have reached the minimum amount of funds, the card will be frozen")
                .build();
        user.addTransaction(transaction);
    }
}
