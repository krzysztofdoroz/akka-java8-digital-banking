package com.gft.digitalbank.exchange.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.InvalidActorNameException;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.gft.digitalbank.exchange.actors.messages.*;
import com.gft.digitalbank.exchange.domain.CancellationOrder;
import com.gft.digitalbank.exchange.domain.ModificationOrder;
import com.gft.digitalbank.exchange.domain.Order;
import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.Transaction;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;

/**
 * Created by krzysztof on 31/07/16.
 */
public class ParentDispatcherActor extends AbstractLoggingActor {

    public static final String ALL_CHILDREN = "*";
    private Set<String> products = new HashSet<>();
    private final int shutdownTimeoutInMs;
    private int currentOrderNumber = 1;
    private NavigableSet<Order> pendingOrders = new TreeSet<Order>((o1, o2) -> Integer.valueOf(o1.getOrderId()).compareTo(Integer.valueOf(o2.getOrderId())));
    private NavigableSet<ModificationOrder> pendingModificationOrders = new TreeSet<ModificationOrder>((o1, o2) -> Integer.valueOf(o1.getOrderId()).compareTo(Integer.valueOf(o2.getOrderId())));
    private NavigableSet<CancellationOrder> pendingCancellationOrders = new TreeSet<CancellationOrder>((o1, o2) -> Integer.valueOf(o1.getOrderId()).compareTo(Integer.valueOf(o2.getOrderId())));

    public ParentDispatcherActor(int shutdownTimeoutInMs) {
        this.shutdownTimeoutInMs = shutdownTimeoutInMs;

        receive(ReceiveBuilder.
                        match(Order.class, order -> {
                            if (order.getOrderId() == currentOrderNumber) {
                                log().debug("forwarding message to engine..." + order);
                                context().actorSelection(order.getProduct()).tell(order, self());
                            } else {
                                log().info("message out of order, buffered for now");
                                pendingOrders.add(order);
                            }
                        }).
                        matchEquals(UtilityMessages.ACK, ack -> {
                            log().debug("received ACK...");

                            currentOrderNumber++;
                            // maybe one of previously buffered messages can be forwarded now...
                            if (!pendingOrders.isEmpty() && pendingOrders.first().getOrderId() == currentOrderNumber) {
                                Order order = pendingOrders.pollFirst();
                                context().actorSelection(order.getProduct()).tell(order, self());
                            } else if (!pendingModificationOrders.isEmpty() && pendingModificationOrders.first().getOrderId() == currentOrderNumber) {
                                ModificationOrder modificationOrder = pendingModificationOrders.pollFirst();
                                context().actorSelection(ALL_CHILDREN).tell(modificationOrder, self());
                            } else if (!pendingCancellationOrders.isEmpty() && pendingCancellationOrders.first().getOrderId() == currentOrderNumber) {
                                CancellationOrder cancellationOrder = pendingCancellationOrders.pollFirst();
                                context().actorSelection(ALL_CHILDREN).tell(cancellationOrder, self());
                            }
                        }).
                        match(ModificationOrder.class, modOrder -> {
                            if (modOrder.getOrderId() == currentOrderNumber) {
                                log().info("Sending modification order.." + modOrder);
                                context().actorSelection(ALL_CHILDREN).tell(modOrder, self());
                            } else {
                                // out of order message, buffer for now
                                pendingModificationOrders.add(modOrder);
                            }

                        }).
                        match(CancellationOrder.class, cancellationOrder -> {
                            if (cancellationOrder.getOrderId() == currentOrderNumber) {
                                log().info("Sending cancellation order.." + cancellationOrder);
                                context().actorSelection(ALL_CHILDREN).tell(cancellationOrder, self());
                            } else {
                                // out of order message, buffer for now
                                pendingCancellationOrders.add(cancellationOrder);
                            }
                        }).
                        match(ActorCreationRequest.class, creationRequest -> {
                            log().info("creating new engine actor");
                            Order order = creationRequest.getOrder();

                            // keep track of products
                            products.add(order.getProduct());

                            try {
                                context()
                                        .actorOf(Props.create(TransactionEngineActor.class, order.getProduct()),
                                                order.getProduct());
                            } catch (InvalidActorNameException ex) {
                                log().error("actor creation failed - there is already an actor with this path" + ex);
                            }

                            context().actorSelection(order.getProduct()).tell(order, self());

                        }).
                        match(Shutdown.class, s -> {
                            log().info(new Date() + ":SHUTTING DOWN PARENT from:" + sender());

                            if (s.getTotal() == currentOrderNumber - 1) {
                                sendResults(sender());
                            } else {
                                context().system().scheduler().scheduleOnce(Duration.create(shutdownTimeoutInMs, TimeUnit.MILLISECONDS), self(),
                                        new ForceShutdown(s.getTotal(), sender()), context().dispatcher(), null);
                            }

                        }).
                        match(ForceShutdown.class, s -> {
                            log().warning(new Date() + ": forcing shutdown");
                            clearAllPendingMessages();
                            sendResults(s.getDestination());
                        }).
                        build()
        );
    }

    private void clearAllPendingMessages() {
        // first clear all pending messages
        NavigableMap<Integer, Object> pendingMessagesInAscendingOrd = new TreeMap<Integer, Object>();
        if (!pendingOrders.isEmpty()) {
            for (Order order : pendingOrders) {
                pendingMessagesInAscendingOrd.put(order.getOrderId(), order);
            }
        }
        if (!pendingModificationOrders.isEmpty()) {
            for (ModificationOrder modificationOrder : pendingModificationOrders) {
                pendingMessagesInAscendingOrd.put(modificationOrder.getOrderId(), modificationOrder);
            }
        }
        if (!pendingCancellationOrders.isEmpty()) {
            for (CancellationOrder cancellationOrder : pendingCancellationOrders) {
                pendingMessagesInAscendingOrd.put(cancellationOrder.getOrderId(), cancellationOrder);
            }
        }

        log().info(pendingMessagesInAscendingOrd.size() + " messages will be forwarded");

        while (!pendingMessagesInAscendingOrd.isEmpty()) {
            Map.Entry<Integer, Object> entry = pendingMessagesInAscendingOrd.pollFirstEntry();
            if (entry.getValue() instanceof Order) {
                Order order = (Order) entry.getValue();
                context().actorSelection(order.getProduct()).tell(order, self());
            } else {
                context().actorSelection(ALL_CHILDREN).tell(entry.getValue(), self());
            }
        }
    }

    private void sendResults(final ActorRef destination) throws Exception {
        Set<OrderBook> orderBooks = new HashSet<>();
        Set<Transaction> transactions = new HashSet<>();

        for (String product : products) {
            Future<?> result = ask(context().actorSelection(product), UtilityMessages.SHUTDOWN, shutdownTimeoutInMs);
            PartialResult r = (PartialResult) Await.result(result, Duration.Inf());
            log().info("PARTIAL RESULT:" + r);
            if (r.getOrderBook().isPresent()) {
                orderBooks.add(r.getOrderBook().get());
            }
            transactions.addAll(r.getTransactions());
        }

        BooksAndTransactions booksAndTransactions = new BooksAndTransactions(orderBooks, transactions);

        log().info("RESULT:" + booksAndTransactions);
        destination.tell(booksAndTransactions, ActorRef.noSender());
    }

}
