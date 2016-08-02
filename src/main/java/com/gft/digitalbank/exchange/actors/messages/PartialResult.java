package com.gft.digitalbank.exchange.actors.messages;

import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.Transaction;

import java.util.Optional;
import java.util.Set;

/**
 * Created by krzysztof on 31/07/16.
 */
public class PartialResult {

    private final Optional<OrderBook> orderBook;
    private final Set<Transaction> transactions;

    public PartialResult(Optional<OrderBook> orderBook, Set<Transaction> transactions) {
        this.orderBook = orderBook;
        this.transactions = transactions;
    }

    public Optional<OrderBook> getOrderBook() {
        return orderBook;
    }

    public Set<Transaction> getTransactions() {
        return transactions;
    }

    @Override
    public String toString() {
        return "PartialResult{" +
                "orderBook=" + orderBook +
                ", transactions=" + transactions +
                '}';
    }
}
