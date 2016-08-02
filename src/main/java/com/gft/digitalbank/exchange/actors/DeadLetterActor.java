package com.gft.digitalbank.exchange.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.DeadLetter;
import akka.japi.pf.ReceiveBuilder;
import com.gft.digitalbank.exchange.actors.messages.ActorCreationRequest;
import com.gft.digitalbank.exchange.domain.Order;

/**
 * Created by krzysztof on 31/07/16.
 */
public class DeadLetterActor extends AbstractLoggingActor {

    public DeadLetterActor() {
        receive(ReceiveBuilder.
                        match(DeadLetter.class, message -> {
                            if (((DeadLetter) message).message() instanceof Order) {
                                log().info("missing actor, sending request...");
                                Order payload = (Order) ((DeadLetter) message).message();

                                context().system().actorFor("/user/parent-dispatcher").tell(new ActorCreationRequest(payload), ActorRef.noSender());
                            }
                        }).
                        build()
        );
    }
}