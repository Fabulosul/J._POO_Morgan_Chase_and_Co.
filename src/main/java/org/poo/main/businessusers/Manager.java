package org.poo.main.businessusers;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.bank.BusinessAccount;
import org.poo.main.bank.Card;
import org.poo.main.bank.User;

@Getter
@Setter
public class Manager extends BusinessUser {

    public Manager(String name, BusinessAccount businessAccount) {
        super(name, businessAccount);
    }

    @Override
    public boolean addNewAssociate(User user, String role) {
        return false;
    }

    @Override
    public boolean changeSpendingLimit(double newLimit) {
        return false;
    }

    @Override
    public boolean changeDepositLimit(double newLimit) {
        return false;
    }

}
