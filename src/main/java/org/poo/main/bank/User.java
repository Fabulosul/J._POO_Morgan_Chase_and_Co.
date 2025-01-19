package org.poo.main.bank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.UserInput;
import org.poo.main.serviceplans.*;


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


    public User(final UserInput user) {
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        String birthDate = user.getBirthDate();
        String[] values = birthDate.split("-");
        int year = Integer.parseInt(values[0]);
        this.age = 2025 - year;
        this.occupation = user.getOccupation();
        this.servicePlan = this.occupation.equals("student") ? new StudentPlan() : new StandardUserPlan();
        this.bankAccounts = new ArrayList<>();
        this.ibanToAccountMap = new HashMap<>();
        this.aliasToAccountMap = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        this.transactionsReport = mapper.createArrayNode();

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
        }
    }

    public BankAccount findClassicAccountByCurrency(final String currency) {
        for (BankAccount account : bankAccounts) {
            if (account.getCurrency().equals(currency) && account.getAccountType().equals("classic")) {
                return account;
            }
        }
        return null;
    }


}
