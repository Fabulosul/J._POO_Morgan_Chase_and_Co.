package org.poo.main.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.UserInput;
import org.poo.main.bankaccounts.BankAccount;
import org.poo.main.serviceplans.GoldUserPlan;
import org.poo.main.serviceplans.SilverUserPlan;
import org.poo.main.serviceplans.StandardUserPlan;
import org.poo.main.serviceplans.StudentPlan;
import org.poo.main.serviceplans.UserPlan;
import org.poo.main.transaction.Transaction;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public final class User {
    private String firstName;
    private String lastName;
    private String email;
    private int age;
    private String occupation;
    private UserPlan servicePlan;
    // A list of bank accounts that the user has opened
    private List<BankAccount> bankAccounts;
    // A map that maps the IBAN of a bank account to a bank account object
    private Map<String, BankAccount> ibanToAccountMap;
    // A map that maps the alias of a bank account to a bank account object
    private Map<String, BankAccount> aliasToAccountMap;
    // An array of transactions that the user has made
    private ArrayNode transactionsReport;
    // A counter that keeps track of the number of transactions made by a silver user
    // that are over 300 RON
    private int upgradeCounter;
    public static final int CURRENT_YEAR = 2025;
    private static final int GOLD_AUTO_UPGRADE_THRESHOLD = 300;
    private static final long NUM_TRANSACTIONS_AUTO_UPGRADE = 5;

    public User(final UserInput user) {
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        String birthDate = user.getBirthDate();
        String[] values = birthDate.split("-");
        int year = Integer.parseInt(values[0]);
        this.age = CURRENT_YEAR - year;
        this.occupation = user.getOccupation();
        this.servicePlan = this.occupation.equals("student")
                ? new StudentPlan() : new StandardUserPlan();
        this.bankAccounts = new ArrayList<>();
        this.ibanToAccountMap = new HashMap<>();
        this.aliasToAccountMap = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        this.transactionsReport = mapper.createArrayNode();
        this.upgradeCounter = 0;
    }

    /**
     * Adds a bank account to the current user.
     * It updates the bankAccounts list and the ibanToAccountMap by adding the new bank account.
     *
     * @param bankAccount -> the bank account to be added
     */
    public void addBankAccount(final BankAccount bankAccount) {
        bankAccounts.add(bankAccount);
        ibanToAccountMap.put(bankAccount.getIban(), bankAccount);
    }

    /**
     * Removes a bank account from the current user.
     * It updates the bankAccounts list and the ibanToAccountMap by removing the bank account
     * from both structures.
     * It also removes the alias of the bank account from the user's aliasToAccountMap.
     *
     * @param bankAccount -> the bank account to be removed
     */
    public void removeBankAccount(final BankAccount bankAccount) {
        bankAccounts.remove(bankAccount);
        ibanToAccountMap.remove(bankAccount.getIban());
        removeAliasByAccount(bankAccount);
    }

    /**
     * Method used to get a bank account by its IBAN.
     * It uses the get method of the ibanToAccountMap to get the bank account
     * in O(1) time complexity.
     *
     * @param iban -> the IBAN of the searched bank account
     * @return the bank account with the given IBAN
     */
    public BankAccount getAccountByIban(final String iban) {
        return ibanToAccountMap.get(iban);
    }

    /**
     * Method used to get a bank account by its alias.
     * It uses the get method of the aliasToAccountMap to get the bank account
     * in O(1) time complexity.
     *
     * @param alias -> the alias of the searched bank account
     * @return the bank account with the given alias
     */
    public BankAccount getAccountByAlias(final String alias) {
        return aliasToAccountMap.get(alias);
    }

    /**
     * Method used to add a bank account to the aliasToAccountMap.
     * It maps the alias of the bank account to a bank account object by
     * calling the put method of the aliasToAccountMap.
     *
     * @param alias -> the alias to be added
     * @param bankAccount -> the bank account that corresponds to the alias
     */
    public void addAccountByAlias(final String alias, final BankAccount bankAccount) {
        aliasToAccountMap.put(alias, bankAccount);
    }

    /**
     * Method used to remove all aliases associated with a given bank account.
     * This method iterates through the aliasToAccountMap and removes
     * any entries where the account matches the given bank account.
     *
     * @param bankAccount -> the bank account for which all the aliases are removed
     */
    public void removeAliasByAccount(final BankAccount bankAccount) {
        aliasToAccountMap.entrySet().removeIf(entry -> entry.getValue().equals(bankAccount));
    }

    /**
     * Method used to add a transaction to the ArrayNode of user transactions.
     * It creates a new object mapper and uses it together with the convertValue
     * method to convert the transaction object to an ObjectNode and then adds
     * it to the transactionsReport ArrayNode.
     *
     * @param transaction -> the transaction to be added to the report
     */
    public void addTransaction(final Transaction transaction) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode transactionNode = mapper.convertValue(transaction, ObjectNode.class);
        transactionsReport.add(transactionNode);
    }

    /**
     * Method used to change the service plan of the user.
     * It changes the service plan of the user based on the new plan type
     * by creating a new plan object and assigning it to the servicePlan field.
     *
     * @param newPlanType -> the new plan type to be assigned
     */
    public void changeServicePlan(final String newPlanType) {
        switch (newPlanType) {
            case "student":
                this.servicePlan = new StudentPlan();
                break;
            case "standard":
                this.servicePlan = new StandardUserPlan();
                break;
            case "silver":
                this.servicePlan = new SilverUserPlan();
                break;
            case "gold":
                this.servicePlan = new GoldUserPlan();
                break;
            default:
                break;
        }
    }

    /**
     * Method used to find a classic account by its currency.
     * It iterates through the bankAccounts list and returns the first
     * classic account that has the given currency.
     *
     * @param currency -> the currency of the searched account
     * @return the first classic account with the given currency
     */
    public BankAccount findClassicAccountByCurrency(final String currency) {
        for (BankAccount account : bankAccounts) {
            if (account.getCurrency().equals(currency)
                    && account.getAccountType().equals("classic")) {
                return account;
            }
        }
        return null;
    }

    /**
     * Method used to check if a user is eligible for an auto-upgrade.
     * It checks if the user has a silver plan and if the upgrade counter
     * is greater than 5.
     * If the user has made enough transactions toqq meet the criteria, the user
     * is upgraded to a gold plan and a transaction is added to the user's transaction list.
     *
     * @param bankAccount -> the bank account that the user has made the transaction from
     * @param amountInRon -> the amount of the transaction in RON
     * @param timestamp -> the timestamp of the transaction
     */
    public void checkForAutoUpgrade(final BankAccount bankAccount, final double amountInRon,
                                    final int timestamp) {
        if (this.getServicePlan().getPlanName().equals("silver")
                && amountInRon >= GOLD_AUTO_UPGRADE_THRESHOLD) {
            this.setUpgradeCounter(this.getUpgradeCounter() + 1);
            if (this.getUpgradeCounter() == NUM_TRANSACTIONS_AUTO_UPGRADE) {
                this.changeServicePlan("gold");
                Transaction transaction = new Transaction
                        .TransactionBuilder(timestamp, "Upgrade plan")
                        .accountIban(bankAccount.getIban())
                        .newPlanType("gold")
                        .build();
                this.addTransaction(transaction);
                bankAccount.addTransaction(transaction);
            }
        }
    }


}
