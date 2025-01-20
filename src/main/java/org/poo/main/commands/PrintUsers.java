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


@Getter
@Setter
public final class PrintUsers extends Command implements CommandInterface {
    private Bank bank;
    private ArrayNode output;

    public PrintUsers(final Bank bank, final CommandInput command,
                      final ArrayNode output) {
        super(command);
        this.bank = bank;
        this.output = output;
    }

    /**
     * Method overridden from the CommandInterface to add all information about the users
     * to the output ArrayNode.
     * It uses a for loop to iterate through all the users, their bank accounts and cards and
     * adds all the data related (firstName, lastName, email, IBAN, balance, currency,
     * type, cardNumber, status and timestamp) to the output.
     */
    @Override
    public void execute() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        ArrayNode usersArray = mapper.createArrayNode();

        objectNode.put("command", "printUsers");

        for (User user : bank.getUsers()) {
            ObjectNode userNode = mapper.createObjectNode();

            userNode.put("firstName", user.getFirstName());
            userNode.put("lastName", user.getLastName());
            userNode.put("email", user.getEmail());

            ArrayNode accountsArray = mapper.createArrayNode();
            for (BankAccount bankAccount : user.getBankAccounts()) {
                ObjectNode bankAccountNode = mapper.createObjectNode();
                bankAccountNode.put("IBAN", bankAccount.getIban());
                bankAccountNode.put("balance", bankAccount.getBalance());
                bankAccountNode.put("currency", bankAccount.getCurrency());
                bankAccountNode.put("type", bankAccount.getAccountType());

                ArrayNode cardsArray = mapper.createArrayNode();
                for (Card card : bankAccount.getCards()) {
                    ObjectNode cardNode = mapper.createObjectNode();
                    cardNode.put("cardNumber", card.getCardNumber());
                    cardNode.put("status", card.getStatus());
                    cardsArray.add(cardNode);
                }

                bankAccountNode.set("cards", cardsArray);

                accountsArray.add(bankAccountNode);
            }
            userNode.set("accounts", accountsArray);

            usersArray.add(userNode);
        }
        objectNode.set("output", usersArray);
        objectNode.put("timestamp",  getTimestamp());

        output.add(objectNode);
    }
}
