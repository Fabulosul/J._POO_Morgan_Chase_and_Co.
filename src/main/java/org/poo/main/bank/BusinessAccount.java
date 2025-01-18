package org.poo.main.bank;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.businessusers.BusinessUser;
import org.poo.main.businessusers.Employee;
import org.poo.main.businessusers.Manager;
import org.poo.main.businessusers.Owner;
import org.poo.main.cashback.Commerciant;

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
    List<Commerciant> commerciants;
    List<Transaction> businessTransactions;

    public BusinessAccount(String currency, User user, Bank bank) {
        super(currency);
        setAccountType(AccountType.BUSINESS);
        String name = user.getLastName() + " " + user.getFirstName();
        this.owner = new Owner(name, this);
        this.managers = new ArrayList<>();
        this.employees = new ArrayList<>();
        this.spendingLimit =
                bank.convertCurrency(500, "RON", currency);
        this.depositLimit =
                bank.convertCurrency(500, "RON", currency);
        this.businessUsers = new HashMap<>();
        businessUsers.put(name, owner);
        this.businessTransactions = new ArrayList<>();
    }

    public UserRole getUserRole(User user) {
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

    public BusinessUser getBusinessUserByName(String username) {
        return businessUsers.get(username);
    }


    public boolean changeDepositLimit(User user, double newLimit) {
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

    public void addBusinessTransaction(final Transaction transaction) {
        businessTransactions.add(transaction);
    }

}
