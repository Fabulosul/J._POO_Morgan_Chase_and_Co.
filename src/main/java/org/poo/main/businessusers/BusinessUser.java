package org.poo.main.businessusers;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.bankaccounts.BusinessAccount;
import org.poo.main.user.User;

@Getter
@Setter
public abstract class BusinessUser {
    private String username;
    private String email;
    private final BusinessAccount businessAccount;

    public BusinessUser(final String username, final BusinessAccount businessAccount) {
        this.username = username;
        this.businessAccount = businessAccount;
    }

    /**
     * Method overriden by the subclasses to add a new associate to the business account
     * depending on its role(manager/employee).
     *
     * @param user - the user to be added
     * @param role - the role of the user
     */
    public abstract void addNewAssociate(User user, String role);

    /**
     * Method used to be overriden in the subclasses to change the spending limit
     * of the business account.
     *
     * @param newLimit - the new spending limit
     * @return true if the limit was changed, false if the user does not have the permission
     */
    public abstract boolean changeSpendingLimit(double newLimit);

    /**
     * Method used to be overriden in the subclasses to change the deposit limit
     * of the business account.
     *
     * @param newLimit - the new deposit limit
     * @return true if the limit was changed, false if the user does not have the permission
     */
    public abstract boolean changeDepositLimit(double newLimit);
}
