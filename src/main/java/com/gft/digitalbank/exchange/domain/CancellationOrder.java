package com.gft.digitalbank.exchange.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by krzysztof on 23/07/16.
 */

@JsonIgnoreProperties("messageType")
public class CancellationOrder {

    @JsonProperty("id")
    private int orderId;
    private int cancelledOrderId;
    private String broker;
    private int timestamp;

    public CancellationOrder() {
        // empty constructor for Jackson
    }

    public CancellationOrder(int orderId, int cancelledOrderId, String broker, int timestamp) {
        this.orderId = orderId;
        this.cancelledOrderId = cancelledOrderId;
        this.broker = broker;
        this.timestamp = timestamp;
    }

    public int getCancelledOrderId() {
        return cancelledOrderId;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getBroker() {
        return broker;
    }

    public int getOrderId() {
        return orderId;
    }

    @Override
    public String toString() {
        return "CancellationOrder{" +
                "orderId=" + orderId +
                ", cancelledOrderId=" + cancelledOrderId +
                ", broker='" + broker + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
