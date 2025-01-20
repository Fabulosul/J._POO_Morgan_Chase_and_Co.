package org.poo.main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bank.BankAccount;
import org.poo.main.bank.Transaction;
import org.poo.main.bank.User;

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

    @Override
    public void execute() {
        User user = bank.getUserByAccount(getAccount());
        if (user == null) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode objectNode = mapper.createObjectNode();
            objectNode.put("command", "upgradePlan");

            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("description", "Account not found");
            outputNode.put("timestamp", getTimestamp());

            objectNode.set("output", outputNode);
            objectNode.put("timestamp", getTimestamp());

            output.add(objectNode);
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

    public double getUpgradeFee(final String oldPlanName, final String newPlanName) {
        return upgradePlanFees.get(oldPlanName).get(newPlanName);
    }

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
