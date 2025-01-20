package org.poo.main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bankaccounts.BankAccount;
import org.poo.main.user.User;

@Getter
@Setter
public final class Report extends Command implements CommandInterface {
    private Bank bank;
    private ArrayNode output;

    public Report(final Bank bank, final CommandInput command,
                  final ArrayNode output) {
        super(command);
        this.bank = bank;
        this.output = output;
    }

    /**
     * Method overridden from the CommandInterface to add a classic report of a bank account
     * to the output ArrayNode.
     * It gets the user and the bank account by the IBAN and calls the printReport method
     * to add the report to the output.
     * If the user can't be found, the addErrorToOutput method is called to add an error
     * message to the output.
     */
    @Override
    public void execute() {
        User user = bank.getUserByAccount(getAccount());
        if (user == null) {
            addErrorToOutput();
            return;
        }
        BankAccount bankAccount = user.getAccountByIban(getAccount());
        if (bankAccount == null) {
            return;
        }
        printReport(bankAccount);
    }

    /**
     * Method used to create an objectNode with an error message and add it to the output.
     * It adds the command name, the description of the error("Account not found")
     * and the timestamp to the objectNode and then adds it to the output ArrayNode.
     */
    private void addErrorToOutput() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();

        objectNode.put("command", "report");

        ObjectNode outputNode = mapper.createObjectNode();
        outputNode.put("description", "Account not found");
        outputNode.put("timestamp", getTimestamp());

        objectNode.set("output", outputNode);
        objectNode.put("timestamp", getTimestamp());
        output.add(objectNode);
    }

    /**
     * Method used to filter the transactions of a bank account between two timestamps.
     * It creates a new ArrayNode called filteredTransactions and iterates through the transactions
     * of the current bank account. Then, for each transaction, gets the objectNode and
     * the timestamp associated with it. If the timestamp is between the startTimestamp and
     * the endTimestamp, the transaction is added to the new ArrayNode.
     *
     * @implNote We need to upcast the transactions ArrayNode to an ObjectNode in order to
     * access the timestamp field because the get method in arrayNode returns a JsonNode
     * which is the superclass of ObjectNode.
     * @implNote We use asInt() to convert the timestamp to an integer because the get method
     * in the ArrayNode returns a JsonNode.
     *
     * @param transactions -> the transactions of the bank account
     * @param startTimestamp -> the start timestamp
     * @param endTimestamp -> the end timestamp
     * @return an ArrayNode with the filtered transactions
     */
    private ArrayNode filterTransactions(final ArrayNode transactions, final int startTimestamp,
                                        final int endTimestamp) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode filteredTransactions = mapper.createArrayNode();
        for (int i = 0; i < transactions.size(); i++) {
            ObjectNode transaction = (ObjectNode) transactions.get(i);
            int timestamp = transaction.get("timestamp").asInt();
            if (timestamp >= startTimestamp && timestamp <= endTimestamp) {
                filteredTransactions.add(transaction);
            }
        }
        return filteredTransactions;
    }

    /**
     * Method used to create an objectNode with the filtered transactions of a bank account
     * and add it to the output.
     * It calls the filterTransactions method to get the filtered transactions and then
     * creates an objectNode with the command name, the IBAN, the balance of the account,
     * the currency, the filtered transactions and the timestamp and then adds it to the
     * output ArrayNode.
     *
     * @param bankAccount -> the bank account for which we want to print the filtered report
     */
    private void printReport(final BankAccount bankAccount) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        ObjectNode outputNode = mapper.createObjectNode();

        objectNode.put("command", "report");

        outputNode.put("IBAN", bankAccount.getIban());
        outputNode.put("balance", bankAccount.getBalance());
        outputNode.put("currency", bankAccount.getCurrency());

        ArrayNode filteredTransactions = filterTransactions(bankAccount.getTransactions(),
                getStartTimestamp(), getEndTimestamp());
        outputNode.set("transactions", filteredTransactions);

        objectNode.set("output", outputNode);
        objectNode.put("timestamp", getTimestamp());

        output.add(objectNode);
    }
}
