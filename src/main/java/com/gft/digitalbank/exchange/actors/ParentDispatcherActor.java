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

    private Set<String> products = new HashSet<>();
    public static final String PATH = "/user/parent-dispatcher/";
    private static final int SHUTDOWN_TIMEOUT_IN_MS = 150;
    private int currentOrderNumber = 1;
    private int expectedAcks = 0;
    private NavigableSet<Order> pendingOrders = new TreeSet<>(new Comparator<Order>() {
        @Override
        public int compare(Order o1, Order o2) {
            return Integer.valueOf(o1.getOrderId()).compareTo(Integer.valueOf(o2.getOrderId()));
        }
    });
    private NavigableSet<ModificationOrder> pendingModificationOrders = new TreeSet<>(new Comparator<ModificationOrder>() {
        @Override
        public int compare(ModificationOrder o1, ModificationOrder o2) {
            return Integer.valueOf(o1.getOrderId()).compareTo(Integer.valueOf(o2.getOrderId()));
        }
    });
    private NavigableSet<CancellationOrder> pendingCancellationOrders = new TreeSet<>(new Comparator<CancellationOrder>() {
        @Override
        public int compare(CancellationOrder o1, CancellationOrder o2) {
            return Integer.valueOf(o1.getOrderId()).compareTo(Integer.valueOf(o2.getOrderId()));
        }
    });

    public ParentDispatcherActor() {
        receive(ReceiveBuilder.
                        match(Order.class, order -> {

                            if (order.getOrderId() == currentOrderNumber) {
                                log().debug("forwarding message to engine...|" + order);

                                context().actorSelection(PATH + order.getProduct()).tell(order, self());
                                expectedAcks++;
                            } else {
                                log().info("parked messaeg");
                                pendingOrders.add(order);
                            }
                        }).
                        match(Ack.class, ack -> {
                            log().debug("RECEIVED ACK...");

                            currentOrderNumber++;
                            if (!pendingOrders.isEmpty() && pendingOrders.first().getOrderId() == currentOrderNumber) {
                                Order order = pendingOrders.pollFirst();
                                context().actorSelection(PATH + order.getProduct()).tell(order, self());
                                expectedAcks++;

                            } else if (!pendingModificationOrders.isEmpty() && pendingModificationOrders.first().getOrderId() == currentOrderNumber) {
                                ModificationOrder modificationOrder = pendingModificationOrders.pollFirst();
                                context().actorSelection(PATH + "*").tell(modificationOrder, self());
                                expectedAcks++;
                            } else if (!pendingCancellationOrders.isEmpty() && pendingCancellationOrders.first().getOrderId() == currentOrderNumber) {
                                CancellationOrder cancellationOrder = pendingCancellationOrders.pollFirst();
                                context().actorSelection(PATH + "*").tell(cancellationOrder, self());
                                expectedAcks++;
                            }
                        }).
                        match(ModificationOrder.class, modOrder -> {
                            if (modOrder.getOrderId() == currentOrderNumber) {
                                log().info("Sending modification order.." + modOrder);
                                context().actorSelection(PATH + "*").tell(modOrder, self());
                                expectedAcks++;
                            } else {
                                pendingModificationOrders.add(modOrder);
                            }

                        }).
                        match(CancellationOrder.class, cancellationOrder -> {
                            if (cancellationOrder.getOrderId() == currentOrderNumber) {
                                context().actorSelection(PATH + "*").tell(cancellationOrder, self());
                                expectedAcks++;
                            } else {
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
                                log().info("actor creation failed - there is already an actor with this path" + ex);
                            }

                            context().actorSelection(PATH + order.getProduct()).tell(order, self());

                        }).
        match(Shutdown.class, s -> {
    log().info("SHUTTING DOWN PARENT from:" + sender() + "expected acks:" + s.getTotal() + " so far:" + currentOrderNumber);

    if (s.getTotal() == currentOrderNumber - 1) {
        Set<OrderBook> orderBooks = new HashSet<OrderBook>();
        Set<Transaction> transactions = new HashSet<Transaction>();

        for (String product : products) {
            Future<?> result = ask(context().system().actorSelection(PATH + product), UtilityMessages.SHUTDOWN, SHUTDOWN_TIMEOUT_IN_MS);
            PartialResult r = (PartialResult) Await.result(result, Duration.Inf());
            log().info("PARTIAL RES:" + r);
            if (r.getOrderBook().isPresent()) {
                orderBooks.add(r.getOrderBook().get());
            }
            transactions.addAll(r.getTransactions());
        }

        BooksAndTransactions booksAndTransactions = new BooksAndTransactions(orderBooks, transactions);

        log().debug("RESULT:" + booksAndTransactions);
        sender().tell(booksAndTransactions, ActorRef.noSender());
    } else {
        context().system().scheduler().scheduleOnce(Duration.create(SHUTDOWN_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS), self(),
                new ForceShutdown(s.getTotal(), sender()), context().dispatcher(), null);
    }

}).
                        match(ForceShutdown.class, s ->{
                            Set<OrderBook> orderBooks = new HashSet<OrderBook>();
                            Set<Transaction> transactions = new HashSet<Transaction>();

                            for (String product : products) {
                                Future<?> result = ask(context().system().actorSelection(PATH + product), UtilityMessages.SHUTDOWN, SHUTDOWN_TIMEOUT_IN_MS);
                                PartialResult r = (PartialResult) Await.result(result, Duration.Inf());
                                log().info("PARTIAL RES:" + r);
                                if (r.getOrderBook().isPresent()) {
                                    orderBooks.add(r.getOrderBook().get());
                                }
                                transactions.addAll(r.getTransactions());
                            }

                            BooksAndTransactions booksAndTransactions = new BooksAndTransactions(orderBooks, transactions);

                            log().info("RESULT:" + booksAndTransactions);
                            s.getDestination().tell(booksAndTransactions, ActorRef.noSender());
                        }).
                        build()
        );
    }

}
