package com.gft.digitalbank.exchange.engine;

import com.gft.digitalbank.exchange.domain.CancellationOrder;
import com.gft.digitalbank.exchange.domain.ModificationOrder;
import com.gft.digitalbank.exchange.domain.Order;
import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.Transaction;

import java.util.Optional;
import java.util.Set;

/**
 * Created by krzysztof on 23/07/16.
 */
public interface MatchingEngine {
    void processOrder(Order order);
    boolean cancelOrder(CancellationOrder cancellationOrder);
    boolean modifyOrder(ModificationOrder modificationOrder);
    Optional<OrderBook> getOrderBook();
    Set<Transaction> getTransactions();
}
