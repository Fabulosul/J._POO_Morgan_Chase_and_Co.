package org.poo.main.businessusers;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.bank.BusinessAccount;
import org.poo.main.bank.User;

@Getter
@Setter
public final class Manager extends BusinessUser {

    public Manager(final String name, final BusinessAccount businessAccount) {
        super(name, businessAccount);
    }

    @Override
    public void addNewAssociate(final User user, final String role) {
    }

    @Override
    public boolean changeSpendingLimit(final double newLimit) {
        return false;
    }

    @Override
    public boolean changeDepositLimit(final double newLimit) {
        return false;
    }

}
