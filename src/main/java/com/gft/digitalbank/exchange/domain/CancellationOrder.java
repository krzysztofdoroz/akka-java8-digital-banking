package com.gft.digitalbank.exchange.domain;

/**
 * Created by krzysztof on 23/07/16.
 */
public class CancellationOrder {

    private int cancelledOrderId;
    private String broker;
    private int timestamp;

    public CancellationOrder(int cancelledOrderId, String broker, int timestamp) {
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

    @Override
    public String toString() {
        return "CancellationOrder{" +
                "cancelledOrderId=" + cancelledOrderId +
                ", broker='" + broker + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
