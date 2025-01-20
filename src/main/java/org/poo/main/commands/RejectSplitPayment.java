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
public final class RejectSplitPayment extends Command implements CommandInterface {
    private Bank bank;
    private ArrayNode output;

    public RejectSplitPayment(final Bank bank, final CommandInput command,
                              final ArrayNode output) {
        super(command);
        this.bank = bank;
        this.output = output;
    }

    /**
     * Method used to reject a split payment.
     * If the user is not found, an error message is added to the output.
     * If the user is found, the split payment is rejected by calling the
     * rejectSplitPayment method from the bank class.
     *
     * @see Bank#rejectSplitPayment(User, String) for more information.
     */
    @Override
    public void execute() {
        User user = bank.getUserByMail(getEmail());
        if (user == null) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode objectNode = mapper.createObjectNode();
            objectNode.put("command", "rejectSplitPayment");

            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("description", "User not found");
            outputNode.put("timestamp", getTimestamp());

            objectNode.set("output", outputNode);
            objectNode.put("timestamp", getTimestamp());

            output.add(objectNode);
            return;
        }
        bank.rejectSplitPayment(user, getSplitPaymentType());
    }
}
