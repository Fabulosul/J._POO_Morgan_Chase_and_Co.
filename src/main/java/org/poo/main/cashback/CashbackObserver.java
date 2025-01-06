package org.poo.main.cashback;

import org.poo.main.bank.BankAccount;
import org.poo.main.bank.User;

public interface CashbackObserver {
    void update(PaymentDetails paymentDetails);
}