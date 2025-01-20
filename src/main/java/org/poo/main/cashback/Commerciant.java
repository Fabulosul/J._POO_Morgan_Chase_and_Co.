package org.poo.main.cashback;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.user.User;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public final class Commerciant {
    public enum Category {
        FOOD, CLOTHES, TECH, OTHER
    }
    public enum CashbackStrategy {
        NR_OF_TRANSACTIONS, SPENDING_THRESHOLD, NONE
    }
    private final String name;
    private final Category category;
    private final CashbackStrategy cashbackStrategy;
    // The amount spent at the current commerciant
    private double amountSpent;
    // The number of transactions made at the current commerciant
    private int nrOfTransactions;
    // The list of users that have made transactions at the current commerciant
    private List<User> users;

    public Commerciant(final String name, final String category, final String cashbackStrategy) {
        this.name = name;
        this.category = switch (category) {
            case "Food" -> Category.FOOD;
            case "Clothes" -> Category.CLOTHES;
            case "Tech" -> Category.TECH;
            default -> Category.OTHER;
        };
        this.cashbackStrategy = switch (cashbackStrategy) {
            case "nrOfTransactions" -> CashbackStrategy.NR_OF_TRANSACTIONS;
            case "spendingThreshold" -> CashbackStrategy.SPENDING_THRESHOLD;
            default -> CashbackStrategy.NONE;
        };
        this.amountSpent = 0;
        this.nrOfTransactions = 0;
        this.users = new ArrayList<>();
    }

    /**
     * Method that adds a user to the list of users that have made transactions
     * at the current commerciant.
     *
     * @param user -> the user to be added.
     */
    public void addUser(final User user) {
        users.add(user);
    }
}
