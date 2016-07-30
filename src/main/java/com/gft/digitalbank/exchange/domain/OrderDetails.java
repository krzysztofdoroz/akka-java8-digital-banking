package com.gft.digitalbank.exchange.domain;

/**
 * Created by krzysztof on 24/07/16.
 */

public class OrderDetails {

    private int price;
    private int amount;

    public OrderDetails() {
        // empty constructor for Jackson
    }

    public OrderDetails(int price, int amount) {
        this.price = price;
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderDetails that = (OrderDetails) o;

        if (amount != that.amount) return false;
        if (price != that.price) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = price;
        result = 31 * result + amount;
        return result;
    }

    public int getPrice() {
        return price;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "OrderDetails{" +
                "price=" + price +
                ", amount=" + amount +
                '}';
    }
}
