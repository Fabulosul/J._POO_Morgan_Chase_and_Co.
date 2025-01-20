package org.poo.main.businessusers;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.bankaccounts.BusinessAccount;
import org.poo.main.user.User;

@Getter
@Setter
public final class Owner extends BusinessUser {
    public Owner(final String name, final BusinessAccount businessAccount) {
        super(name, businessAccount);
    }

    @Override
    public void addNewAssociate(final User user, final String role) {
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
    }

    @Override
    public boolean changeSpendingLimit(final double newLimit) {
        BusinessAccount businessAccount = getBusinessAccount();
        businessAccount.setSpendingLimit(newLimit);
        return true;
    }

    @Override
    public boolean changeDepositLimit(final double newLimit) {
        BusinessAccount businessAccount = getBusinessAccount();
        businessAccount.setDepositLimit(newLimit);
        return true;
    }
}
