package org.poo.main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.user.User;

@Getter
@Setter
public final class AcceptSplitPayment extends Command implements CommandInterface {
    private Bank bank;
    private ArrayNode output;

    public AcceptSplitPayment(final Bank bank, final CommandInput command,
                              final ArrayNode output) {
        super(command);
        this.bank = bank;
        this.output = output;
    }

    /**
     * Method that accepts a split payment.
     * It gets the user by email and accepts the split payment by calling the
     * acceptSplitPayment method from the bank class.
     * If the user is not found, it will return an error message.
     *
     * @see Bank#acceptSplitPayment(User, String) for more information.
     */
    @Override
    public void execute() {
        User user = bank.getUserByMail(getEmail());
        if (user == null) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode objectNode = mapper.createObjectNode();
            objectNode.put("command", "acceptSplitPayment");

            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("description", "User not found");
            outputNode.put("timestamp", getTimestamp());

            objectNode.set("output", outputNode);
            objectNode.put("timestamp", getTimestamp());

            output.add(objectNode);
            return;
        }
        bank.acceptSplitPayment(user, getSplitPaymentType());
    }
}
