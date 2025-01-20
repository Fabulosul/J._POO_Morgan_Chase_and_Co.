package org.poo.main.cashback;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.bank.User;

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
    private final CashbackStrategy type;
    private double amountSpent;
    private int nrOfTransactions;
    private List<User> users;

    public Commerciant(final String name, final String category, final String type) {
        this.name = name;
        this.category = switch (category) {
            case "Food" -> Category.FOOD;
            case "Clothes" -> Category.CLOTHES;
            case "Tech" -> Category.TECH;
            default -> Category.OTHER;
        };
        this.type = switch (type) {
            case "nrOfTransactions" -> CashbackStrategy.NR_OF_TRANSACTIONS;
            case "spendingThreshold" -> CashbackStrategy.SPENDING_THRESHOLD;
            default -> CashbackStrategy.NONE;
        };
        this.amountSpent = 0;
        this.nrOfTransactions = 0;
        this.users = new ArrayList<>();
    }

    public void addUser(final User user) {
        users.add(user);
    }
}
