package org.poo.main.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.bank.Bank;
import org.poo.main.bank.User;

@Getter
@Setter
public class AcceptSplitPayment extends Command implements CommandInterface {
    private Bank bank;

    public AcceptSplitPayment(final Bank bank, final CommandInput command,
                              final ArrayNode output) {
        super(command);
        this.bank = bank;
    }

    @Override
    public void execute() {
        User user = bank.getUserByMail(getEmail());
        if (user == null) {
            return;
        }
        bank.acceptSplitPayment(user, getSplitPaymentType());
    }
}