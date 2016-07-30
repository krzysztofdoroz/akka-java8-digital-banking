package com.gft.digitalbank.exchange.engine;

import com.gft.digitalbank.exchange.model.Transaction;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by krzysztof on 24/07/16.
 */
public class TransactionRegisterImpl implements TransactionRegister {

    private Set<Transaction> transactions = new HashSet<>();

    @Override
    public void register(Transaction tx) {
         transactions.add(tx);
    }

    @Override
    public Set<Transaction> getTransactions() {
        return transactions;
    }
}
