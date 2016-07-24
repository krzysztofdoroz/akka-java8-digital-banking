package com.gft.digitalbank.exchange.domain;

/**
 * Created by krzysztof on 23/07/16.
 */
public class ModificationOrder {

    private int orderId;
    private int modifiedOrderId;
    private String broker;
    private int newAmount;
    private int newPrice;
    private int timestamp;

    public ModificationOrder(int orderId, int modifiedOrderId, String broker, int newAmount, int newPrice, int timestamp) {
        this.orderId = orderId;
        this.modifiedOrderId = modifiedOrderId;
        this.broker = broker;
        this.newAmount = newAmount;
        this.newPrice = newPrice;
        this.timestamp = timestamp;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getModifiedOrderId() {
        return modifiedOrderId;
    }

    public String getBroker() {
        return broker;
    }

    public int getNewAmount() {
        return newAmount;
    }

    public int getNewPrice() {
        return newPrice;
    }

    public int getTimestamp() {
        return timestamp;
    }
}
