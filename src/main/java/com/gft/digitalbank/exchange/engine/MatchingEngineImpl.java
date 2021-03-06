package com.gft.digitalbank.exchange.engine;

import com.gft.digitalbank.exchange.domain.*;
import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.OrderEntry;
import com.gft.digitalbank.exchange.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by krzysztof on 23/07/16.
 */
public class MatchingEngineImpl implements MatchingEngine {

    private final static Logger LOG = LoggerFactory.getLogger(MatchingEngineImpl.class);

    private final String product;
    private final OrderBookSide buySideOrderBook;
    private final OrderBookSide sellSideOrderBook;
    private final TransactionRegister transactionRegister;
    private int transactionId = 1;

    public MatchingEngineImpl(String product) {
        this.product = product;
        this.buySideOrderBook = new OrderBookSideImpl(Side.BUY);
        this.sellSideOrderBook = new OrderBookSideImpl(Side.SELL);
        this.transactionRegister = new TransactionRegisterImpl();
    }

    public MatchingEngineImpl(String product, TransactionRegister transactionRegister) {
        this.product = product;
        this.buySideOrderBook = new OrderBookSideImpl(Side.BUY);
        this.sellSideOrderBook = new OrderBookSideImpl(Side.SELL);
        this.transactionRegister = transactionRegister;
    }

    @Override
    public void processOrder(Order order) {
        addOrder(order);
    }

    private void addOrder(final Order order) {
        switch (order.getSide()) {
            case BUY:
                buySideOrderBook.addOrder(order);
                break;
            case SELL:
                sellSideOrderBook.addOrder(order);
                break;
            default:
                LOG.error("Unrecognized size:" + order.getSide());
        }
        tryMatching();
    }

    @Override
    public boolean cancelOrder(CancellationOrder cancellationOrder) {
        return
                buySideOrderBook.cancelOrder(cancellationOrder) ||
                        sellSideOrderBook.cancelOrder(cancellationOrder);
    }

    @Override
    public boolean modifyOrder(final ModificationOrder modificationOrder) {
        boolean modified = buySideOrderBook.modifyOrder(modificationOrder) || sellSideOrderBook.modifyOrder(modificationOrder);

        if (modified) {
            tryMatching();
        }

        return modified;
    }

    @Override
    public Optional<OrderBook> getOrderBook() {

        List<OrderEntry> buySideEntries = transformOrdersToOrderEntries(buySideOrderBook.getOrders());
        List<OrderEntry> sellSideEntries = transformOrdersToOrderEntries(sellSideOrderBook.getOrders());

        if (buySideEntries.isEmpty() && sellSideEntries.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(new OrderBook(product, buySideEntries, sellSideEntries));
        }
    }

    @Override
    public Set<Transaction> getTransactions() {
        return transactionRegister.getTransactions();
    }

    private void tryMatching() {

        boolean stillMatching = true;

        while (stillMatching) {
            // get orders from both sides
            Optional<Order> buySideOrder = buySideOrderBook.getTopOrder();
            Optional<Order> sellSideOrder = sellSideOrderBook.getTopOrder();

            if (buySideOrder.isPresent() && sellSideOrder.isPresent()) {

                if (buySideOrder.get().getPrice() >= sellSideOrder.get().getPrice()) {

                    Order buySide = buySideOrderBook.pollTopOrder();
                    Order sellSide = sellSideOrderBook.pollTopOrder();

                    int txPrice = findTxPrice(buySide, sellSide);
                    int txAmount = Math.min(buySide.getAmount(), sellSide.getAmount());

                    Transaction tx = new Transaction(transactionId++, txAmount, txPrice, product,
                            buySide.getBroker(), sellSide.getBroker(), buySide.getClient(), sellSide.getClient());
                    transactionRegister.register(tx);

                    addPartialOrders(buySide, sellSide, txAmount);
                } else {
                    stillMatching = false;
                }
            } else {
                stillMatching = false;
            }
        }
    }

    private void addPartialOrders(Order buySide, Order sellSide, int txAmount) {
        if (txAmount < buySide.getAmount()) {
            // partial order
            addPartialOrder(buySideOrderBook, buySide, txAmount);
        }
        if (txAmount < sellSide.getAmount()) {
            // partial order
            addPartialOrder(sellSideOrderBook, sellSide, txAmount);
        }
    }

    private void addPartialOrder(OrderBookSide orderBookSide, Order oldOrder, int txAmount) {
        orderBookSide.addOrder(new Order(oldOrder.getOrderId(),oldOrder.getProduct(), oldOrder.getSide(),
                oldOrder.getPrice(), oldOrder.getTimestamp(), oldOrder.getAmount() - txAmount, oldOrder.getBroker(), oldOrder.getClient()));
    }

    private int findTxPrice(final Order buySideOrder, final Order sellSideOrder) {
        if (buySideOrder.getTimestamp() > sellSideOrder.getTimestamp()) {
            return sellSideOrder.getPrice();
        } else {
            return buySideOrder.getPrice();
        }
    }

    private List<OrderEntry> transformOrdersToOrderEntries(final List<Order> orders) {
        int index = 1;
        List<OrderEntry> entries = new LinkedList<>();

        for (Order ord : orders) {
            entries.add(new OrderEntry(index++, ord.getBroker(), ord.getAmount(), ord.getPrice(), ord.getClient()));
        }

        return entries;
    }

}
