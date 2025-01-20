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
import org.poo.main.businessusers.BusinessUser;
import org.poo.main.transaction.Transaction;
import org.poo.main.user.User;
import org.poo.main.businessusers.Employee;
import org.poo.main.businessusers.Manager;
import org.poo.main.cashback.Commerciant;

import java.util.List;

@Getter
@Setter
public final class BusinessReport extends Command implements CommandInterface {
    private Bank bank;
    private ArrayNode output;

    public BusinessReport(final Bank bank, final CommandInput command, final ArrayNode output) {
        super(command);
        this.bank = bank;
        this.output = output;
    }

    /**
     * Method used to get a business report depending on the type of the report
     * by calling the getReport method.
     */
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

    /**
     * Method used to get a business report depending on the type of the report.
     * It calls the getTransactionReport method if the type is "transaction" and
     * the getCommerciantReport method if the type is "commerciant".
     *
     * @param businessAccount the business account for which the report is generated
     * @return the business report
     */
    public ObjectNode getReport(final BusinessAccount businessAccount) {
        if (getType().equals("transaction")) {
            return getTransactionReport(businessAccount);
        } else {
            return getCommerciantReport(businessAccount);
        }
    }

    /**
     * Helper method used to get a transaction report for a business account.
     * It creates an object node and adds the details of the business account
     * by calling the addReportDetails method. It then processes the managers
     * and employees lists by calling the processUserArray method and adds
     * the total spent and total deposited amounts by calling the getTotalSpent
     * and getTotalDeposited methods. It sets the statistics type to "transaction"
     * and returns the report.
     *
     * @param businessAccount -> the business account for which the report is generated
     * @return the transaction report
     */
    public ObjectNode getTransactionReport(final BusinessAccount businessAccount) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode report = mapper.createObjectNode();
        report.put("command", "businessReport");

        ObjectNode outputNode = mapper.createObjectNode();
        addReportDetails(businessAccount, outputNode);

        ArrayNode managersArray =
                processBusinessUsers(businessAccount.getManagers(), businessAccount);
        ArrayNode employeesArray =
                processBusinessUsers(businessAccount.getEmployees(), businessAccount);

        outputNode.set("managers", managersArray);
        outputNode.set("employees", employeesArray);
        outputNode.put("total spent", getTotalSpent(businessAccount));
        outputNode.put("total deposited", getTotalDeposited(businessAccount));
        outputNode.put("statistics type", "transaction");

        report.set("output", outputNode);
        report.put("timestamp", getTimestamp());

        return report;
    }

    /**
     * Helper method that creates a commerciant report for a business account.
     * It creates an object node and adds the details of the business account
     * by calling the addReportDetails method. It first sorts the commerciants
     * list by name and then iterates through the commerciants list and creates
     * an object node for each commerciant. It then processes the managers and
     * employees lists for each commerciant by calling the processUserArray method
     * and adds the total received amount for each commerciant by calling the
     * getAmountSpent method. It sets the statistics type to "commerciant" and
     * returns the report.
     *
     * @param businessAccount -> the business account for which the report is generated
     * @return the commerciant report
     */
    public ObjectNode getCommerciantReport(final BusinessAccount businessAccount) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode report = mapper.createObjectNode();
        report.put("command", "businessReport");

        ObjectNode outputNode = mapper.createObjectNode();
        addReportDetails(businessAccount, outputNode);

        ArrayNode commerciantsArray = mapper.createArrayNode();
        List<Commerciant> commerciants = businessAccount.getCommerciants();
        if (commerciants != null) {
            commerciants.sort((c1, c2) -> {
                if (c1.getName() == null) {
                    return -1;
                }
                if (c2.getName() == null) {
                    return 1;
                }
                return c1.getName().compareToIgnoreCase(c2.getName());
            });

            for (Commerciant commerciant : commerciants) {
                ObjectNode commerciantNode = mapper.createObjectNode();
                commerciantNode.put("commerciant", commerciant.getName());

                ArrayNode managersArray = mapper.createArrayNode();
                ArrayNode employeesArray = mapper.createArrayNode();

                for (User user : commerciant.getUsers()) {
                    String username = user.getLastName() + " " + user.getFirstName();
                    if (businessAccount.getUserRole(user) == BusinessAccount.UserRole.MANAGER) {
                        managersArray.add(username);
                    } else {
                        employeesArray.add(username);
                    }
                }

                commerciantNode.set("managers", managersArray);
                commerciantNode.set("employees", employeesArray);
                commerciantNode.put("total received", commerciant.getAmountSpent());

                if (commerciant.getAmountSpent() > 0) {
                    commerciantsArray.add(commerciantNode);
                }
            }
        }

        outputNode.set("commerciants", commerciantsArray);
        outputNode.put("statistics type", "commerciant");

        report.set("output", outputNode);
        report.put("timestamp", getTimestamp());

        return report;
    }

    /**
     * Method used to get the total amount spent by all the managers and employees associated
     * with a business account. It iterates through the managers and employees
     * lists and calls the getAmountSpent method for each user and adds the amount
     * spent to the total spent amount.
     *
     * @param businessAccount -> the business account for which the total spent amount
     *                        is calculated
     * @return the total amount spent by all account users except the owner
     */
    public double getTotalSpent(final BusinessAccount businessAccount) {
        double totalSpent = 0;
        for (Manager manager : businessAccount.getManagers()) {
            totalSpent += getAmountSpent(businessAccount, manager.getUsername());
        }
        for (Employee employee : businessAccount.getEmployees()) {
            totalSpent += getAmountSpent(businessAccount, employee.getUsername());
        }
        return totalSpent;
    }

    /**
     * Method used to get the total amount deposited by all the managers and employees associated
     * with a business account. It iterates through the managers and employees
     * lists and calls the getAmountDeposited method for each user and adds the amount
     * deposited to the total deposited amount.
     *
     * @param businessAccount -> the business account for which the total deposited amount
     *                        is calculated
     * @return the total amount deposited by all account users except the owner
     */
    public double getTotalDeposited(final BusinessAccount businessAccount) {
        double totalDeposited = 0;
        for (Manager manager : businessAccount.getManagers()) {
            totalDeposited += getAmountDeposited(businessAccount, manager.getUsername());
        }
        for (Employee employee : businessAccount.getEmployees()) {
            totalDeposited += getAmountDeposited(businessAccount, employee.getUsername());
        }
        return totalDeposited;
    }

    /**
     * Helper method used to get the amount spent by a user associated with a business account.
     * It iterates through the business transactions list and checks if the transaction
     * has the username given as a parameter and the description "spend". If the conditions
     * are met, it adds the amount of the transaction to the amount spent by the user.
     *
     * @param businessAccount -> the business account for which the calculation is made
     * @param username -> the username of the user for which the amount spent is calculated
     * @return the amount spent by the business user
     */
    public double getAmountSpent(final BusinessAccount businessAccount, final String username) {
        double amountSpent = 0;
        for (int i = 0; i < businessAccount.getBusinessTransactions().size(); i++) {
            Transaction transaction = businessAccount.getBusinessTransactions().get(i);
            if (transaction.getUsername().equals(username)
                    && transaction.getDescription().equals("spend")) {
                amountSpent +=
                        businessAccount.getBusinessTransactions().get(i).getRawAmount();
            }
        }
        return amountSpent;
    }

    /**
     * Helper method used to get the amount deposited by a user associated with a business account.
     * It iterates through the business transactions list and checks if the transaction
     * has the username given as a parameter and the description "deposit". If the conditions
     * are met, it adds the amount of the transaction to the amount deposited by the user.
     *
     * @param businessAccount -> the business account for which the calculation is made
     * @param username -> the username of the user for which the amount deposited is calculated
     * @return the amount deposited by the business user
     */
    public double getAmountDeposited(final BusinessAccount businessAccount, final String username) {
        double amountDeposited = 0;
        for (int i = 0; i < businessAccount.getBusinessTransactions().size(); i++) {
            Transaction transaction = businessAccount.getBusinessTransactions().get(i);
            if (transaction.getUsername().equals(username)
                    && transaction.getDescription().equals("deposit")) {
                amountDeposited += businessAccount.getBusinessTransactions().get(i).getRawAmount();
            }
        }
        return amountDeposited;
    }

    /**
     * Helper method used to add the details of a business account to an object node.
     * It adds the IBAN, balance, currency, spending limit and deposit limit to the
     * object node.
     *
     * @param businessAccount -> the business account for which the details are added
     * @param outputNode -> the object node to which the details are added
     */
    private void addReportDetails(final BusinessAccount businessAccount,
                                  final ObjectNode outputNode) {
        outputNode.put("IBAN", getAccount());
        outputNode.put("balance", businessAccount.getBalance());
        outputNode.put("currency", businessAccount.getCurrency());
        outputNode.put("spending limit", businessAccount.getSpendingLimit());
        outputNode.put("deposit limit", businessAccount.getDepositLimit());
    }

    /**
     * Helper method used to process the managers and employees lists of a business account
     * and add all the information regarding their transactions to an array node.
     * It creates an object node for each user and adds the username, amount spent and
     * amount deposited to the object node, which is then added to the array node.
     *
     * @param users -> the list of users
     * @param businessAccount -> the business account for which the information is processed
     * @return the array node containing the information about the users
     */
    private ArrayNode processBusinessUsers(final List<? extends BusinessUser> users,
                                           final BusinessAccount businessAccount) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode usersArray = mapper.createArrayNode();
        if (users != null) {
            for (BusinessUser user : users) {
                ObjectNode userNode = mapper.createObjectNode();
                String username = user.getUsername();
                userNode.put("username", username);

                double amountSpent = getAmountSpent(businessAccount, username);
                userNode.put("spent", amountSpent);

                double amountDeposited = getAmountDeposited(businessAccount, username);
                userNode.put("deposited", amountDeposited);

                usersArray.add(userNode);
            }
        }
        return usersArray;
    }
}
