package com.gft.digitalbank.exchange.engine;

import com.gft.digitalbank.exchange.domain.CancellationOrder;
import com.gft.digitalbank.exchange.domain.ModificationOrder;
import com.gft.digitalbank.exchange.domain.Order;
import com.gft.digitalbank.exchange.domain.Side;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Created by krzysztof on 23/07/16.
 */
public class OrderBookSideImpl implements OrderBookSide {

    private final Map<Pair<Integer, String>, Order> orderIdAndBrokerToOrder;
    private final NavigableSet<Order> orders;

    public OrderBookSideImpl(final Side side) {
        if (side == Side.BUY){
            orders = new TreeSet<>(new BuySideComparator());
        }  else {
            orders = new TreeSet<>(new SellSideComparator());
        }
        this.orderIdAndBrokerToOrder = new HashMap<>();
    }

    @Override
    public void addOrder(final Order order) {
        orderIdAndBrokerToOrder.put(Pair.of(order.getOrderId(), order.getBroker()), order);
        orders.add(order);
    }

    @Override
    public List<Order> getOrders() {
        return new LinkedList(orders);
    }

    @Override
    public boolean cancelOrder(CancellationOrder cancellationOrder) {
        boolean cancelled = false;
        Pair<Integer, String> orderIdAndBroker = Pair.of(cancellationOrder.getCancelledOrderId(),
                                                         cancellationOrder.getBroker());
        Order orderToBeCancelled = orderIdAndBrokerToOrder.get(orderIdAndBroker);
        if (orderToBeCancelled != null) {
            orders.remove(orderToBeCancelled);
            orderIdAndBrokerToOrder.remove(orderIdAndBroker);
            cancelled = true;
        }
        return cancelled;
    }

    @Override
    public boolean modifyOrder(ModificationOrder modificationOrder) {
        boolean modified = false;
        Pair<Integer, String> orderIdAndBroker = Pair.of(modificationOrder.getModifiedOrderId(),
                modificationOrder.getBroker());
        Order orderToBeModified = orderIdAndBrokerToOrder.get(orderIdAndBroker);
        if (orderToBeModified != null) {
            orders.remove(orderToBeModified);
            Order updatedOrder = new Order(orderToBeModified.getOrderId(),
                    orderToBeModified.getProduct(),
                    orderToBeModified.getSide(),
                    modificationOrder.getPrice(),
                    modificationOrder.getTimestamp(),
                    modificationOrder.getAmount(),
                    orderToBeModified.getBroker(),
                    orderToBeModified.getClient());
            orders.add(updatedOrder);
            orderIdAndBrokerToOrder.put(orderIdAndBroker, updatedOrder);
            modified = true;
        }
        return modified;
    }

    @Override
    public Optional<Order> getTopOrder() {
        if (orders.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(orders.first());
        }
    }

    @Override
    public Order pollTopOrder() {
        Order top = orders.pollFirst();
        // keep orderIdAndBroker in sync
        orderIdAndBrokerToOrder.remove(Pair.of(top.getOrderId(), top.getBroker()));
        return top;
    }

    private class BuySideComparator implements Comparator<Order> {
        @Override
        public int compare(Order o1, Order o2) {
            if (o1.getOrderId() == o2.getOrderId()) {
                return 0;
            } else if (o1.getPrice() > o2.getPrice()) {
                return -1;
            } else if (o1.getPrice() == o2.getPrice()) {
                if (o1.getTimestamp() < o2.getTimestamp()) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                return 1;
            }
        }
    }

    private class SellSideComparator implements Comparator<Order> {
        @Override
        public int compare(Order o1, Order o2) {
            if (o1.getOrderId() == o2.getOrderId()) {
                return 0;
            } else if (o1.getPrice() > o2.getPrice()) {
                return 1;
            } else if (o1.getPrice() == o2.getPrice()) {
                if (o1.getTimestamp() < o2.getTimestamp()) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                return -1;
            }
        }
    }
}

