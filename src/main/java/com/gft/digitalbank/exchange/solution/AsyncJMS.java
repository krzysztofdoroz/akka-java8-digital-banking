package com.gft.digitalbank.exchange.solution;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gft.digitalbank.exchange.domain.CancellationOrder;
import com.gft.digitalbank.exchange.domain.ModificationOrder;
import com.gft.digitalbank.exchange.domain.Order;
import com.gft.digitalbank.exchange.domain.ShutdownNotification;
import com.gft.digitalbank.exchange.engine.MatchingEngine;
import com.gft.digitalbank.exchange.engine.MatchingEngineImpl;
import com.gft.digitalbank.exchange.engine.TransactionRegister;
import com.gft.digitalbank.exchange.engine.TransactionRegisterImpl;
import com.gft.digitalbank.exchange.listener.ProcessingListener;
import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.SolutionResult;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by krzysztof on 23/07/16.
 */
public class AsyncJMS {

    public static final String ORDER = "ORDER";
    public static final String CANCEL = "CANCEL";
    public static final String MODIFICATION = "MODIFICATION";
    public static final String SHUTDOWN_NOTIFICATION = "SHUTDOWN_NOTIFICATION";
    private final List<String> dests;
    private final ProcessingListener processingListener;
    private AtomicInteger activeBrokers;

    public AsyncJMS(final List<String> dests, final ProcessingListener processingListener) throws NamingException, JMSException {
        this.dests = dests;
        this.processingListener = processingListener;
        this.activeBrokers = new AtomicInteger(dests.size());

        Context context = new InitialContext();
        ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup("ConnectionFactory");

        TransactionRegister transactionRegister = new TransactionRegisterImpl();
        MatchingEngine matchingEngine = new MatchingEngineImpl("A", transactionRegister);

        // Create a Connection
        Connection connection = connectionFactory.createConnection();
        // connection.start();

        for (String queue : dests) {

            Session session = connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);

            Destination destination = session.createQueue(queue);

            MessageConsumer consumer = session.createConsumer(destination);
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {

                        String payload = ((TextMessage) message).getText();
                        String type = message.getStringProperty("messageType");

                        ObjectMapper mapper = new ObjectMapper();

                        switch (type) {
                            case ORDER:
                                Order order = mapper.readValue(payload, Order.class);
                                System.out.println("ORDER:" + order);

                                matchingEngine.processOrder(order);

                                break;
                            case CANCEL:
                                CancellationOrder cancellationOrder = mapper.readValue(payload, CancellationOrder.class);
                                matchingEngine.cancelOrder(cancellationOrder);
                                   System.out.println("CANCEL:" + cancellationOrder);
                                break;
                            case MODIFICATION:
                                ModificationOrder modificationOrder = mapper.readValue(payload, ModificationOrder.class);
                                System.out.println("MODIFICATION:" + modificationOrder);
                                matchingEngine.modifyOrder(modificationOrder);
                                break;
                            case SHUTDOWN_NOTIFICATION:
                                ShutdownNotification shutdownNotification = mapper.readValue(payload, ShutdownNotification.class);
                                System.out.println("SHUTDOWN_NOTIFICATION:" + shutdownNotification);
                                // return result from matching engine

                                activeBrokers.decrementAndGet();

                                session.close();

                                // kill connection for this broker
                                if (activeBrokers.get() == 0) {
                                    Set<OrderBook> orderBooks = new HashSet<OrderBook>();

                                    if(matchingEngine.getOrderBook().isPresent()) {
                                        orderBooks.add(matchingEngine.getOrderBook().get());
                                    }

                                    processingListener.processingDone(
                                            SolutionResult.builder()
                                                    .transactions(transactionRegister.getTransactions()) // transactions is a Set<Transaction>
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
                    }
                }
            });
            connection.start();
        }

    }


}
