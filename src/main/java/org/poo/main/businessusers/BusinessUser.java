package org.poo.main.businessusers;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.bank.BusinessAccount;
import org.poo.main.bank.Card;
import org.poo.main.bank.User;

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

    public abstract void addNewAssociate(User user, String role);
    public abstract boolean changeSpendingLimit(double newLimit);
    public abstract boolean changeDepositLimit(double newLimit);
    public void addCard(final Card card) { }
    public void removeCard(final Card card) { }

}
