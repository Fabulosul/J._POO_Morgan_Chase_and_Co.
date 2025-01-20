package org.poo.main.bank;

import lombok.Getter;
import lombok.Setter;
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
    private final Owner owner;
    private List<Manager> managers;
    private List<Employee> employees;
    public enum UserRole {
        OWNER,
        MANAGER,
        EMPLOYEE
    }
    private double spendingLimit;
    private double depositLimit;
    private Map<String, BusinessUser> businessUsers;
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

    public final BusinessUser getBusinessUserByName(final String username) {
        return businessUsers.get(username);
    }


    public final boolean changeDepositLimit(final User user, final double newLimit) {
        if (getUserRole(user) == UserRole.OWNER) {
            setDepositLimit(newLimit);
            return true;
        }
        return false;
    }

    @Override
    public void addCard(final Card card, final User user) {
        super.addCard(card, user);
        String username = user.getLastName() + " " + user.getFirstName();
        BusinessUser businessUser = getBusinessUserByName(username);
        businessUser.addCard(card);
    }

    @Override
    public void removeCard(final Card card, final User user) {
        super.removeCard(card, user);
        String username = user.getLastName() + " " + user.getFirstName();
        BusinessUser businessUser = getBusinessUserByName(username);
        businessUser.removeCard(card);
    }

    public final void addBusinessTransaction(final Transaction transaction) {
        businessTransactions.add(transaction);
    }

    public final boolean isBusinessUser(final User user) {
        return getUserRole(user) != null;
    }

}
