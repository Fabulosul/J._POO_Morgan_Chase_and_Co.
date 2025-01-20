package org.poo.main.bankaccounts;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.bank.Bank;
import org.poo.main.transaction.Transaction;
import org.poo.main.user.User;
import org.poo.main.businessusers.BusinessUser;
import org.poo.main.businessusers.Employee;
import org.poo.main.businessusers.Manager;
import org.poo.main.businessusers.Owner;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class BusinessAccount extends BankAccount {
    // The owner of the business account
    private final Owner owner;
    // The assigned managers of the business account
    private List<Manager> managers;
    // The assigned employees of the business account
    private List<Employee> employees;
    public enum UserRole {
        OWNER,
        MANAGER,
        EMPLOYEE
    }
    // The spending limit for the employees
    private double spendingLimit;
    // The deposit limit for the employees
    private double depositLimit;
    // A map containing all the users associated with the business account
    private Map<String, BusinessUser> businessUsers;
    // A list containing all the transactions made by the business account
    private List<Transaction> businessTransactions;
    public static final int INITIAL_SPENDING_LIMIT = 500;
    public static final int INITIAL_DEPOSIT_LIMIT = 500;

    public BusinessAccount(final Bank bank, final String currency, final User user) {
        super(bank, currency);
        setAccountType(AccountType.BUSINESS);
        String name = user.getLastName() + " " + user.getFirstName();
        this.owner = new Owner(name, this);
        this.managers = new ArrayList<>();
        this.employees = new ArrayList<>();
        this.spendingLimit =
                bank.convertCurrency(INITIAL_SPENDING_LIMIT, "RON", currency);
        this.depositLimit =
                bank.convertCurrency(INITIAL_DEPOSIT_LIMIT, "RON", currency);
        this.businessUsers = new HashMap<>();
        businessUsers.put(name, owner);
        this.businessTransactions = new ArrayList<>();
    }

    /**
     * Method used to get the role of a user associated with the business account.
     * It iterates through the owner, managers and employees lists and checks
     * if the user is present in any of them.
     *
     * @param user -> the user for which we want to get the role
     * @return the role of the user or null if the user is not associated with the business account
     */
    public final UserRole getUserRole(final User user) {
        String username = user.getLastName() + " " + user.getFirstName();
        if (owner.getUsername().equals(username)) {
            return UserRole.OWNER;
        }
        for (Manager manager : managers) {
            if (manager.getUsername().equals(username)) {
                return UserRole.MANAGER;
            }
        }
        for (Employee employee : employees) {
            if (employee.getUsername().equals(username)) {
                return UserRole.EMPLOYEE;
            }
        }
        return null;
    }

    /**
     * Helper method used to find a business user by its username.
     * It gets the user from the businessUsers map.
     *
     * @param username -> the username of the user
     * @return the business user with the given username or null if the user is not found
     */
    public final BusinessUser getBusinessUserByName(final String username) {
        return businessUsers.get(username);
    }

    /**
     * Method used to add a new business transaction to the business account.
     * It adds the transaction to the businessTransactions list.
     *
     * @param transaction -> the transaction to be added
     */
    public final void addBusinessTransaction(final Transaction transaction) {
        businessTransactions.add(transaction);
    }

    /**
     * Method used check if the user is associated with the business account.
     *
     * @param user -> the user to be checked
     * @return true if the user is associated with the business account, false otherwise
     */
    public final boolean isBusinessUser(final User user) {
        return getUserRole(user) != null;
    }

}
