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
    private String currency;
    private double amount;
    private double minBalance;
    private String target;
    private String description;
    private String cardNumber;
    private String commerciant;
    private int timestamp;
    private int startTimestamp;
    private int endTimestamp;
    private String receiver;
    private String alias;
    private String accountType;
    private double interestRate;
    private List<String> accounts;

    public Command(final CommandInput command) {
        this.command = command.getCommand();
        this.email = command.getEmail();
        this.account = command.getAccount();
        this.currency = command.getCurrency();
        this.amount = command.getAmount();
        this.minBalance = command.getMinBalance();
        this.target = command.getTarget();
        this.description = command.getDescription();
        this.cardNumber = command.getCardNumber();
        this.commerciant = command.getCommerciant();
        this.timestamp = command.getTimestamp();
        this.startTimestamp = command.getStartTimestamp();
        this.endTimestamp = command.getEndTimestamp();
        this.receiver = command.getReceiver();
        this.alias = command.getAlias();
        this.accountType = command.getAccountType();
        this.interestRate = command.getInterestRate();
        this.accounts = command.getAccounts();
    }

    /**
     * Abstract method meant to be implemented by the subclasses
     * to do a specific action depending on the command called.
     */
    @Override
    public abstract void execute();
}
