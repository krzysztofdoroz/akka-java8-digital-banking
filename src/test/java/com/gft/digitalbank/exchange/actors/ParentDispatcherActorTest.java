package com.gft.digitalbank.exchange.actors;

import akka.actor.*;
import akka.testkit.JavaTestKit;
import com.gft.digitalbank.exchange.actors.messages.*;
import com.gft.digitalbank.exchange.domain.Order;
import com.gft.digitalbank.exchange.domain.ShutdownNotification;
import com.gft.digitalbank.exchange.domain.Side;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by krzysztof on 06/08/16.
 */
public class ParentDispatcherActorTest {

    private ActorSystem system;

    @Before
    public void setup() {
        system = ActorSystem.create();
    }

    @After
    public void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testNoReplyToOrderMessage() {
        new JavaTestKit(system) {{
            // given
            ActorRef deadLetters = system.actorOf(Props.create(DeadLetterActor.class), "dead-letter-actor");
            system.eventStream().subscribe(deadLetters, DeadLetter.class);
            final Props props = Props.create(ParentDispatcherActor.class);
            final ActorRef parentDispatcher = system.actorOf(props, "parent-dispatcher");

            // when
            Order order = new Order(1, "A", Side.BUY, 110, 1, 1000, "broker-1", "cl-01");
            parentDispatcher.tell(order, getRef());

            // then
            new Within(duration("1 seconds")) {
                protected void run() {
                    expectNoMsg();
                }
            };
        }};
    }

    @Test
    public void testForceShutdownMessage() {
        new JavaTestKit(system) {{
            // given
            ActorRef deadLetters = system.actorOf(Props.create(DeadLetterActor.class), "dead-letter-actor");
            system.eventStream().subscribe(deadLetters, DeadLetter.class);
            final Props props = Props.create(ParentDispatcherActor.class);
            final ActorRef parentDispatcher = system.actorOf(props, "parent-dispatcher");

            // when
            Order order = new Order(1, "A", Side.BUY, 110, 1, 1000, "broker-1", "cl-01");
            Order order2 = new Order(5, "A", Side.SELL, 110, 5, 1000, "broker-1", "cl-01");

            parentDispatcher.tell(order, getRef());
            parentDispatcher.tell(order2, getRef());
            parentDispatcher.tell(new Shutdown(1), getRef());

            // then
            new Within(duration("5 seconds")) {
                protected void run() {
                    expectMsgClass(BooksAndTransactions.class);
                }
            };
        }};
    }



}
