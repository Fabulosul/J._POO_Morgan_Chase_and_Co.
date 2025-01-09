package org.poo.main.businessusers;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.bank.BusinessAccount;
import org.poo.main.bank.Card;
import org.poo.main.bank.User;

@Getter
@Setter
public class Owner extends BusinessUser {
    public Owner(String name, BusinessAccount businessAccount) {
        super(name, businessAccount);
    }

    @Override
    public boolean addNewAssociate(User user, String role) {
        String username = user.getLastName() + " " + user.getFirstName();
        BusinessAccount businessAccount = getBusinessAccount();
        if (role.equals("manager")) {
            Manager manager = new Manager(username, getBusinessAccount());
            businessAccount.getManagers().add(manager);
            businessAccount.getBusinessUsers().put(username, manager);
        } else {
            Employee employee = new Employee(username, getBusinessAccount());
            businessAccount.getEmployees().add(employee);
            businessAccount.getBusinessUsers().put(username, employee);
        }
        return true;
    }

    @Override
    public boolean changeSpendingLimit(double newLimit) {
        BusinessAccount businessAccount = getBusinessAccount();
        businessAccount.setSpendingLimit(newLimit);
        return true;
    }

    @Override
    public boolean changeDepositLimit(double newLimit) {
        BusinessAccount businessAccount = getBusinessAccount();
        businessAccount.setDepositLimit(newLimit);
        return true;
    }
}
