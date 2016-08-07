package com.gft.digitalbank.exchange.solution;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.DeadLetter;
import akka.actor.Props;
import com.gft.digitalbank.exchange.actors.DeadLetterActor;
import com.gft.digitalbank.exchange.actors.ParentDispatcherActor;
import com.gft.digitalbank.exchange.listener.ProcessingListener;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by krzysztof on 23/07/16.
 */
public class BrokersGateway {

    private final List<String> dests;
    private final ProcessingListener processingListener;
    private final AtomicInteger activeBrokers;
    private final ActorRef parentDispatcher;
    private final ActorRef deadLetters;
    private final ActorSystem system = ActorSystem.create("digitalBanking");

    public BrokersGateway(final List<String> dests, final ProcessingListener processingListener) throws NamingException, JMSException {
        this.dests = dests;
        this.processingListener = processingListener;
        this.activeBrokers = new AtomicInteger(dests.size());

        parentDispatcher = system.actorOf(Props.create(ParentDispatcherActor.class), "parent-dispatcher");
        deadLetters = system.actorOf(Props.create(DeadLetterActor.class), "dead-letter-actor");

        system.eventStream().subscribe(deadLetters, DeadLetter.class);

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
                    new JmsToAkkaMessageDispatcher(session, activeBrokers, processingListener, system, parentDispatcher)  );

            connection.start();
        }
    }
}
