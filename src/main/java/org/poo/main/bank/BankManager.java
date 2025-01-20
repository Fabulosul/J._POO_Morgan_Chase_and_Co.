package org.poo.main.bank;

import com.fasterxml.jackson.databind.node.ArrayNode;

import org.poo.fileio.CommandInput;
import org.poo.fileio.ObjectInput;
import org.poo.fileio.UserInput;
import org.poo.main.commands.Command;
import org.poo.main.commands.CommandFactory;
import org.poo.main.commands.CommandHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.poo.fileio.ExchangeInput;
import org.poo.fileio.CommerciantInput;
import org.poo.main.user.User;

public final class BankManager {
    // Private constructor used to prevent instantiation of this utility class
    private BankManager() {
        throw new UnsupportedOperationException("This is a utility class "
                + "and should not be instantiated!");
    }

    /**
     * Static method used to do all the preliminary operations needed to open the bank.
     * It takes all the users, exchange rates and commerciants from the input and creates
     * a bank object. Then, in a for loop, for each command from the input, it creates a
     * command object and executes it.
     *
     * @param input -> the input object
     * @param output -> the ArrayNode object used to store the output
     */
    public static void openBank(final ObjectInput input, final ArrayNode output) {
        List<ExchangeInput> exchangeRates =
                new ArrayList<>(Arrays.asList(input.getExchangeRates()));
        List<CommerciantInput> commerciants = new ArrayList<>();
        List<User> users = new ArrayList<>();

        if (input.getCommerciants() != null) {
            commerciants = Arrays.asList(input.getCommerciants());
        }
        for (UserInput user : input.getUsers()) {
            users.add(new User(user));
        }

        Bank bank = new Bank(users, exchangeRates, commerciants);

        List<CommandInput> commands = new ArrayList<>(Arrays.asList(input.getCommands()));

        CommandHandler commandHandler = new CommandHandler();

        for (CommandInput command : commands) {
            Command newCommand = CommandFactory.createCommand(command, bank, output);
            commandHandler.setCommand(newCommand);
            if (newCommand != null) {
                commandHandler.executeCommand();
            }
        }
    }
}
