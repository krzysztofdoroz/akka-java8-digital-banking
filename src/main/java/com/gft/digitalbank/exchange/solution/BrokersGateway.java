package com.gft.digitalbank.exchange.solution;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.DeadLetter;
import akka.actor.Props;
import com.gft.digitalbank.exchange.actors.DeadLetterActor;
import com.gft.digitalbank.exchange.actors.ParentDispatcherActor;
import com.gft.digitalbank.exchange.listener.ProcessingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by krzysztof on 23/07/16.
 */
public class BrokersGateway {

    private final static Logger LOG = LoggerFactory.getLogger(BrokersGateway.class);

    private static final String DEAD_LETTERS_ACTOR_NAME = "dead-letter-actor";
    private static final String PARENT_DISPATCHER_ACTOR_NAME = "parent-dispatcher";
    private static final String ACTOR_SYSTEM_NAME = "digitalBanking";
    public static final String DIGITALBANKING_PROPERTIES = "digitalbanking.properties";
    public static final String SHUTDOWN_TIMEOUT_PROP_NAME = "shutdown.timeout.in.ms";
    public static final String SHUTDOWN_TIMEOUT_IN_MS_DEFAULT = "shutdown.timeout.in.ms";

    private final ProcessingListener processingListener;
    private final AtomicInteger activeBrokers;
    private final ActorRef parentDispatcher;
    private final ActorRef deadLetters;
    private final ActorSystem system = ActorSystem.create(ACTOR_SYSTEM_NAME);
    private int shutdownTimeout;
    private Properties properties = new Properties();

    public BrokersGateway(final List<String> dests, final ProcessingListener processingListener) throws NamingException, JMSException {
        this.processingListener = processingListener;
        this.activeBrokers = new AtomicInteger(dests.size());

        // load properties
        loadProperties(DIGITALBANKING_PROPERTIES);
        shutdownTimeout = Integer.valueOf(properties.getProperty(SHUTDOWN_TIMEOUT_PROP_NAME, SHUTDOWN_TIMEOUT_IN_MS_DEFAULT));

        // spawn parent dispatcher actor
        parentDispatcher = system.actorOf(Props.create(ParentDispatcherActor.class, shutdownTimeout), PARENT_DISPATCHER_ACTOR_NAME);
        deadLetters = system.actorOf(Props.create(DeadLetterActor.class), DEAD_LETTERS_ACTOR_NAME);

        // subscribe to dead letters
        system.eventStream().subscribe(deadLetters, DeadLetter.class);

        // get hold of JMS connection factory
        Context context = new InitialContext();
        ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup("ConnectionFactory");

        // Create a Connection
        Connection connection = connectionFactory.createConnection();

        for (String queue : dests) {

            Session session = connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);

            Destination destination = session.createQueue(queue);

            MessageConsumer consumer = session.createConsumer(destination);
            consumer.setMessageListener(
                    new JmsToAkkaMessageDispatcher(session, activeBrokers, processingListener, system, parentDispatcher));

        }
        connection.start();
    }

    private void loadProperties(final String filename) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream resourceStream = loader.getResourceAsStream(filename)) {
            properties.load(resourceStream);
        } catch (IOException e) {
            LOG.error("error loading properties... falling back to defaults", e);
        }
    }
}
