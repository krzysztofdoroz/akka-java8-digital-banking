package com.gft.digitalbank.exchange.domain;

/**
 * Created by krzysztof on 23/07/16.
 */
public class Order {

    private final int orderId;
    private final Side side;
    private int price;
    private int timestamp;
    private int amount;
    private String broker;

    public Order(int orderId,
                 Side side,
                 int price,
                 int timestamp,
                 int amount,
                 String broker) {
        this.side = side;
        this.orderId = orderId;
        this.price = price;
        this.timestamp = timestamp;
        this.amount = amount;
        this.broker = broker;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;

        if (amount != order.amount) return false;
        if (orderId != order.orderId) return false;
        if (price != order.price) return false;
        if (timestamp != order.timestamp) return false;
        if (!broker.equals(order.broker)) return false;
        if (side != order.side) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = orderId;
        result = 31 * result + side.hashCode();
        result = 31 * result + price;
        result = 31 * result + timestamp;
        result = 31 * result + amount;
        result = 31 * result + broker.hashCode();
        return result;
    }

    public int getOrderId() {
        return orderId;
    }

    public Side getSide() {
        return side;
    }

    public int getPrice() {
        return price;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getBroker() {
        return broker;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", side=" + side +
                ", price=" + price +
                ", timestamp=" + timestamp +
                ", amount=" + amount +
                ", broker='" + broker + '\'' +
                '}';
    }
}
