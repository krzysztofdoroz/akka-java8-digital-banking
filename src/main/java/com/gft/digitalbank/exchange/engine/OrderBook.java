package com.gft.digitalbank.exchange.engine;

import com.gft.digitalbank.exchange.domain.CancellationOrder;
import com.gft.digitalbank.exchange.domain.ModificationOrder;
import com.gft.digitalbank.exchange.domain.Order;

import java.util.List;

/**
 * Created by krzysztof on 23/07/16.
 */
public interface OrderBook {
    void addOrder(Order order);
    List<Order> getOrders();
    void cancelOrder(CancellationOrder cancellationOrder);
    void modifyOrder(ModificationOrder modificationOrder);
    Order getTopOrder();
}
