package com.gft.digitalbank.exchange.engine;

import com.gft.digitalbank.exchange.model.Transaction;

import java.util.Set;

/**
 * Created by krzysztof on 24/07/16.
 */
public interface TransactionRegister {
    void register(Transaction tx);
    Set<Transaction> getTransactions();
}
