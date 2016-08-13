package com.gft.digitalbank.exchange.solution;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gft.digitalbank.exchange.actors.messages.BooksAndTransactions;
import com.gft.digitalbank.exchange.actors.messages.Shutdown;
import com.gft.digitalbank.exchange.domain.CancellationOrder;
import com.gft.digitalbank.exchange.domain.ModificationOrder;
import com.gft.digitalbank.exchange.domain.Order;
import com.gft.digitalbank.exchange.listener.ProcessingListener;
import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.SolutionResult;
import com.gft.digitalbank.exchange.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.jms.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static akka.pattern.Patterns.ask;

/**
 * Created by krzysztof on 06/08/16.
 */
public class JmsToAkkaMessageDispatcher implements MessageListener {

    private final static Logger LOG = LoggerFactory.getLogger(JmsToAkkaMessageDispatcher.class);

    public static final String ORDER = "ORDER";
    public static final String CANCEL = "CANCEL";
    public static final String MODIFICATION = "MODIFICATION";
    public static final String SHUTDOWN_NOTIFICATION = "SHUTDOWN_NOTIFICATION";
    public static final String MESSAGE_TYPE = "messageType";
    private static final int TIMEOUT_IN_MS = 15000;

    private final Session session;
    private final AtomicInteger activeBrokers;
    private final ProcessingListener processingListener;
    private final ActorSystem system;
    private final ActorRef parentDispatcher;
    private int total = 0;

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
            String type = message.getStringProperty(MESSAGE_TYPE);

            ObjectMapper mapper = new ObjectMapper();

            switch (type) {
                case ORDER:
                    Order order = mapper.readValue(payload, Order.class);
                    parentDispatcher.tell(order, ActorRef.noSender());

                    total++;
                    break;
                case CANCEL:
                    CancellationOrder cancellationOrder = mapper.readValue(payload, CancellationOrder.class);
                    LOG.info("received cancellation message:" + cancellationOrder);
                    parentDispatcher.tell(cancellationOrder, ActorRef.noSender());

                    total++;
                    break;
                case MODIFICATION:
                    ModificationOrder modificationOrder = mapper.readValue(payload, ModificationOrder.class);
                    LOG.info("modification:" + modificationOrder);
                    parentDispatcher.tell(modificationOrder, ActorRef.noSender());

                    total++;
                    break;
                case SHUTDOWN_NOTIFICATION:
                    LOG.debug("close JMS session...");
                    session.close();

                    if (activeBrokers.decrementAndGet() == 0) {
                        Set<OrderBook> orderBooks = new HashSet<>();
                        Set<Transaction> transactions = new HashSet<>();

                        Future<?> result = ask(parentDispatcher, new Shutdown(total), TIMEOUT_IN_MS);

                        Object r = Await.result(result, Duration.Inf());
                        LOG.info("result from akka:" + r);
                        if (r instanceof BooksAndTransactions) {
                            orderBooks = ((BooksAndTransactions) r).getOrderBooks();
                            transactions = ((BooksAndTransactions) r).getTransactions();
                        } else {
                            LOG.error("unrecognized result type:" + r);
                        }

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
                    LOG.warn("received unrecognized message type:" + type);
            }
            // don't rethrow any exceptions, just log them
        } catch (Exception e) {
            LOG.error("Some issue with dispatching messages to actor system:" + e);
        }
    }
}
