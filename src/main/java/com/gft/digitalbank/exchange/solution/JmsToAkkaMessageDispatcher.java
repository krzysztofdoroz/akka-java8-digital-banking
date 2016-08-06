package com.gft.digitalbank.exchange.solution;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.DeadLetter;
import akka.actor.Props;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gft.digitalbank.exchange.actors.DeadLetterActor;
import com.gft.digitalbank.exchange.actors.ParentDispatcherActor;
import com.gft.digitalbank.exchange.actors.messages.*;
import com.gft.digitalbank.exchange.domain.CancellationOrder;
import com.gft.digitalbank.exchange.domain.ModificationOrder;
import com.gft.digitalbank.exchange.domain.Order;
import com.gft.digitalbank.exchange.domain.ShutdownNotification;
import com.gft.digitalbank.exchange.listener.ProcessingListener;
import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.SolutionResult;
import com.gft.digitalbank.exchange.model.Transaction;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.jms.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static akka.pattern.Patterns.ask;

/**
 * Created by krzysztof on 06/08/16.
 */
public class JmsToAkkaMessageDispatcher implements MessageListener {

    public static final String ORDER = "ORDER";
    public static final String CANCEL = "CANCEL";
    public static final String MODIFICATION = "MODIFICATION";
    public static final String SHUTDOWN_NOTIFICATION = "SHUTDOWN_NOTIFICATION";


    private int total = 0;
    private final Session session;
    private final AtomicInteger activeBrokers;
    final ProcessingListener processingListener;
    final ActorSystem system;
    final ActorRef parentDispatcher;

    public JmsToAkkaMessageDispatcher(final Session session, final AtomicInteger activeBrokers,
                                      final ProcessingListener processingListener, final ActorSystem system,
                                      final ActorRef parentDispatcher) {
        this.session = session;
        this.activeBrokers = activeBrokers;
        this.processingListener = processingListener;
        this.system = system;
        this.parentDispatcher = parentDispatcher;

    }

    @Override
    public void onMessage(Message message) {
        try {

            String payload = ((TextMessage) message).getText();
            String type = message.getStringProperty("messageType");

            ObjectMapper mapper = new ObjectMapper();

            switch (type) {
                case ORDER:
                    Order order = mapper.readValue(payload, Order.class);
                    system.actorSelection("/user/parent-dispatcher").tell(order, ActorRef.noSender());
                    total++;

                    break;
                case CANCEL:
                    CancellationOrder cancellationOrder = mapper.readValue(payload, CancellationOrder.class);

//                                for (MatchingEngine engine : productToEngine.values()) {
//                                     engine.cancelOrder(cancellationOrder);
//                                }
                    parentDispatcher.tell(cancellationOrder, ActorRef.noSender());
                    total++;
                    System.out.println("CANCEL:" + cancellationOrder);
                    break;
                case MODIFICATION:
                    ModificationOrder modificationOrder = mapper.readValue(payload, ModificationOrder.class);
                    System.out.println("MODIFICATION:" + modificationOrder);
//                                for (MatchingEngine engine : productToEngine.values()) {
//                                    engine.modifyOrder(modificationOrder);
//                                }
                    parentDispatcher.tell(modificationOrder, ActorRef.noSender());
                    total++;
                    break;
                case SHUTDOWN_NOTIFICATION:
                    ShutdownNotification shutdownNotification = mapper.readValue(payload, ShutdownNotification.class);
                    System.out.println("SHUTDOWN_NOTIFICATION:" + shutdownNotification);
                    // return result from matching engine

                    session.close();

                    // kill connection for this broker
                    if (activeBrokers.decrementAndGet() == 0) {
                        Set<OrderBook> orderBooks = new HashSet<OrderBook>();
                        Set<Transaction> transactions = new HashSet<Transaction>();

                        Future<?> result = ask(parentDispatcher, new Shutdown(total), 15000);

                        Object r = Await.result(result, Duration.Inf());
                        System.out.println("result from akka:" + r);
                        if (r instanceof BooksAndTransactions) {
                            orderBooks = ((BooksAndTransactions) r).getOrderBooks();
                            transactions = ((BooksAndTransactions) r).getTransactions();
                        }

//                                    for (MatchingEngine engine : productToEngine.values()){
//                                        if(engine.getOrderBook().isPresent()) {
//                                            orderBooks.add(engine.getOrderBook().get());
//                                        }
//                                    }

                        system.shutdown();

                        processingListener.processingDone(
                                SolutionResult.builder()
                                        .transactions(transactions) // transactions is a Set<Transaction>
                                        .orderBooks(orderBooks) // orderBooks is a Set<OrderBook>
                                        .build()
                        );

                    }

                    break;
                default:
                    // log warn
            }

        } catch (JMSException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
