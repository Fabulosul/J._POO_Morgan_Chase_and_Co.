package org.poo.main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bank.BankAccount;
import org.poo.main.bank.BusinessAccount;
import org.poo.main.bank.User;
import org.poo.main.businessusers.BusinessUser;
import org.poo.main.businessusers.Owner;

@Getter
@Setter
public class ChangeSpendingLimit extends Command implements CommandInterface {
    private Bank bank;
    private ArrayNode output;

    public ChangeSpendingLimit(final Bank bank, final CommandInput command, final ArrayNode output) {
        super(command);
        this.bank = bank;
        this.output = output;
    }

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
        if(!businessAccount.getAccountType().equals("business")) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode objectNode = mapper.createObjectNode();

            objectNode.put("command", "changeSpendingLimit");

            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("description", "This is not a business account.");
            outputNode.put("timestamp", getTimestamp());

            objectNode.set("output", outputNode);
            objectNode.put("timestamp", getTimestamp());

            output.add(objectNode);
            return;
        }
        String username = user.getLastName() + " " + user.getFirstName();
        BusinessUser businessUser = ((BusinessAccount) businessAccount).getBusinessUserByName(username);
        if(!businessUser.changeSpendingLimit(getAmount())) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode objectNode = mapper.createObjectNode();

            objectNode.put("command", "changeSpendingLimit");

            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("description", "You must be owner in order to change spending limit.");
            outputNode.put("timestamp", getTimestamp());

            objectNode.set("output", outputNode);
            objectNode.put("timestamp", getTimestamp());

            output.add(objectNode);
        }

    }
}
