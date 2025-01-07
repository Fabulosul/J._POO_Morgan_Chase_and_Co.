package org.poo.main.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;

public final class CommandFactory {
    // Private constructor used to prevent instantiation of this utility class.
    private CommandFactory() {
        throw new UnsupportedOperationException("This is a utility class "
                + "and should not be instantiated!");
    }

    /**
     * Static method used to create a command based on the input command.
     * It uses the Factory design pattern to create the command object depending
     * on the command name by calling the appropriate constructor with the necessary parameters.
     *
     * @param command -> the input command
     * @param bank -> the bank instance
     * @param output  -> the output array node
     * @return a command object based on the input command's name
     */
    public static Command createCommand(final CommandInput command, final Bank bank,
                                        final ArrayNode output) {
        return switch (command.getCommand()) {
            case "printUsers" -> new PrintUsers(bank, command, output);
            case "setAlias" -> new SetAlias(bank, command);
            case "addAccount" -> new AddAccount(bank, command);
            case "setMinBalance" -> new SetMinBalance(bank, command);
            case "changeInterestRate" -> new ChangeInterestRate(bank, command, output);
            case "addInterest" -> new AddInterest(bank, command, output);
            case "deleteAccount" -> new DeleteAccount(bank, command, output);
            case "createCard" -> new CreateCard(bank, command, "classic");
            case "createOneTimeCard" -> new CreateCard(bank, command, "oneTime");
            case "checkCardStatus" -> new CheckCardStatus(bank, command, output);
            case "deleteCard" -> new DeleteCard(bank, command);
            case "addFunds" -> new AddFunds(bank, command);
            case "payOnline" -> new PayOnline(bank, command, output);
            case "sendMoney" -> new SendMoney(bank, command, output);
            case "splitPayment" -> new SplitPayment(bank, command);
            case "printTransactions" -> new PrintTransactions(bank, command, output);
            case "report" -> new Report(bank, command, output);
            case "spendingsReport" -> new SpendingsReport(bank, command, output);
            case "withdrawSavings" -> new WithdrawSavings(bank, command, output);
            case "upgradePlan" -> new UpgradePlan(bank, command, output);
            case "cashWithdrawal" -> new CashWithdrawal(bank, command, output);
            case "acceptSplitPayment" -> new AcceptSplitPayment(bank, command, output);
            case "rejectSplitPayment" -> new RejectSplitPayment(bank, command, output);
            default -> null;
        };
    }
}
