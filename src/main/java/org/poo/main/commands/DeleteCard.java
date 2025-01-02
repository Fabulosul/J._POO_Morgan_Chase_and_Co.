package org.poo.main.commands;

import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bank.BankAccount;
import org.poo.main.bank.Card;
import org.poo.main.bank.Transaction;
import org.poo.main.bank.User;

@Getter
@Setter
public final class DeleteCard extends Command implements CommandInterface {
    private Bank bank;

    public DeleteCard(final Bank bank, final CommandInput command) {
        super(command);
        this.bank = bank;
    }

    /**
     * Method overridden from the CommandInterface to delete a card associated with a bank account.
     * It gets the user by email, the card by card number and the bank account by card number
     * and then removes the card from the bank account and registers the transaction.
     * If the user, card or bank account is not found, the method terminates.
     */
    @Override
    public void execute() {
        User user = bank.getUserByMail(getEmail());
        if (user == null) {
            return;
        }
        Card card = bank.getCardByCardNr(getCardNumber());
        if (card == null) {
            return;
        }
        BankAccount bankAccount = bank.getAccountByCardNr(getCardNumber());
        if (bankAccount == null) {
            return;
        }

        bankAccount.removeCard(card);
        registerTransaction(user, bankAccount.getIban());
    }

    /**
     * Method used to register a card deletion transaction.
     * It creates a transaction with the information required to outline the deletion of a card
     * and then adds it to the user's transaction list.
     *
     * @param user -> the user that deletes the card
     * @param iban -> the iban of the bank account associated with the card
     */
    private void registerTransaction(final User user, final String iban) {
        Transaction transaction = new Transaction
                .TransactionBuilder(getTimestamp(),
                "The card has been destroyed")
                .card(getCardNumber())
                .account(iban)
                .cardHolder(getEmail())
                .build();
        user.addTransaction(transaction);
    }
}
