package com.gft.digitalbank.exchange.actors.messages;

import com.gft.digitalbank.exchange.domain.Order;

/**
 * Created by krzysztof on 31/07/16.
 */
public class ActorCreationRequest {

    private final Order order;

    public ActorCreationRequest(Order message) {
        this.order = message;
    }

    public Order getOrder() {
        return order;
    }
}
