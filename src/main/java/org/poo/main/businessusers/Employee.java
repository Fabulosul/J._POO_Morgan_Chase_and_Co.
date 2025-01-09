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
public class Employee extends BusinessUser {
    List<Card> createdCards;

    public Employee(String name, BusinessAccount businessAccount) {
        super(name, businessAccount);
        this.createdCards = new ArrayList<>();
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

    @Override
    public void addCard(Card card) {
        createdCards.add(card);
    }

    @Override
    public void removeCard(Card card) {
        createdCards.remove(card);
    }
}
