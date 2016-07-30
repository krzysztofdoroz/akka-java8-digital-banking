package com.gft.digitalbank.exchange.engine;

import com.gft.digitalbank.exchange.domain.CancellationOrder;
import com.gft.digitalbank.exchange.domain.ModificationOrder;
import com.gft.digitalbank.exchange.domain.Order;
import com.gft.digitalbank.exchange.model.OrderBook;

import java.util.Optional;

/**
 * Created by krzysztof on 23/07/16.
 */
public interface MatchingEngine {
    void processOrder(Order order);
    void cancelOrder(CancellationOrder cancellationOrder);
    void modifyOrder(ModificationOrder modificationOrder);
    Optional<OrderBook> getOrderBook();

}
