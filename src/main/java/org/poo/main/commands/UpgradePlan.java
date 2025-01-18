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
public class UpgradePlan extends Command implements CommandInterface {
    private Bank bank;
    private ArrayNode output;
    private Map<String, Map<String, Double>> upgradePlanFees;
    private final String feeCurrency = "RON";

    public UpgradePlan(Bank bank, CommandInput command, ArrayNode output) {
        super(command);
        this.bank = bank;
        this.output = output;
        this.upgradePlanFees = new HashMap<>();

        Map<String, Double> studentPlanFees = new HashMap<>();
        studentPlanFees.put("silver", 100.0);
        studentPlanFees.put("gold", 350.0);
        upgradePlanFees.put("student", studentPlanFees);

        Map<String, Double> standardUserPlanFees = new HashMap<>();
        standardUserPlanFees.put("silver", 100.0);
        standardUserPlanFees.put("gold", 350.0);
        upgradePlanFees.put("standard", standardUserPlanFees);

        Map<String, Double> silverUserPlanFees = new HashMap<>();
        silverUserPlanFees.put("gold", 250.0);
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
            registerTransactionError(user, "Account not found");
            return;
        }
        if(user.getServicePlan().getPlanName().equals(getNewPlanType())) {
            registerTransactionError(user, "The user already has the " +
                    user.getServicePlan().getPlanName() + " plan.");
            return;
        }
        if(!user.getServicePlan().canUpgradePlan(getNewPlanType())) {
            registerTransactionError(user, "You cannot downgrade your plan.");
            return;
        }

        double upgradeFee = getUpgradeFee(user.getServicePlan().getPlanName(), getNewPlanType());
        if(bankAccount.hasSufficientFunds(upgradeFee, feeCurrency, bank)) {
            double convertedFee = bank.convertCurrency(upgradeFee, feeCurrency, bankAccount.getCurrency());
            bankAccount.deductMoney(convertedFee);
            user.changeServicePlan(getNewPlanType());
            Transaction transaction = new Transaction
                    .TransactionBuilder(getTimestamp(), "Upgrade plan")
                    .accountIban(bankAccount.getIban())
                    .newPlanType(getNewPlanType())
                    .build();
            user.addTransaction(transaction);
        } else {
            registerTransactionError(user, "Insufficient funds");
        }

    }

    public double getUpgradeFee(String oldPlanName, String newPlanName) {
        return upgradePlanFees.get(oldPlanName).get(newPlanName);
    }

    public void registerTransactionError(User user, String message) {
        Transaction transaction = new Transaction
                .TransactionBuilder(getTimestamp(), message)
                .build();
        user.addTransaction(transaction);
    }

}