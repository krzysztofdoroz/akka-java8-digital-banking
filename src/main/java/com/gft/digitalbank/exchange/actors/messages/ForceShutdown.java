package com.gft.digitalbank.exchange.actors.messages;

import akka.actor.ActorRef;

/**
 * Created by krzysztof on 02/08/16.
 */
public class ForceShutdown {

    private int total;
    private ActorRef destination;

    public ForceShutdown(int total, ActorRef destination) {
        this.total = total;
        this.destination = destination;
    }

    public int getTotal() {
        return total;
    }

    public ActorRef getDestination() {
        return destination;
    }

    @Override
    public String toString() {
        return "Shutdown{" +
                "total=" + total +
                '}';
    }

}
