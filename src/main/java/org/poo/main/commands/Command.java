package org.poo.main.commands;

import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;

import java.util.List;

@Getter
@Setter
public abstract class Command implements CommandInterface {
    private String command;
    private String email;
    private String account;
    private String newPlanType;
    private String role;
    private String currency;
    private String target;
    private String description;
    private String cardNumber;
    private String commerciant;
    private String receiver;
    private String alias;
    private String accountType;
    private String splitPaymentType;
    private String type;
    private String location;
    private int timestamp;
    private int startTimestamp;
    private int endTimestamp;
    private double interestRate;
    private double spendingLimit;
    private double depositLimit;
    private double amount;
    private double minBalance;
    private List<String> accounts;
    private List<Double> amountForUsers;

    public Command(final CommandInput command) {
        this.command = command.getCommand();
        this.email = command.getEmail();
        this.account = command.getAccount();
        this.newPlanType = command.getNewPlanType();
        this.role = command.getRole();
        this.currency = command.getCurrency();
        this.target = command.getTarget();
        this.description = command.getDescription();
        this.cardNumber = command.getCardNumber();
        this.commerciant = command.getCommerciant();
        this.receiver = command.getReceiver();
        this.alias = command.getAlias();
        this.accountType = command.getAccountType();
        this.splitPaymentType = command.getSplitPaymentType();
        this.type = command.getType();
        this.location = command.getLocation();
        this.timestamp = command.getTimestamp();
        this.startTimestamp = command.getStartTimestamp();
        this.endTimestamp = command.getEndTimestamp();
        this.interestRate = command.getInterestRate();
        this.spendingLimit = command.getSpendingLimit();
        this.depositLimit = command.getDepositLimit();
        this.amount = command.getAmount();
        this.minBalance = command.getMinBalance();
        this.accounts = command.getAccounts();
        this.amountForUsers = command.getAmountForUsers();
    }

    /**
     * Abstract method meant to be implemented by the subclasses
     * to do a specific action depending on the command called.
     */
    @Override
    public abstract void execute();
}
