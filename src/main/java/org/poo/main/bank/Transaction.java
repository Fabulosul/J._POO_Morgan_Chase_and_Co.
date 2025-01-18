package org.poo.main.bank;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
// This annotation is used to exclude null or empty fields from the JSON output.
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class Transaction {
    private String username;
    private int timestamp;
    private String account;
    @JsonProperty("accountIBAN")
    private String accountIban;
    private String card;
    private String cardHolder;
    private String commerciant;
    private String description;
    private String newPlanType;
    private String splitPaymentType;
    @JsonIgnore
    private boolean currencyWithoutAmount;
    // This annotation is used to give a custom name to the field in the JSON output.
    @JsonProperty("senderIBAN")
    private String senderIban;
    @JsonProperty("receiverIBAN")
    private String receiverIban;
    @JsonIgnore
    private String currency;
    private String transferType;
    private String error;
    // This annotation is used to exclude the field from the JSON output.
    @JsonIgnore
    private double amount;
    @JsonIgnore
    private boolean separateAmountAndCurrency;
    private List<String> involvedAccounts;
    private List<Double> amountForUsers;

    /**
     * Getter used to return the amount of the transaction in the
     * wanted format.
     * If the currency field and the amount field are default,
     * it will return null.
     * If the currency field and the amount field are not default,
     * it will return the amount and the currency separated or not
     * depending on the value of the  separateAmountAndCurrency field.
     * If only the amount field is not default, it will return the amount
     * without the currency.
     *
     * @return the amount of the transaction in special format depending on the context
     */
    @JsonProperty("amount")
    public Object getAmount() {
        if ((currency == null && amount == 0) || currencyWithoutAmount) {
            return null;
        }
        if (currency != null && amount != 0) {
            if (separateAmountAndCurrency) {
                return amount;
            } else {
                return amount + " " + currency;
            }
        } else {
            return amount;
        }
    }

    /**
     * Getter used to return the currency of the transaction if needed.
     * If the separateAmountAndCurrency field is true, it will return the currency,
     * otherwise it will return null.
     *
     * @return the currency of the transaction if needed
     */
    @JsonProperty("currency")
    public String getCurrency() {
        if (separateAmountAndCurrency || currencyWithoutAmount) {
            return currency;
        }
        return null;
    }

    @JsonIgnore
    public double getRawAmount() {
        return amount;
    }

    private Transaction(final TransactionBuilder builder) {
        this.username = builder.username;
        this.timestamp = builder.timestamp;
        this.account = builder.account;
        this.accountIban = builder.accountIban;
        this.card = builder.card;
        this.cardHolder = builder.cardHolder;
        this.commerciant = builder.commerciant;
        this.description = builder.description;
        this.newPlanType = builder.newPlanType;
        this.currencyWithoutAmount = builder.currencyWithoutAmount;
        this.splitPaymentType = builder.splitPaymentType;
        this.senderIban = builder.senderIban;
        this.receiverIban = builder.receiverIban;
        this.currency = builder.currency;
        this.transferType = builder.transferType;
        this.error = builder.error;
        this.amount = builder.amount;
        this.separateAmountAndCurrency = builder.separateAmountAndCurrency;
        this.involvedAccounts = builder.involvedAccounts;
        this.amountForUsers = builder.amountForUsers;
    }

    @Getter
    @Setter
    public static final class TransactionBuilder {
        private String username;
        private int timestamp;
        private String account;
        private String accountIban;
        private String card;
        private String cardHolder;
        private String commerciant;
        private String description;
        private String newPlanType;
        private boolean currencyWithoutAmount;
        private String splitPaymentType;
        private String senderIban;
        private String receiverIban;
        private String currency;
        private String transferType;
        private String error;
        private double amount;
        private boolean separateAmountAndCurrency;
        private List<String> involvedAccounts;
        private List<Double> amountForUsers;

        public TransactionBuilder(final int timestamp, final String description) {
            this.timestamp = timestamp;
            this.description = description;
        }

        public TransactionBuilder username(final String username) {
            this.username = username;
            return this;
        }

        /**
         * Method used to set the account field of the transaction in the transactionBuilder
         * with a given iban.
         *
         * @param iban -> the iban of the account
         * @return the transactionBuilder with the account field set
         */
        public TransactionBuilder account(final String iban) {
            this.account = iban;
            return this;
        }

        public TransactionBuilder accountIban(final String iban) {
            this.accountIban = iban;
            return this;
        }

        /**
         * Method used to set the card field of the transaction in the transactionBuilder
         * with a given card number.
         *
         * @param cardNr -> the card number
         * @return the transactionBuilder with the card field set
         */
        public TransactionBuilder card(final String cardNr) {
            this.card = cardNr;
            return this;
        }

        /**
         * Method used to set the cardHolder field of the transaction in the transactionBuilder
         * with a given email.
         *
         * @param email -> the email of the card holder
         * @return the transactionBuilder with the cardHolder field set
         */
        public TransactionBuilder cardHolder(final String email) {
            this.cardHolder = email;
            return this;
        }

        /**
         * Method used to set the commerciant field of the transaction in the transactionBuilder
         * with a given commerciant name.
         *
         * @param commerciantName -> the name of the commerciant
         * @return the transactionBuilder with the commerciant field set
         */
        public TransactionBuilder commerciant(final String commerciantName) {
            this.commerciant = commerciantName;
            return this;
        }

        /**
         * Method used to set the senderIban field of the transaction in the transactionBuilder
         * with a given iban of the sender account.
         *
         * @param senderAccount -> the iban of the sender account
         * @return the transactionBuilder with the senderIban field set
         */
        public TransactionBuilder senderIban(final String senderAccount) {
            this.senderIban = senderAccount;
            return this;
        }

        /**
         * Method used to set the receiverIban field of the transaction in the transactionBuilder
         * with a given iban of the receiver account.
         *
         * @param receiverAccount -> the iban of the receiver account
         * @return the transactionBuilder with the receiverIban field set
         */
        public TransactionBuilder receiverIban(final String receiverAccount) {
            this.receiverIban = receiverAccount;
            return this;
        }

        /**
         * Method used to set the currency field of the transaction in the transactionBuilder
         * with a given currency.
         *
         * @param paymentCurrency -> the currency of the payment
         * @return the transactionBuilder with the currency field set
         */
        public TransactionBuilder currency(final String paymentCurrency) {
            this.currency = paymentCurrency;
            return this;
        }

        /**
         * Method used to set the transferType field of the transaction in the transactionBuilder
         * with a given type of transfer(send or receive).
         *
         * @param typeOfTransfer -> the type of the transfer
         * @return the transactionBuilder with the transferType field set
         */
        public TransactionBuilder transferType(final String typeOfTransfer) {
            this.transferType = typeOfTransfer;
            return this;
        }

        /**
         * Method used to set the error field of the transaction in the transactionBuilder
         * with a given error description.
         *
         * @param errorDescription -> the description of the error
         * @return the transactionBuilder with the error field set
         */
        public TransactionBuilder error(final String errorDescription) {
            this.error = errorDescription;
            return this;
        }

        /**
         * Method used to set the amount field of the transaction in the transactionBuilder
         * with a given value.
         *
         * @param value -> the amount of the transaction
         * @return the transactionBuilder with the amount field set
         */
        public TransactionBuilder amount(final double value) {
            this.amount = value;
            return this;
        }

        /**
         * Method used to set the separateAmountAndCurrency field of the transaction
         * in the transactionBuilder with true or false to know if the amount and the currency
         * should be separated or not.
         *
         * @param splitAmountAndCurrency -> true if the amount and the currency should be separated
         *                               false otherwise
         * @return the transactionBuilder with the separateAmountAndCurrency field set
         */
        public TransactionBuilder separateAmountAndCurrency(final boolean splitAmountAndCurrency) {
            this.separateAmountAndCurrency = splitAmountAndCurrency;
            return this;
        }

        /**
         * Method used to set the involvedAccounts field of the transaction in the
         * transactionBuilder with a given list of accounts that take part in the transaction.
         *
         * @param involvedAccountsList -> the list of involved accounts
         * @return the transactionBuilder with the involvedAccounts field set
         */
        public TransactionBuilder involvedAccounts(final List<String> involvedAccountsList) {
            this.involvedAccounts = involvedAccountsList;
            return this;
        }

        public TransactionBuilder newPlanType(final String newPlanName) {
            this.newPlanType = newPlanName;
            return this;
        }

        public TransactionBuilder splitPaymentType(final String splitPaymentTypeName) {
            this.splitPaymentType = splitPaymentTypeName;
            return this;
        }

        public TransactionBuilder amountForUsers(final List<Double> amountForUsersList) {
            this.amountForUsers = amountForUsersList;
            return this;
        }

        public TransactionBuilder currencyWithoutAmount(final boolean currencyWithoutAmount) {
            this.currencyWithoutAmount = currencyWithoutAmount;
            return this;
        }

        /**
         * Method used to build the transaction with the fields set in the transactionBuilder
         * and create a new transaction object.
         *
         * @return the transaction object with the fields set in the transactionBuilder
         */
        public Transaction build() {
            return new Transaction(this);
        }
    }
}