package org.poo.main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bankaccounts.BankAccount;
import org.poo.main.transaction.Transaction;
import org.poo.main.user.User;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public final class UpgradePlan extends Command implements CommandInterface {
    private Bank bank;
    private ArrayNode output;
    private Map<String, Map<String, Double>> upgradePlanFees;
    private static final String FEE_CURRENCY = "RON";
    private static final double STANDARD_TO_SILVER_FEE = 100.0;
    private static final double SILVER_TO_GOLD_FEE = 250.0;
    private static final double STANDARD_TO_GOLD_FEE = 350.0;

    public UpgradePlan(final Bank bank, final CommandInput command, final ArrayNode output) {
        super(command);
        this.bank = bank;
        this.output = output;
        this.upgradePlanFees = new HashMap<>();

        Map<String, Double> studentPlanFees = new HashMap<>();
        studentPlanFees.put("silver", STANDARD_TO_SILVER_FEE);
        studentPlanFees.put("gold", STANDARD_TO_GOLD_FEE);
        upgradePlanFees.put("student", studentPlanFees);

        Map<String, Double> standardUserPlanFees = new HashMap<>();
        standardUserPlanFees.put("silver", STANDARD_TO_SILVER_FEE);
        standardUserPlanFees.put("gold", STANDARD_TO_GOLD_FEE);
        upgradePlanFees.put("standard", standardUserPlanFees);

        Map<String, Double> silverUserPlanFees = new HashMap<>();
        silverUserPlanFees.put("gold", SILVER_TO_GOLD_FEE);
        upgradePlanFees.put("silver", silverUserPlanFees);
    }

    /**
     * Method used to upgrade the plan of a user.
     * If the user does not exist, an error is added to the output.
     * If the user already has the new plan, an error is added to the output.
     * If the user plan is higher than the new plan, an error is added to the output.
     * Then, the upgrade fee is calculated and if the user has enough money, the fee is deducted
     * from the account and the user's plan is changed by calling the changeServicePlan method
     * from the User class.
     *
     * @see User#changeServicePlan(String)
     */
    @Override
    public void execute() {
        User user = bank.getUserByAccount(getAccount());
        if (user == null) {
            addErrorToOutput();
            return;
        }
        BankAccount bankAccount = user.getAccountByIban(getAccount());
        if (bankAccount == null) {
            registerTransactionError(null, user, "Account not found");
            return;
        }
        if (user.getServicePlan().getPlanName().equals(getNewPlanType())) {
            registerTransactionError(bankAccount, user, "The user already has the "
                    + user.getServicePlan().getPlanName() + " plan.");
            return;
        }
        if (!user.getServicePlan().canUpgradePlan(getNewPlanType())) {
            registerTransactionError(bankAccount, user, "You cannot downgrade your plan.");
            return;
        }

        double upgradeFee = getUpgradeFee(user.getServicePlan().getPlanName(), getNewPlanType());
        if (bankAccount.hasSufficientFunds(upgradeFee, FEE_CURRENCY, bank)) {
            double convertedFee =
                    bank.convertCurrency(upgradeFee, FEE_CURRENCY, bankAccount.getCurrency());
            bankAccount.deductMoney(convertedFee);
            user.changeServicePlan(getNewPlanType());
            Transaction transaction = new Transaction
                    .TransactionBuilder(getTimestamp(), "Upgrade plan")
                    .accountIban(bankAccount.getIban())
                    .newPlanType(getNewPlanType())
                    .build();
            user.addTransaction(transaction);
            bankAccount.addTransaction(transaction);
        } else {
            registerTransactionError(bankAccount, user, "Insufficient funds");
        }

    }

    /**
     * Helper method used to add an error to the output
     * stating that the account was not found.
     */
    private void addErrorToOutput() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("command", "upgradePlan");

        ObjectNode outputNode = mapper.createObjectNode();
        outputNode.put("description", "Account not found");
        outputNode.put("timestamp", getTimestamp());

        objectNode.set("output", outputNode);
        objectNode.put("timestamp", getTimestamp());

        output.add(objectNode);
    }

    /**
     * Helper method used to get the upgrade fee from the upgradePlanFees map.
     *
     * @param oldPlanName -> the name of the old plan
     * @param newPlanName -> the name of the new plan
     * @return the upgrade fee needed to upgrade the plan
     */
    public double getUpgradeFee(final String oldPlanName, final String newPlanName) {
        return upgradePlanFees.get(oldPlanName).get(newPlanName);
    }

    /**
     * Helper method used to register a transaction error based on the message.
     *
     * @param bankAccount -> the bank account of the user
     * @param user -> the user that wants to upgrade the plan
     * @param message -> the error message
     */
    public void registerTransactionError(final BankAccount bankAccount, final User user,
                                         final String message) {
        Transaction transaction = new Transaction
                .TransactionBuilder(getTimestamp(), message)
                .build();
        user.addTransaction(transaction);
        if (bankAccount != null) {
            bankAccount.addTransaction(transaction);
        }
    }
}
