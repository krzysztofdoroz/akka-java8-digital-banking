package com.gft.digitalbank.exchange.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by krzysztof on 23/07/16.
 */
@JsonIgnoreProperties("messageType")
public class ModificationOrder {

    @JsonProperty("id")
    private int orderId;
    private int modifiedOrderId;
    private String broker;
    @JsonProperty("details")
    private OrderDetails orderDetails;
    private int timestamp;

    public ModificationOrder() {
        // empty constructor for Jackson
    }

    public ModificationOrder(int orderId, int modifiedOrderId, String broker, int newAmount, int newPrice, int timestamp) {
        this.orderId = orderId;
        this.modifiedOrderId = modifiedOrderId;
        this.broker = broker;
        this.orderDetails = new OrderDetails(newPrice, newAmount);
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

    public OrderDetails getOrderDetails() {
        return orderDetails;
    }

    public int getPrice() {
        return orderDetails.getPrice();
    }

    public int getAmount() {
        return orderDetails.getAmount();
    }

    public int getTimestamp() {
        return timestamp;
    }
}
