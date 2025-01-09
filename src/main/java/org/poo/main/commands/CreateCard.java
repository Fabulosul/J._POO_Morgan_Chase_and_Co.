package org.poo.main.commands;

import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bank.BankAccount;
import org.poo.main.bank.Card;
import org.poo.main.bank.User;
import org.poo.main.bank.Transaction;

@Getter
@Setter
public final class CreateCard extends Command implements CommandInterface {
    private Bank bank;
    private String cardType;

    public CreateCard(final Bank bank, final CommandInput command,
                      final String cardType) {
        super(command);
        this.bank = bank;
        this.cardType = cardType;
    }

    /**
     * Method overridden from the CommandInterface to create a new card and add it
     * to a user account.
     * It gets the user by email and the bank account by iban, then creates a new classic card
     * and adds it to the bank account.
     * At the end, it registers the "New card created" transaction to the user and bank account.
     * If the user or the bank account is not found, the method terminates.
     */
    @Override
    public void execute() {
        User user = bank.getUserByMail(getEmail());
        if (user == null) {
            return;
        }
        BankAccount bankAccount = user.getAccountByIban(getAccount());
        if (bankAccount == null) {
            return;
        }

        Card card = new Card(cardType);
        bankAccount.addCard(card, user);
        registerTransaction(user, bankAccount, card.getCardNumber());
    }

    /**
     * Method used to register the new card creation transaction to the user
     * and bank account.
     * It creates a new transaction with the current timestamp and the message "New card created"
     * and all the other important fields to the transaction and adds it to the user and
     * bank account.
     *
     * @param user -> the user to add the transaction to
     * @param bankAccount -> the bank account to add the transaction to
     * @param cardNr -> the card number added to the transaction information
     */
    private void registerTransaction(final User user, final BankAccount bankAccount,
                                    final String cardNr) {
        Transaction transaction = new Transaction
                .TransactionBuilder(getTimestamp(), "New card created")
                .card(cardNr)
                .cardHolder(getEmail())
                .account(bankAccount.getIban())
                .build();
        user.addTransaction(transaction);
        bankAccount.addTransaction(transaction);
    }
}
