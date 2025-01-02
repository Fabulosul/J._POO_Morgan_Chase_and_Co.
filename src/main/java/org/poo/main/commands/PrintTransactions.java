package org.poo.main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bank.User;

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

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("command", "printTransactions");
        objectNode.set("output", user.getTransactionsReport().deepCopy());
        objectNode.put("timestamp", getTimestamp());

        output.add(objectNode);
    }
}
