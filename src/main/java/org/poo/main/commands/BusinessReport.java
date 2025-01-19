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
import org.poo.main.businessusers.Employee;
import org.poo.main.businessusers.Manager;
import org.poo.main.cashback.Commerciant;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BusinessReport extends Command implements CommandInterface {
    private Bank bank;
    private ArrayNode output;

    public BusinessReport(final Bank bank, CommandInput command, ArrayNode output) {
        super(command);
        this.bank = bank;
        this.output = output;
    }

    @Override
    public void execute() {
        User user = bank.getUserByAccount(getAccount());
        if (user == null) {
            return;
        }
        BankAccount bankAccount = user.getAccountByIban(getAccount());
        if (bankAccount == null) {
            return;
        }
        if (!bankAccount.getAccountType().equals("business")) {
            return;
        }
        ObjectNode report = getReport((BusinessAccount) bankAccount);
        output.add(report);
    }

    public ObjectNode getReport(BusinessAccount businessAccount) {
        if(getType().equals("transaction")) {
            return getTransactionReport(businessAccount);
        } else {
            return getCommerciantReport(businessAccount);
        }
    }

    public ObjectNode getTransactionReport(BusinessAccount businessAccount) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode report = mapper.createObjectNode();
        report.put("command", "businessReport");

        ObjectNode output = mapper.createObjectNode();
        output.put("IBAN", getAccount());
        output.put("balance", businessAccount.getBalance());
        output.put("currency", businessAccount.getCurrency());
        output.put("spending limit", businessAccount.getSpendingLimit());
        output.put("deposit limit", businessAccount.getDepositLimit());

        ArrayNode managers = mapper.createArrayNode();
        List<Manager> managersList = businessAccount.getManagers();
        if (managersList == null) {
            return report;
        }
        double totalSpent = 0;
        double totalDeposited = 0;
        for (int i = 0; i < managersList.size(); i++) {
            ObjectNode manager = mapper.createObjectNode();
            manager.put("username", businessAccount.getManagers().get(i).getUsername());
            double amountSpent =
                    getAmountSpent(businessAccount, businessAccount.getManagers().get(i).getUsername());
            manager.put("spent", amountSpent);
            double amountDeposited =
                    getAmountDeposited(businessAccount, businessAccount.getManagers().get(i).getUsername());
            manager.put("deposited", amountDeposited);
            managers.add(manager);
            totalSpent += amountSpent;
            totalDeposited += amountDeposited;
        }
        output.set("managers", managers);

        ArrayNode employees = mapper.createArrayNode();
        List<Employee> employeesList = businessAccount.getEmployees();
        if (employeesList == null) {
            return report;
        }
        for (int i = 0; i < employeesList.size(); i++) {
            ObjectNode employee = mapper.createObjectNode();
            employee.put("username", businessAccount.getEmployees().get(i).getUsername());
            double amountSpent =
                    getAmountSpent(businessAccount, businessAccount.getEmployees().get(i).getUsername());
            employee.put("spent", amountSpent);
            double amountDeposited =
                    getAmountDeposited(businessAccount, businessAccount.getEmployees().get(i).getUsername());
            employee.put("deposited", amountDeposited);
            employees.add(employee);
            totalSpent += amountSpent;
            totalDeposited += amountDeposited;
        }
        output.set("employees", employees);
        output.put("total spent", totalSpent);
        output.put("total deposited", totalDeposited);
        output.put("statistics type", "transaction");

        report.set("output", output);
        report.put("timestamp", getTimestamp());
        return report;
    }

    public ObjectNode getCommerciantReport(BusinessAccount businessAccount) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode report = mapper.createObjectNode();
        report.put("command", "businessReport");

        ObjectNode outputNode = mapper.createObjectNode();
        outputNode.put("IBAN", getAccount());
        outputNode.put("balance", businessAccount.getBalance());
        outputNode.put("currency", businessAccount.getCurrency());
        outputNode.put("spending limit", businessAccount.getSpendingLimit());
        outputNode.put("deposit limit", businessAccount.getDepositLimit());

        ArrayNode commerciantsArray = mapper.createArrayNode();
        List<Commerciant> commerciants = businessAccount.getCommerciants();
        if (commerciants == null) {
            return report;
        }

        commerciants.sort((c1, c2) -> {
            if (c1.getName() == null) return -1;
            if (c2.getName() == null) return 1;
            return c1.getName().compareToIgnoreCase(c2.getName());
        });

        for (int i = 0; i < commerciants.size(); i++) {
            ObjectNode commerciant = mapper.createObjectNode();
            commerciant.put("commerciant", commerciants.get(i).getName());

            List<String> managers = new ArrayList<>();
            List<String> employees =  new ArrayList<>();

            for (int j = 0; j < commerciants.get(i).getUsers().size(); j++) {
               User user = commerciants.get(i).getUsers().get(j);
               String username = user.getLastName() + " " + user.getFirstName();
               BusinessAccount.UserRole userRole = businessAccount.getUserRole(user);
                if (userRole == BusinessAccount.UserRole.MANAGER) {
                    managers.add(username);
                } else {
                    employees.add(username);
                }
            }

            ArrayNode managersArray = mapper.createArrayNode();
            for (String manager : managers) {
                managersArray.add(manager);
            }

            commerciant.set("managers", managersArray);

            ArrayNode employeesArray = mapper.createArrayNode();
            for (String employee : employees) {
                employeesArray.add(employee);
            }

            commerciant.set("employees", employeesArray);

            commerciant.put("total received", commerciants.get(i).getAmountSpent());
            if(commerciants.get(i).getAmountSpent() > 0) {
                commerciantsArray.add(commerciant);
            }
        }
        outputNode.set("commerciants", commerciantsArray);

        outputNode.put("statistics type", "commerciant");

        report.set("output", outputNode);
        report.put("timestamp", getTimestamp());

        return report;
    }

    public double getAmountSpent(BusinessAccount businessAccount, String username) {
        double amountSpent = 0;
        for (int i = 0; i < businessAccount.getBusinessTransactions().size(); i++) {
            if (businessAccount.getBusinessTransactions().get(i).getUsername().equals(username)
                    && businessAccount.getBusinessTransactions().get(i).getDescription().equals("spend")) {
                amountSpent += businessAccount.getBusinessTransactions().get(i).getRawAmount();
            }
        }
        return amountSpent;
    }

    public double getAmountDeposited(BusinessAccount businessAccount, String username) {
        double amountDeposited = 0;
        for (int i = 0; i < businessAccount.getBusinessTransactions().size(); i++) {
            if (businessAccount.getBusinessTransactions().get(i).getUsername().equals(username)
                    && businessAccount.getBusinessTransactions().get(i).getDescription().equals("deposit")) {
                amountDeposited += businessAccount.getBusinessTransactions().get(i).getRawAmount();
            }
        }
        return amountDeposited;
    }


}
