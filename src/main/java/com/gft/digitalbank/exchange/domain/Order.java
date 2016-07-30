package com.gft.digitalbank.exchange.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by krzysztof on 23/07/16.
 */

@JsonIgnoreProperties("messageType")
public class Order {

    @JsonProperty("id")
    private int orderId;
    private String product;
    private Side side;
    private int timestamp;
    private String broker;
    private String client;
    @JsonProperty("details")
    private OrderDetails orderDetails;

    public Order(){
        // empty constructor for Jackson
    }

    public Order(int orderId,
                 String product,
                 Side side,
                 int price,
                 int timestamp,
                 int amount,
                 String broker,
                 String client) {
        this.side = side;
        this.orderId = orderId;
        this.product = product;
        this.timestamp = timestamp;
        this.broker = broker;
        this.client = client;
        this.orderDetails = new OrderDetails(price, amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;

//        if (amount != order.amount) return false;
        if (orderId != order.orderId) return false;
//        if (price != order.price) return false;
        if (timestamp != order.timestamp) return false;
        if (!broker.equals(order.broker)) return false;
        if (!client.equals(order.client)) return false;
        if (!product.equals(order.product)) return false;
        if (side != order.side) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = orderId;
        result = 31 * result + product.hashCode();
        result = 31 * result + side.hashCode();
//        result = 31 * result + price;
        result = 31 * result + timestamp;
//        result = 31 * result + amount;
        result = 31 * result + broker.hashCode();
        result = 31 * result + client.hashCode();
        return result;
    }

    public int getOrderId() {
        return orderId;
    }

    public Side getSide() {
        return side;
    }

    public int getPrice() {
        return orderDetails.getPrice();
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getBroker() {
        return broker;
    }

    public int getAmount() {
        return orderDetails.getAmount();
    }

    public String getClient() {
        return client;
    }

    public String getProduct() {
        return product;
    }

    public OrderDetails getOrderDetails() {
        return orderDetails;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", product='" + product + '\'' +
                ", side=" + side +
                ", price=" + orderDetails.getPrice() +
                ", timestamp=" + timestamp +
                ", amount=" + orderDetails.getAmount() +
                ", broker='" + broker + '\'' +
                ", client='" + client + '\'' +
                '}';
    }
}
