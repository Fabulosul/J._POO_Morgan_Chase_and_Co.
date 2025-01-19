package org.poo.main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.fileio.CommerciantInput;
import org.poo.main.bank.*;
import org.poo.main.cashback.Commerciant;
import org.poo.main.cashback.PaymentDetails;

@Getter
@Setter
public final class SendMoney extends Command implements CommandInterface {
    private Bank bank;
    private ArrayNode output;

    public SendMoney(final Bank bank, final CommandInput command, final ArrayNode output) {
        super(command);
        this.bank = bank;
        this.output = output;
    }

    /**
     * Method overridden from the CommandInterface to transfer money between two accounts.
     * It gets the sender and receiver accounts and checks if the sender has enough funds
     * to make the transfer. If the sender has enough funds, the transfer transaction is
     * registered in the sender's and receiver's transaction history.
     * If the sender does not have enough funds, an error transaction is registered in the
     * sender's transaction report.
     * Also, if the sender, sender's account or receiver's account are not found, the method
     * returns without executing the transfer.
     *
     * @implNote The method checks first if the receiver is an alias and then if it is an IBAN
     * because the search by alias is faster due to the use of a HashMap.
     */
    @Override
    public void execute() {
        if(getReceiver().isEmpty()) {
            addErrorToOutput("User not found");
            return;
        }
        User sender = bank.getUserByMail(getEmail());
        if (sender == null) {
            return;
        }
        BankAccount senderAccount = sender.getAccountByIban(getAccount());
        if (senderAccount == null) {
            return;
        }

        User receiver;
        BankAccount receiverAccount;
        if (bank.findAccountByAlias(getReceiver()) != null) {
            receiverAccount = bank.findAccountByAlias(getReceiver());
        } else {
            receiverAccount = bank.findAccountByIban(getReceiver());
        }
        if (receiverAccount == null) {
            handleCommerciantCase(sender, senderAccount, getReceiver());
            return;
        }
        receiver = bank.getUserByAccount(getReceiver());
        if (receiver == null) {
            return;
        }

        boolean hasSufficientFunds =
                senderAccount.sendMoney(bank, receiverAccount, getAmount());
        if (!hasSufficientFunds) {
            registerTransactionError(sender, senderAccount);
            return;
        }
        registerSenderTransaction(sender, senderAccount, receiverAccount);
        registerReceiverTransaction(receiver, receiverAccount, senderAccount);
        if (receiverAccount.getAccountType().equals("business")) {
            Transaction.TransactionBuilder transactionBuilder = new Transaction
                    .TransactionBuilder(getTimestamp(), "spend")
                    .amount(getAmount())
                    .username(sender.getLastName() + " " + sender.getFirstName());
            Transaction businessTransaction = transactionBuilder.build();
            ((BusinessAccount) receiverAccount).addBusinessTransaction(businessTransaction);
        }
        if(sender.getServicePlan().getPlanName().equals("silver")) {
            sender.setUpgradeCounter(sender.getUpgradeCounter() + 1);
            if(sender.getUpgradeCounter() == 5) {
                sender.changeServicePlan("gold");
            }
        }
    }

    public void handleCommerciantCase(User sender, BankAccount senderAccount, String commerciantIban) {
        String commmerciantName = null;
        for (CommerciantInput commerciant : bank.getCommerciants()) {
            if (commerciant.getAccount().equals(commerciantIban)) {
                commmerciantName = commerciant.getCommerciant();
                break;
            }
        }
        if (commmerciantName == null) {
            return;
        }
       for (Commerciant commerciant : senderAccount.getCommerciants()) {
            if (commerciant.getName().equals(commmerciantName)) {
                senderAccount.payOnline(bank, getAmount(), senderAccount.getCurrency());
                PaymentDetails paymentDetails = new PaymentDetails(getAmount(), senderAccount.getCurrency(),
                        commerciant, sender);
                senderAccount.notifyCashbackObservers(paymentDetails);
                Transaction senderTransaction = new Transaction
                        .TransactionBuilder(getTimestamp(), getDescription())
                        .senderIban(getAccount())
                        .receiverIban(commerciantIban)
                        .amount(getAmount())
                        .currency(senderAccount.getCurrency())
                        .transferType("sent")
                        .build();
                senderAccount.addTransaction(senderTransaction);
                sender.addTransaction(senderTransaction);
                double amountInRon = bank.convertCurrency(getAmount(), senderAccount.getCurrency(),
                        "RON");
                if(sender.getServicePlan().getPlanName().equals("silver")
                        && amountInRon >= 300) {
                    sender.setUpgradeCounter(sender.getUpgradeCounter() + 1);
                    if(sender.getUpgradeCounter() == 5) {
                        sender.changeServicePlan("gold");
                        Transaction transaction = new Transaction
                                .TransactionBuilder(getTimestamp(), "Upgrade plan")
                                .accountIban(senderAccount.getIban())
                                .newPlanType("gold")
                                .build();
                        sender.addTransaction(transaction);
                        senderAccount.addTransaction(transaction);
                    }
                }
                return;
            }
       }
    }

    /**
     * Method used to register the time when the sender does not have enough funds
     * to make the transfer.
     * It creates a transaction with the current timestamp and the description "Insufficient funds"
     * and adds it to the sender's and sender's account transaction ArrayNode.
     *
     * @param user -> the sender user
     * @param bankAccount -> the sender bank account
     */
    private void registerTransactionError(final User user, final BankAccount bankAccount) {
        Transaction transaction = new Transaction
                .TransactionBuilder(getTimestamp(), "Insufficient funds")
                .build();
        user.addTransaction(transaction);
        bankAccount.addTransaction(transaction);
    }

    /**
     * Method used to register the transaction of a successful transfer from the sender's
     * point of view.
     * It creates a transaction with the current timestamp, the description of the transfer,
     * the sender's IBAN, the receiver's IBAN, the amount of money transferred, the currency of
     * the sender's account and the transfer type "sent" and adds it to the sender and his account.
     *
     * @param sender -> the person who sends the moneys
     * @param senderAccount -> the account of the person who sends the money
     * @param receiverAccount -> the account of the person who receives the money
     */
    private void registerSenderTransaction(final User sender, final BankAccount senderAccount,
                                              final BankAccount receiverAccount) {
        Transaction senderTransaction = new Transaction
                .TransactionBuilder(getTimestamp(), getDescription())
                .senderIban(getAccount())
                .receiverIban(receiverAccount.getIban())
                .amount(getAmount())
                .currency(senderAccount.getCurrency())
                .transferType("sent")
                .build();
        sender.addTransaction(senderTransaction);
        senderAccount.addTransaction(senderTransaction);
    }

    /**
     * Method used to register the transaction of a successful transfer from the receiver's
     * point of view.
     * It creates a transaction with the current timestamp, the description of the transfer,
     * the sender's IBAN, the receiver's IBAN, the amount of money transferred, the currency of
     * the receiver's account and the transfer type "received" and adds it to the receiver
     * and his account.
     * It also converts the amount of money in the receiver's currency if the currency
     * of the sender's account is different from the currency of the receiver's account.
     *
     * @param receiver -> the person who receives the money
     * @param receiverAccount -> the account of the person who receives the money
     * @param senderAccount -> the account of the person who sends the money
     */
    private void registerReceiverTransaction(final User receiver, final BankAccount receiverAccount,
                                                final BankAccount senderAccount) {
        double convertedAmount = bank.convertCurrency(getAmount(), senderAccount.getCurrency(),
                receiverAccount.getCurrency());
        Transaction receiverTransaction = new Transaction
                .TransactionBuilder(getTimestamp(), getDescription())
                .senderIban(getAccount())
                .receiverIban(receiverAccount.getIban())
                .amount(convertedAmount)
                .currency(receiverAccount.getCurrency())
                .transferType("received")
                .build();
        receiver.addTransaction(receiverTransaction);
        receiverAccount.addTransaction(receiverTransaction);
    }

    public void addErrorToOutput(String description) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("command", "sendMoney");

        ObjectNode outputNode = mapper.createObjectNode();
        outputNode.put("description", description);
        outputNode.put("timestamp", getTimestamp());

        objectNode.set("output", outputNode);
        objectNode.put("timestamp", getTimestamp());
        output.add(objectNode);
    }
}
