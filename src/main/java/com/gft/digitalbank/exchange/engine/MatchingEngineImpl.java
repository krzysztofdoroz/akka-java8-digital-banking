package com.gft.digitalbank.exchange.engine;

import com.gft.digitalbank.exchange.domain.CancellationOrder;
import com.gft.digitalbank.exchange.domain.ModificationOrder;
import com.gft.digitalbank.exchange.domain.Order;
import com.gft.digitalbank.exchange.domain.Side;
import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.OrderEntry;
import com.gft.digitalbank.exchange.model.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by krzysztof on 23/07/16.
 */
public class MatchingEngineImpl implements MatchingEngine {

    private final String product;
    private final OrderBookSide buySideOrderBook;
    private final OrderBookSide sellSideOrderBook;
    private final TransactionRegister transactionRegister;
    private int transactionId = 1;

    public MatchingEngineImpl(String product, TransactionRegister transactionRegister) {
        this.product = product;
        this.buySideOrderBook = new OrderBookSideImpl(Side.BUY);
        this.sellSideOrderBook = new OrderBookSideImpl(Side.SELL);
        this.transactionRegister = transactionRegister;
    }

    @Override
    public void processOrder(Order order) {
        switch (order.getSide()) {
            case BUY:
                buySideOrderBook.addOrder(order);
                break;
            case SELL:
                sellSideOrderBook.addOrder(order);
                break;
        }

        tryMatching();
    }

    @Override
    public void cancelOrder(CancellationOrder cancellationOrder) {

    }

    @Override
    public void modifyOrder(ModificationOrder modificationOrder) {

    }

    @Override
    public OrderBook getOrderBook() {
        // TODO: refactor repeated logic
        List<OrderEntry> buySideEntries = buySideOrderBook.getOrders()
                .stream()
                .map(ord -> new OrderEntry(ord.getOrderId(), ord.getBroker(), ord.getAmount(), ord.getPrice(), ord.getClient()))
                .collect(Collectors.toList());
        List<OrderEntry> sellSideEntries = sellSideOrderBook.getOrders()
                .stream()
                .map(ord -> new OrderEntry(ord.getOrderId(), ord.getBroker(), ord.getAmount(), ord.getPrice(), ord.getClient()))
                .collect(Collectors.toList());

        return new OrderBook(product, buySideEntries, sellSideEntries);
    }

    private void tryMatching() {

        boolean stillMatching = true;

        while(stillMatching) {
            // get orders from both sides
            Optional<Order> buySideOrder = buySideOrderBook.getTopOrder();
            Optional<Order> sellSideOrder = sellSideOrderBook.getTopOrder();

            // TODO: flatmap instead?
            if (buySideOrder.isPresent() && sellSideOrder.isPresent()) {

                if (buySideOrder.get().getPrice() >= sellSideOrder.get().getPrice()) {

                    Order buySide = buySideOrderBook.pollTopOrder();
                    Order sellSide = sellSideOrderBook.pollTopOrder();

                    int txPrice = Math.min(buySide.getPrice(), sellSide.getPrice());
                    int txAmount = Math.min(buySide.getAmount(), sellSide.getAmount());

                    Transaction tx = new Transaction(transactionId++, txAmount, txPrice, product,
                            buySide.getBroker(), sellSide.getBroker(), buySide.getClient(), sellSide.getClient());
                    transactionRegister.register(tx);

                    if (txAmount < buySide.getAmount()) {
                        // partial order
                        buySideOrderBook.addOrder(new Order(buySide.getOrderId(), "A", buySide.getSide(), buySide.getPrice(),
                                buySide.getTimestamp(), buySide.getAmount() - txAmount, buySide.getBroker(), buySide.getClient()));
                    }
                    if (txAmount < sellSide.getAmount()) {
                        // partial order
                        sellSideOrderBook.addOrder(new Order(buySide.getOrderId(), "A", buySide.getSide(), buySide.getPrice(),
                                buySide.getTimestamp(), buySide.getAmount() - txAmount, buySide.getBroker(), buySide.getClient()));
                    }

                } else {
                    stillMatching = false;
                }
            } else {
                stillMatching = false;
            }
        }
    }
}
