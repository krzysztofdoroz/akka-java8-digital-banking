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

    private static final String PATH = "/user/parent-dispatcher";

    public DeadLetterActor() {
        receive(ReceiveBuilder.
                        match(DeadLetter.class, message -> {
                            if (message.message() instanceof Order) {
                                log().info("missing actor, sending request...");
                                Order payload = (Order) message.message();

                                context().system().actorFor(PATH).tell(new ActorCreationRequest(payload), ActorRef.noSender());
                            }
                        }).
                        build()
        );
    }
}