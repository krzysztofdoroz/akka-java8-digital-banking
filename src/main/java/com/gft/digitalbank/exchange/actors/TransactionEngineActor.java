package com.gft.digitalbank.exchange.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import com.gft.digitalbank.exchange.actors.messages.PartialResult;
import com.gft.digitalbank.exchange.actors.messages.UtilityMessages;
import com.gft.digitalbank.exchange.domain.CancellationOrder;
import com.gft.digitalbank.exchange.domain.ModificationOrder;
import com.gft.digitalbank.exchange.domain.Order;
import com.gft.digitalbank.exchange.engine.MatchingEngine;
import com.gft.digitalbank.exchange.engine.MatchingEngineImpl;

/**
 * Created by krzysztof on 31/07/16.
 */
public class TransactionEngineActor extends AbstractLoggingActor {

    private final MatchingEngine engine;

    public TransactionEngineActor(final String product) {

        this.engine = new MatchingEngineImpl(product);

        receive(ReceiveBuilder.
                        match(Order.class, order -> {
                            log().debug("received message:" + order);
                            engine.processOrder(order);
                            sender().tell(UtilityMessages.ACK, ActorRef.noSender());

                        }).
                        match(ModificationOrder.class, mod -> {
                            if(engine.modifyOrder(mod)) {
                                sender().tell(UtilityMessages.ACK, ActorRef.noSender());
                            }
                        }).
                        match(CancellationOrder.class, cancel -> {
                            if (engine.cancelOrder(cancel)) {
                                sender().tell(UtilityMessages.ACK, ActorRef.noSender());
                            }
                        }).
                        matchEquals(UtilityMessages.SHUTDOWN, s -> {
                            log().info("shutting down..." + product);
                            sender().tell(new PartialResult(engine.getOrderBook(), engine.getTransactions()), ActorRef.noSender());
                        })
                        .build()
        );
    }
}
