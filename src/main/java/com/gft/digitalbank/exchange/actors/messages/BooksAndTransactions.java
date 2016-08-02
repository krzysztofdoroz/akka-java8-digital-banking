package com.gft.digitalbank.exchange.actors.messages;

import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.Transaction;

import java.util.Set;

/**
 * Created by krzysztof on 31/07/16.
 */
public class BooksAndTransactions {

    private final Set<OrderBook> orderBooks;
    private final Set<Transaction> transactions;

    public BooksAndTransactions(Set<OrderBook> orderBooks, Set<Transaction> transactions) {
        this.orderBooks = orderBooks;
        this.transactions = transactions;
    }

    public Set<OrderBook> getOrderBooks() {
        return orderBooks;
    }

    public Set<Transaction> getTransactions() {
        return transactions;
    }

    @Override
    public String toString() {
        return "BooksAndTransactions{" +
                "orderBooks=" + orderBooks +
                ", transactions=" + transactions +
                '}';
    }
}
