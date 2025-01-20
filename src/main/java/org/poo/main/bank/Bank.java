package org.poo.main.bank;

import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommerciantInput;
import org.poo.fileio.ExchangeInput;
import org.poo.main.cashback.Commerciant;
import org.poo.main.splitpayment.SplitPaymentDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public final class Bank {
    // A list that stores the users of the bank.
    private List<User> users;
    // A list that stores the exchange rates between currencies.
    private List<ExchangeInput> exchangeRates;
    // A map that stores the exchange rates between currencies.
    private Map<String, Map<String, Double>> exchangeRateMap;
    // A list that stores the commerciants.
    private List<CommerciantInput> commerciants;
    private Map<String, Commerciant> nameToCommerciantMap;
    // A map that stores the users by email to enable faster searching.
    private Map<String, User> emailToUserMap;
    private List<SplitPaymentDetails> splitPayments;

    public Bank(final List<User> users, final List<ExchangeInput> exchangeRates,
                final List<CommerciantInput> commerciants) {
        this.users = users;
        this.exchangeRates = exchangeRates;
        this.exchangeRateMap = new HashMap<>();
        // Simulate a non-directed graph where the currencies are the nodes
        // and the exchange rates are the edges between the nodes.
        // Add both the direct and inverse exchange rates to keep the idea of a non-directed graph.
        for (ExchangeInput exchangeRate : exchangeRates) {
            String fromCurrency = exchangeRate.getFrom();
            String toCurrency = exchangeRate.getTo();
            double rate = exchangeRate.getRate();

            exchangeRateMap.putIfAbsent(fromCurrency, new HashMap<>());
            exchangeRateMap.get(fromCurrency).put(toCurrency, rate);

            exchangeRateMap.putIfAbsent(toCurrency, new HashMap<>());
            exchangeRateMap.get(toCurrency).put(fromCurrency, 1 / rate);
        }
        this.commerciants = commerciants;
        this.nameToCommerciantMap = new HashMap<>();
        for (CommerciantInput commerciant : commerciants) {
            Commerciant newCommerciant = new Commerciant(commerciant.getCommerciant(),
                    commerciant.getType(),
                    commerciant.getCashbackStrategy());
            nameToCommerciantMap.put(newCommerciant.getName(), newCommerciant);
        }
        this.emailToUserMap = new HashMap<>();
        // Add all users to the emailToUserMap to enable faster searching by email
        for (User user : users) {
            addUserByMail(user.getEmail(), user);
        }
        this.splitPayments = new ArrayList<>();
    }

    /**
     * Add a user to the emailToUserMap to enable faster searching for users by email.
     *
     * @param email -> the email of the user
     * @param user -> the user to be added
     */
    public void addUserByMail(final String email, final User user) {
        emailToUserMap.put(email, user);
    }

    /**
     * Get a user from the emailToUserMap by email in O(1) time.
     *
     * @param email -> the email of the user
     * @return the user with the given email or null if no such user exists
     */
    public User getUserByMail(final String email) {
        return emailToUserMap.get(email);
    }

    /**
     * Get the user that has an account opened with the given iban.
     * This method iterates through all users and checks if any of them has
     * an account with the given iban by calling the getAccountByIban method.
     *
     * @param iban -> the iban of the account for which the user is searched
     * @return the user that has an account with the given iban or null if no such user exists
     */
    public User getUserByAccount(final String iban) {
        for (User user : users) {
            if (user.getAccountByIban(iban) != null) {
                return user;
            }
        }
        return null;
    }

    /**
     * Get the card with the given card number.
     * This method iterates through all users and all bank accounts of each user
     * and checks if any of the cards has the given card number by calling the
     * getCardByCardNr method.
     *
     * @param cardNr -> the card number of the searched card
     * @return a card with the given card number or null if no such card exists
     */
    public Card getCardByCardNr(final String cardNr) {
        for (User user : users) {
            for (BankAccount bankAccount : user.getBankAccounts()) {
                Card card = bankAccount.getCardByCardNr(cardNr);
                if (card != null) {
                    return card;
                }
            }
        }
        return null;
    }

    /**
     * Get the account that has a card with the given card number.
     * This method iterates through all users and all bank accounts of each user
     * and checks if any of the bank accounts has a card with the given card number
     * by calling the getCardByCardNr method.
     *
     * @param cardNr -> the card number of the card for which the account is searched
     * @return the account that has a card with the given card number or null if
     * no such account exists
     */
    public BankAccount getAccountByCardNr(final String cardNr) {
        for (User user : users) {
            for (BankAccount bankAccount : user.getBankAccounts()) {
                Card card = bankAccount.getCardByCardNr(cardNr);
                if (card != null) {
                    return bankAccount;
                }
            }
        }
        return null;
    }

    /**
     * Convert an amount from a currency to another.
     * If the fromCurrency is the same as the toCurrency, the amount is returned as it is.
     * If the fromCurrency or the toCurrency are not present in the exchangeRateMap or
     * a path between the two currencies is not found in the exchangeRateMap,
     * an IllegalArgumentException is thrown.
     * The method uses a depth-first search(DFS, in this context the method called
     * findConversionPath) to find a path from the fromCurrency to the toCurrency
     * since the exchangeRateMap is used as a graph where the currencies are
     * the nodes and the exchange rates are the edges.
     * If a path is found, the method calculates the total rate of the conversion by multiplying
     * the exchange rates between the currencies in the found path.
     *
     * @param amount -> the amount to be converted
     * @param fromCurrency -> the currency from which the amount is converted
     * @param toCurrency -> the currency to which the amount is converted
     * @return the amount converted from fromCurrency to toCurrency
     */
    public double convertCurrency(final double amount, final String fromCurrency,
                                  final String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        if (!exchangeRateMap.containsKey(fromCurrency)) {
            throw new IllegalArgumentException("Unknown currency: " + fromCurrency);
        }

        if (!exchangeRateMap.containsKey(toCurrency)) {
            throw new IllegalArgumentException("Unknown currency: " + toCurrency);
        }

        Map<String, Boolean> visited = new HashMap<>();
        for (String currency : exchangeRateMap.keySet()) {
            visited.put(currency, false);
        }
        List<String> path = new ArrayList<>();

        boolean foundPath = findConversionPath(visited, fromCurrency, toCurrency, path);

        if (!foundPath) {
            throw new IllegalArgumentException("No conversion path found from "
                    + fromCurrency + " to " + toCurrency);
        }

        double totalRate = 1.0;
        for (int i = 0; i < path.size() - 1; i++) {
            totalRate *= exchangeRateMap.get(path.get(i)).get(path.get(i + 1));
        }
        return amount * totalRate;
    }

    /**
     * This method is a contextual representation of the depth-first search algorithm and
     * is used to find a path from a currency to another in the exchangeRateMap.
     * The method is called recursively for each unvisited currency that has an exchange rate
     * with the current currency and stops when the targetCurrency is reached or when all currencies
     * are visited.
     * In essence, the method marks the current currency as visited, adds it to the path and
     * recursively calls itself for each unvisited currency that has an exchange rate with
     * the current currency. After all neighbors of the current currency are visited, the
     * current currency is removed from the path and the method returns false. However,
     * if the targetCurrency is reached, the method returns true.
     *
     * @param visited -> map that stores if a currency was visited in the DFS
     * @param currentCurrency -> the currency that is currently visited
     * @param targetCurrency -> the currency that is searched
     * @param path -> the visited currencies in the current attempt to find a path
     * @return true if a path from currentCurrency to targetCurrency is found, false otherwise
     */
    public boolean findConversionPath(final Map<String, Boolean> visited,
                                      final String currentCurrency, final String targetCurrency,
                                      final List<String> path) {
        visited.put(currentCurrency, true);
        path.add(currentCurrency);

        if (currentCurrency.equals(targetCurrency)) {
            return true;
        }

        for (String currency : exchangeRateMap.get(currentCurrency).keySet()) {
            if (!visited.get(currency)) {
                boolean pathFound = findConversionPath(visited,
                        currency, targetCurrency, path);
                if (pathFound) {
                    return true;
                }
            }
        }
        path.removeLast();
        return false;
    }

    /**
     * Search for an account with a given alias.
     * This method iterates through all users and checks if any of them has an alias
     * that matches the given alias by calling the getAccountByAlias method.
     *
     * @param alias -> the alias of the account that is searched
     * @return the account with the given alias or null if no such account exists
     */
    public BankAccount findAccountByAlias(final String alias) {
        for (User user : users) {
            if (user.getAccountByAlias(alias) != null) {
                return user.getAccountByAlias(alias);
            }
        }
        return null;
    }

    /**
     * Search for an account with a given iban.
     * This method iterates through all users and checks if any of them has an account
     * with the given iban by calling the getAccountByIban method.
     *
     * @param iban -> the iban of the account that is searched
     * @return the account with the given iban or null if no such account exists
     */
    public BankAccount findAccountByIban(final String iban) {
        for (User user : users) {
            if (user.getAccountByIban(iban) != null) {
                return user.getAccountByIban(iban);
            }
        }
        return null;
    }

    public Commerciant getCommerciantByName(final String commerciant) {
        return nameToCommerciantMap.get(commerciant);
    }

    public void addSplitPayment(final SplitPaymentDetails splitPaymentDetails) {
        splitPayments.add(splitPaymentDetails);
    }

    public void removeSplitPayment(final SplitPaymentDetails splitPaymentDetails) {
        splitPayments.remove(splitPaymentDetails);
    }

    public void acceptSplitPayment(final User user, final String splitPaymentType) {
        SplitPaymentDetails.SplitPaymentType paymentType;
        paymentType = splitPaymentType.equals("custom")
                ? SplitPaymentDetails.SplitPaymentType.CUSTOM
                : SplitPaymentDetails.SplitPaymentType.EQUAL;
        Iterator<SplitPaymentDetails> iterator = splitPayments.iterator();
        while (iterator.hasNext()) {
            SplitPaymentDetails splitPayment = iterator.next();
            if (splitPayment.getPaymentType() == paymentType) {
                if (splitPayment.acceptPayment(user)) {
                    iterator.remove();
                }
            }
        }
    }

    public void rejectSplitPayment(final User user, final String splitPaymentType) {
        SplitPaymentDetails.SplitPaymentType paymentType;
        paymentType = splitPaymentType.equals("custom")
                ? SplitPaymentDetails.SplitPaymentType.CUSTOM
                : SplitPaymentDetails.SplitPaymentType.EQUAL;
        for (SplitPaymentDetails splitPayment : splitPayments) {
            if (splitPayment.getPaymentType() == paymentType) {
                splitPayment.rejectPayment(user);
                removeSplitPayment(splitPayment);
                return;
            }
        }
    }
}
