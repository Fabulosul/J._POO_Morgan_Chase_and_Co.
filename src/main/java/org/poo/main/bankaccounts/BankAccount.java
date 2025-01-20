package org.poo.main.bankaccounts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommerciantInput;
import org.poo.main.bank.Bank;
import org.poo.main.card.Card;
import org.poo.main.transaction.Transaction;
import org.poo.main.user.User;
import org.poo.main.cashback.CashbackObserver;
import org.poo.main.cashback.Commerciant;
import org.poo.main.cashback.PaymentDetails;
import org.poo.main.cashback.Voucher;
import org.poo.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class BankAccount {
    private String iban;
    private double balance;
    private String currency;
    // Field that is used to differentiate between classic and savings accounts
    private List<Card> cards;
    // Map that makes it easier to find a card by its number
    private Map<String, Card> cardNrToCardMap;
    // Field that stores the minimum balance of the account
    private double minBalance;
    // Field that stores the transactions of the current account
    private ArrayNode transactions;
    // A list of cashback observers that are notified when a payment is made
    private List<CashbackObserver> cashbackObservers;
    // A list of vouchers that represent the nrOfTransactions type of cashback
    private List<Voucher> cashbackVouchers;
    // Field that stores the number of transactions made with the account
    private int nrOfTransactions;
    enum AccountType {
        CLASSIC, SAVINGS, BUSINESS
    }
    // The type of the account
    private AccountType accountType;
    // A list of commerciants that offer cashback
    private List<Commerciant> commerciants;
    // Filed that stores the amount spend at spendingThreshold commerciants
    private double spendingThresholdAmount;


    public BankAccount(final Bank bank, final String currency) {
        this.iban = Utils.generateIBAN();
        this.balance = 0;
        this.currency = currency;
        this.accountType = AccountType.CLASSIC;
        this.cards = new ArrayList<>();
        this.cardNrToCardMap = new HashMap<>();
        this.minBalance = 0;
        ObjectMapper mapper = new ObjectMapper();
        this.transactions = mapper.createArrayNode();
        this.cashbackObservers = new ArrayList<>();
        this.cashbackVouchers = new ArrayList<>();
        this.nrOfTransactions = 0;
        this.commerciants = new ArrayList<>();
        for (CommerciantInput commerciant : bank.getCommerciants()) {
            this.commerciants.add(new Commerciant(commerciant.getCommerciant(),
                    commerciant.getType(), commerciant.getCashbackStrategy()));
        }
        this.spendingThresholdAmount = 0;
    }

    /**
     * Method used to return the account type as a string.
     *
     * @return a string representing the account type
     */
    public final String getAccountType() {
        return switch (accountType) {
            case CLASSIC -> "classic";
            case SAVINGS -> "savings";
            case BUSINESS -> "business";
        };
    }

    /**
     * Method that adds a card to current account and updates the cardNrToCardMap.
     *
     * @param card -> the card to be added to the account
     */
    public void addCard(final Card card) {
        cards.add(card);
        cardNrToCardMap.put(card.getCardNumber(), card);
    }

    /**
     * Method that removes a card from the current account and updates the cardNrToCardMap.
     *
     * @param card -> the card to be removed from the account
     */
    public void removeCard(final Card card) {
        cards.remove(card);
        cardNrToCardMap.remove(card.getCardNumber());
    }

    /**
     * Method that return a card by a given card number.
     *
     * @param cardNumber -> the card number of the card to be found
     * @return the card with the given card number
     */
    public final Card getCardByCardNr(final String cardNumber) {
        return cardNrToCardMap.get(cardNumber);
    }

    /**
     * Helper method used to add an amount of money to the current account.
     *
     * @param amount -> the amount of money to be added
     */
    public final void addMoney(final double amount) {
        balance += amount;
    }

    /**
     * Helper method used to deduct an amount of money from the current account.
     * It checks if the balance of the account is sufficient for the deduction
     * and if it is, it deducts the amount from the balance and returns true,
     * otherwise it returns false.
     *
     * @param amount -> the amount of money to be deducted from the current account
     * @return true if the balance is sufficient for the deduction, false otherwise
     */
    public final boolean deductMoney(final double amount) {
        if (amount > balance) {
            return false;
        }
        balance -= amount;
        return true;
    }

    /**
     * Method used to make an online payment.
     * If the currency of the payment is the same as the currency of the account,
     * the amount is directly deducted from the account.
     * Otherwise, the amount is converted to the currency of the account by calling
     * the convertCurrency method found in the Bank class and then deducts the
     * converted amount from the account.
     *
     * @param bank -> the bank that is used to convert the currency
     * @param amount -> the amount of money to be deducted from the account
     * @param paymentCurrency -> the currency of the payment
     * @return true if the payment was successful, false otherwise
     *
     * @see Bank for the implementation of the convertCurrency method
     */
    public final boolean payWithCommission(final Bank bank, final double amount,
                                           final String paymentCurrency) {
        double amountInRon = bank.convertCurrency(amount, paymentCurrency, "RON");
        double amountWithCommission = calculateAmountWithCommission(bank, amountInRon);
        double convertedAmountWithCommission = bank.convertCurrency(amountWithCommission,
                "RON", getCurrency());
        return deductMoney(convertedAmountWithCommission);

    }

    /**
     * Method used to make a payment without commission.
     * It simply converts the amount to the currency of the account by calling the
     * convertCurrency method found in the Bank class and then deducts the converted
     * amount from the account.
     *
     * @param bank -> the bank that is used to convert the currency
     * @param amount -> the amount of money to be deducted from the account
     * @param paymentCurrency -> the currency of the payment
     */
    public final void payWithoutCommission(final Bank bank, final double amount,
                                           final String paymentCurrency) {
        double convertedAmount = bank.convertCurrency(amount, paymentCurrency, getCurrency());
        deductMoney(convertedAmount);
    }

    /**
     * Method used to transfer money from the current account to another account.
     * If the balance of the account is not sufficient for the transfer, the method
     * returns false. Otherwise, it deducts the amount from the current account and
     * checks if the currency of the receiver account is the same as the currency of
     * the current account.
     * If the two currencies are the same, the amount is directly sent to the
     * receiver account. Otherwise, the amount is converted to the currency of
     * the receiver account by calling the convertCurrency method and then sent
     * to the receiver account.
     * If this whole process is successful, the method returns true.
     *
     * @param bank -> the bank that is used to convert the currency if needed
     * @param receiverAccount -> the account that receives the money
     * @param amount -> the amount of money to be transferred
     * @return true if the transfer was successful, false otherwise
     *
     * @see Bank for details about the implementation of the convertCurrency method
     */
    public final boolean sendMoney(final Bank bank, final BankAccount receiverAccount,
                                   final double amount) {
        double amountInRon = bank.convertCurrency(amount, currency,
                "RON");
        double amountWithCommission = calculateAmountWithCommission(bank, amountInRon);
        double convertedAmountWithCommission = bank.convertCurrency(amountWithCommission,
                "RON", currency);

        if (convertedAmountWithCommission > balance) {
            return false;
        }
        deductMoney(convertedAmountWithCommission);

        if (receiverAccount.getCurrency().equals(currency)) {
            receiverAccount.addMoney(amount);
        } else {
            double convertedAmount = bank.convertCurrency(amount, currency,
                    receiverAccount.getCurrency());
            receiverAccount.addMoney(convertedAmount);
        }
        return true;
    }

    /**
     * Method used to transfer money from the current account to another account
     * without commission. The process is similar to the one in the sendMoney method,
     * but without the commission.
     *
     * @param bank -> the bank that is used to convert the currency if needed
     * @param receiverAccount -> the account that receives the money
     * @param amount -> the amount of money to be transferred
     */
    public final void sendMoneyWithoutCommission(final Bank bank, final BankAccount receiverAccount,
                                                 final double amount) {
        double amountInRon = bank.convertCurrency(amount, currency, "RON");
        double convertedAmount = bank.convertCurrency(amountInRon, "RON", currency);
        if (convertedAmount > balance) {
            return;
        }

        deductMoney(convertedAmount);

        if (receiverAccount.getCurrency().equals(currency)) {
            receiverAccount.addMoney(amount);
        } else {
            double receiverConvertedAmount =
                    bank.convertCurrency(amount, currency, receiverAccount.getCurrency());
            receiverAccount.addMoney(receiverConvertedAmount);
        }

    }

    /**
     * Method used to check if the current account has enough balance for a payment.
     * If the currency of the payment is the same as the currency of the account,
     * the method returns true if the balance is greater than the amount, false otherwise.
     * In the other case, the amount is converted to the currency of the account by
     * calling the convertCurrency method and then the method returns true if the
     * converted amount is less than the balance, false otherwise.
     *
     * @param amount -> the amount of money to be deducted from the account
     * @param paymentCurrency -> the currency of the payment
     * @param bank -> the bank that is used to convert the currency
     * @return true if the account has sufficient funds to complete the payment,
     * false otherwise
     *
     * @see Bank for details about the implementation of the convertCurrency method
     */
    public final boolean hasSufficientFunds(final double amount, final String paymentCurrency,
                                            final Bank bank) {
        if (currency.equals(paymentCurrency)) {
            return amount <= balance;
        }
        double convertedAmount = bank.convertCurrency(amount, paymentCurrency, currency);
        return convertedAmount <= balance;
    }

    /**
     * Method used to add a transaction to the transactions ArrayNode.
     * It creates a new object mapper and uses it together with the convertValue
     * method to convert the transaction object to an ObjectNode and then adds
     * it to the transactions ArrayNode.
     *
     * @param transaction -> the transaction to be added to the ArrayNode
     */
    public final void addTransaction(final Transaction transaction) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode transactionNode = mapper.convertValue(transaction, ObjectNode.class);
        transactions.add(transactionNode);
    }

    /**
     * Method used to calculate the amount plus the commission for a payment.
     * It gets the user of the current account from the bank and then calls the
     * calculateCommission method from the ServicePlan class to calculate the
     * commission for the given amount. At the end, it returns the amount plus
     * the commission.
     *
     * @param bank -> the bank that is used to get the user of the current account
     * @param amount -> the amount of money for which the commission is calculated
     * @return the amount with commission that needs to be paid
     */
    public final double calculateAmountWithCommission(final Bank bank, final double amount) {
        User user = bank.getUserByAccount(iban);
        if (user == null) {
            return amount;
        }
        double commission = user.getServicePlan().calculateCommission(amount);
        return amount + commission;
    }

    /**
     * Method used to add a cashback observer to the list of observers.
     *
     * @param observer -> the observer to be added
     */
    public final void addCashbackObserver(final CashbackObserver observer) {
        cashbackObservers.add(observer);
    }

    /**
     * Method used to remove a cashback observer from the list of observers.
     *
     * @param observer -> the observer to be removed
     */
    public final void removeCashbackObserver(final CashbackObserver observer) {
        cashbackObservers.remove(observer);
    }

    /**
     * Method used to notify all the cashback observers that a payment was made.
     *
     * @param paymentDetails -> the details of the payment
     */
    public final void notifyCashbackObservers(final PaymentDetails paymentDetails) {
        for (CashbackObserver observer : cashbackObservers) {
            observer.update(paymentDetails);
        }
    }

    /**
     * Method used to add a voucher to the list of cashback vouchers.
     * THe vouchers represent the cashback that the user receives for a certain
     * number of transactions.
     *
     * @param voucher -> the voucher to be added to the list
     */
    public final void addVoucher(final Voucher voucher) {
        cashbackVouchers.add(voucher);
    }
}
