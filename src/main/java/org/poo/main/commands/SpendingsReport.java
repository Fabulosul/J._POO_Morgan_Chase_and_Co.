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

import java.util.Map;
import java.util.TreeMap;

@Getter
@Setter
public final class SpendingsReport extends Command implements CommandInterface {
    private Bank bank;
    private ArrayNode output;
    // Map used to store the amount spent at each commerciant
    // and to be able to print the commerciants alphabetically
    private Map<String, Double> commerciantSpendingMap;

    public SpendingsReport(final Bank bank, final CommandInput command,
                           final ArrayNode output) {
        super(command);
        this.bank = bank;
        this.output = output;
        // The tree map stores the keys alphabetically
        this.commerciantSpendingMap = new TreeMap<>();
    }

    /**
     * Method overridden from CommandInterface to add a spendings report of a bank account
     * to the output ArrayNode.
     * It gets the user and the bank account by the account number, then checks if the bank account
     * is a savings account. If it is, it adds an error message to the output. If it is not,
     * it calls the printSpendingsReport method to do the actual work.
     * If user who owns the account is not found, it adds an error message to the output indicating
     * that the account was not found.
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
            addErrorToOutput("This kind of report is not supported for a saving account");
            return;
        }

        printSpendingsReport(bankAccount);
    }

    /**
     * Method used to filter the card transactions of a bank account between two timestamps.
     * It creates a new ArrayNode called cardTransactions and iterates through the transactions
     * of the current bank account. Then, for each transaction, gets the objectNode and
     * the timestamp associated with it. If the timestamp is between the startTimestamp and
     * the endTimestamp and the transactions corresponds to a card payment, it adds the transaction
     * to the cardTransactions ArrayNode and calls the addCommerciantSpending method which increases
     * the amount spent at the commerciant by the amount of the transaction or creates a new entry
     * in the commerciantSpendingMap.
     *
     * @implNote We use asInt() to convert the timestamp to an integer because the get method
     * in the ArrayNode returns a JsonNode and we do the same principle for the commerciant
     * when we use asText() to convert the commerciant to a string and for the amount when we use
     * asDouble() to convert the amount to a double.
     *
     * @param transactions -> the transactions of the bank account
     * @param startTimestamp -> the start timestamp of the interval
     * @param endTimestamp -> the end timestamp of the interval
     * @return the card transactions between the two timestamps
     */
    private ArrayNode filterCardTransactions(final ArrayNode transactions, final int startTimestamp,
                                            final int endTimestamp) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode cardTransactions = mapper.createArrayNode();
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).get("description").asText().equals("Card payment")
                    && transactions.get(i).get("timestamp").asInt() >= startTimestamp
                    && transactions.get(i).get("timestamp").asInt() <= endTimestamp) {
                cardTransactions.add(transactions.get(i));
                addCommerciantSpending(transactions.get(i).get("commerciant").asText(),
                        transactions.get(i).get("amount").asDouble());
            }
        }
        return cardTransactions;
    }

    /**
     * Method used to add the amount spent at a commerciant to the commerciantSpendingMap.
     * If the commerciant is already in the map, it gets the current amount spent at the commerciant
     * and adds the amount of the transaction to it. If the commerciant is not in the map,
     * it creates a new entry with the commerciant and the amount of the transaction.
     *
     * @param commerciant -> the commerciant name
     * @param amount -> the amount spent at the commerciant during the current transaction
     */
    private void addCommerciantSpending(final String commerciant, final double amount) {
        if (commerciantSpendingMap.containsKey(commerciant)) {
            double currentAmount = commerciantSpendingMap.get(commerciant);
            double totalAmount = currentAmount + amount;
            commerciantSpendingMap.put(commerciant, totalAmount);
        } else {
            commerciantSpendingMap.put(commerciant, amount);
        }
    }

    /**
     * Method used to retrieve the amount spent at a commerciant.
     * It simply calls the get method on the commerciantSpendingMap
     * with the commerciant as a key.
     *
     * @param commerciant -> the name of the commerciant
     * @return the amount spent at the commerciant given as parameter
     */
    private double getCommerciantSpending(final String commerciant) {
        return commerciantSpendingMap.get(commerciant);
    }

    /**
     * Method used to add an error message to the output when the
     * spendingsReport command is called for a saving account or when the
     * account for which the report is requested is not found.
     *
     * @param error -> the error message to be added to the output
     */
    private void addErrorToOutput(final String error) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        if (error.equals("Account not found")) {
            objectNode.put("command", "spendingsReport");

            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("description", "Account not found");
            outputNode.put("timestamp", getTimestamp());

            objectNode.set("output", outputNode);
        } else {
            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("error",
                    "This kind of report is not supported for a saving account");
            objectNode.put("command", "spendingsReport");
            objectNode.set("output", outputNode);
        }
        objectNode.put("timestamp", getTimestamp());
        output.add(objectNode);
    }

    /**
     * Method used to add a spendings report of a classic bank account to the
     * output ArrayNode.
     * It creates an objectNode with the command name, the IBAN, the balance, the currency,
     * the transactions between the startTimestamp and the endTimestamp and a list of commerciants
     * sorted alphabetically with the total amount spent at each of them and adds it to the output.
     *
     * @param bankAccount -> the bank account for which the report is requested
     */
    private void printSpendingsReport(final BankAccount bankAccount) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        ObjectNode outputNode = mapper.createObjectNode();

        objectNode.put("command", "spendingsReport");

        outputNode.put("IBAN", bankAccount.getIban());
        outputNode.put("balance", bankAccount.getBalance());
        outputNode.put("currency", bankAccount.getCurrency());

        ArrayNode filteredCardTransactions = filterCardTransactions(bankAccount.getTransactions(),
                getStartTimestamp(), getEndTimestamp());
        outputNode.set("transactions", filteredCardTransactions);

        ArrayNode commerciantsArray = mapper.createArrayNode();

        for (String commerciant : getCommerciantSpendingMap().keySet()) {
            ObjectNode commerciantNode = mapper.createObjectNode();
            commerciantNode.put("commerciant", commerciant);
            commerciantNode.put("total", getCommerciantSpending(commerciant));
            commerciantsArray.add(commerciantNode);
        }

        outputNode.set("commerciants", commerciantsArray);

        objectNode.set("output", outputNode);
        objectNode.put("timestamp", getTimestamp());

        output.add(objectNode);
    }
}
