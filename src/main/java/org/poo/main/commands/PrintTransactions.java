package org.poo.main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bank.User;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public final class PrintTransactions extends Command implements CommandInterface {
    private Bank bank;
    private ArrayNode output;

    public PrintTransactions(final Bank bank, final CommandInput command,
                             final ArrayNode output) {
        super(command);
        this.bank = bank;
        this.output = output;
    }

    /**
     * Method overridden from the CommandInterface to print the transactions of a user.
     * It gets the user by email, adds the necessary information of the command and makes
     * a deep copy of the transactionReport of the user and adds it to the output ArrayNode.
     */
    @Override
    public void execute() {
        User user = bank.getUserByMail(getEmail());

        ArrayNode transactionsReport = user.getTransactionsReport();
        sortTransactionsByTimestamp(transactionsReport);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("command", "printTransactions");
        objectNode.set("output", user.getTransactionsReport().deepCopy());
        objectNode.put("timestamp", getTimestamp());

        output.add(objectNode);
    }

    private void sortTransactionsByTimestamp(final ArrayNode transactionsReport) {
        List<ObjectNode> objectNodeList = new ArrayList<>();
        for (int i = 0; i < transactionsReport.size(); i++) {
            objectNodeList.add((ObjectNode) transactionsReport.get(i));
        }
        objectNodeList.sort((o1, o2) -> {
            int timestamp1 = o1.get("timestamp").asInt();
            int timestamp2 = o2.get("timestamp").asInt();
            return timestamp1 - timestamp2;
        });
        transactionsReport.removeAll();
        for (ObjectNode objectNode : objectNodeList) {
            transactionsReport.add(objectNode);
        }
    }
}
