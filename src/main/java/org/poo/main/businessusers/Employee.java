package org.poo.main.businessusers;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.bank.BusinessAccount;
import org.poo.main.bank.Card;
import org.poo.main.bank.User;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public final class Employee extends BusinessUser {
    private List<Card> createdCards;

    public Employee(final String name, final BusinessAccount businessAccount) {
        super(name, businessAccount);
        this.createdCards = new ArrayList<>();
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

    @Override
    public void addCard(final Card card) {
        createdCards.add(card);
    }

    @Override
    public void removeCard(final Card card) {
        createdCards.remove(card);
    }
}
