package org.poo.main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bankaccounts.BankAccount;
import org.poo.main.bankaccounts.BusinessAccount;
import org.poo.main.user.User;
import org.poo.main.businessusers.BusinessUser;

@Getter
@Setter
public final class ChangeSpendingLimit extends Command implements CommandInterface {
    private Bank bank;
    private ArrayNode output;

    public ChangeSpendingLimit(final Bank bank, final CommandInput command,
                               final ArrayNode output) {
        super(command);
        this.bank = bank;
        this.output = output;
    }

    /**
     * Method that changes the spending limit of a business account.
     * If the user is not the owner of the business account, an error message
     * is added to the output.
     * If the account is not a business account, an error message is added to the output.
     * If all conditions are met, the spending limit is changed by calling
     * the changeSpendingLimit method from the BusinessUser class.
     *
     * @see BusinessUser#changeSpendingLimit(double)
     */
    @Override
    public void execute() {
        User user = bank.getUserByMail(getEmail());
        if (user == null) {
            return;
        }
        BankAccount businessAccount = bank.findAccountByIban(getAccount());
        if (businessAccount == null) {
            return;
        }
        if (!businessAccount.getAccountType().equals("business")) {
            addErrorToOutput("This is not a business account.");
            return;
        }
        String username = user.getLastName() + " " + user.getFirstName();
        BusinessUser businessUser = ((BusinessAccount) businessAccount)
                .getBusinessUserByName(username);
        if (!businessUser.changeSpendingLimit(getAmount())) {
            addErrorToOutput("You must be owner in order to change spending limit.");
        }
    }

    /**
     * Method that adds an error message to the output based on the description
     * given as a parameter.
     *
     * @param description the description of the error
     */
    public void addErrorToOutput(final String description) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();

        objectNode.put("command", "changeSpendingLimit");

        ObjectNode outputNode = mapper.createObjectNode();
        outputNode.put("description", description);
        outputNode.put("timestamp", getTimestamp());

        objectNode.set("output", outputNode);
        objectNode.put("timestamp", getTimestamp());

        output.add(objectNode);
    }
}
