package org.poo.main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bank.BankAccount;
import org.poo.main.bank.Card;
import org.poo.main.bank.User;
import org.poo.main.bank.Transaction;
import org.poo.main.cashback.Commerciant;
import org.poo.main.cashback.PaymentDetails;


@Getter
@Setter
public final class PayOnline extends Command implements CommandInterface {
    private Bank bank;
    private ArrayNode output;

    public PayOnline(final Bank bank, final CommandInput command, final ArrayNode output) {
        super(command);
        this.bank = bank;
        this.output = output;
    }

    /**
     * Method overridden from the CommandInterface to make an online payment with a card.
     * It gets the user by email, the card by card number and the bank account by card number
     * by calling appropriate methods from the bank.
     * Then, it checks if the card is frozen and if it is, it registers a transaction with the
     * description "The card is frozen".
     * If the card is not frozen, it tries to make the payment by calling the payOnline method
     * from the bank account which returns a boolean indicating if the payment was
     * successful or not. If the payment is successful, it registers a transaction with the
     * description "Card payment", otherwise it registers a transaction with the description
     * "Insufficient funds".
     * If the card used to make the payment is a oneTime card, that card is destroyed,
     * a new card is generated and both of these actions are registered as transactions in the
     * user and bank account transaction lists.
     * During this whole process, if the user, the bank account or the card associated with the
     * card number are not found, the method returns without doing anything.
     */
    @Override
    public void execute() {
        User user = bank.getUserByMail(getEmail());
        if (user == null) {
            return;
        }
        Card card = bank.getCardByCardNr(getCardNumber());
        if (card == null) {
            addErrorToOutput();
            return;
        }
        BankAccount bankAccount = bank.getAccountByCardNr(getCardNumber());
        if (bankAccount == null) {
            return;
        }

        if (card.isFrozen()) {
            registerTransaction(user, bankAccount, card, "The card is frozen");
            return;
        }
        if(getAmount() <= 0) {
            return;
        }
        boolean hasSufficientFunds = bankAccount.payOnline(bank, getAmount(), getCurrency());
        if (hasSufficientFunds) {
            registerTransaction(user, bankAccount, card, "Card payment");
            if (card.isOneTimeCard()) {
                bankAccount.removeCard(card);
                registerTransaction(user, bankAccount, card,
                        "The card has been destroyed");

                Card newCard = new Card("oneTime");
                bankAccount.addCard(newCard);
                registerTransaction(user, bankAccount, newCard, "New card created");
            }
            Commerciant commerciant = bank.getCommerciantByName(getCommerciant());
            PaymentDetails paymentDetails = new PaymentDetails(getAmount(), getCurrency(), commerciant);
            bankAccount.notifyCashbackObservers(paymentDetails);
        } else {
            registerTransaction(user, bankAccount, card, "Insufficient funds");
        }
    }

    /**
     * Method used to register a transaction based on a given description.
     * The method creates a transaction object based on the description and adds it
     * to the corresponding user or bank account transaction lists and sometimes both.
     *
     * @param user -> the user that the transaction is registered to
     * @param bankAccount -> the bank account that the transaction is registered to
     * @param card -> the card that the transaction is registered to
     * @param description -> the description of the transaction used to determine the type of
     *                    transaction that is registered
     */
    private void registerTransaction(final User user, final BankAccount bankAccount,
                                    final Card card, final String description) {
        Transaction transaction;
        switch (description) {
            case "The card is frozen":
                transaction = new Transaction
                        .TransactionBuilder(getTimestamp(), description)
                        .build();
                user.addTransaction(transaction);
                break;
            case "Card payment":
                double convertedAmount = bank.convertCurrency(getAmount(), getCurrency(),
                        bankAccount.getCurrency());
                transaction = new Transaction
                        .TransactionBuilder(getTimestamp(), description)
                        .amount(convertedAmount)
                        .commerciant(getCommerciant())
                        .build();
                user.addTransaction(transaction);
                bankAccount.addTransaction(transaction);
                break;
            case "The card has been destroyed":
                transaction = new Transaction
                        .TransactionBuilder(getTimestamp(), description)
                        .cardHolder(getEmail())
                        .card(card.getCardNumber())
                        .account(bankAccount.getIban())
                        .build();
                user.addTransaction(transaction);
                break;
            case "New card created":
                transaction = new Transaction
                        .TransactionBuilder(getTimestamp(), description)
                        .account(bankAccount.getIban())
                        .cardHolder(getEmail())
                        .card(card.getCardNumber())
                        .build();
                user.addTransaction(transaction);
                break;
                case "Insufficient funds":
                    transaction = new Transaction
                            .TransactionBuilder(getTimestamp(), description)
                            .build();
                    user.addTransaction(transaction);
                    bankAccount.addTransaction(transaction);
                    break;
            default:
                break;
        }
    }

    /**
     * Method used to add an error message to the output array when the
     * card on which the payment is made is not found.
     * It creates an object node with the wanted information and adds it to the output array.
     */
    private void addErrorToOutput() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("command", "payOnline");

        ObjectNode outputNode = mapper.createObjectNode();
        outputNode.put("description", "Card not found");
        outputNode.put("timestamp", getTimestamp());

        objectNode.set("output", outputNode);

        objectNode.put("timestamp", getTimestamp());

        output.add(objectNode);
    }
}
