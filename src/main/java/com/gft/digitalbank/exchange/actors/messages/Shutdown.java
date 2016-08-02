package com.gft.digitalbank.exchange.actors.messages;

/**
 * Created by krzysztof on 01/08/16.
 */
public class Shutdown {

    private int total;

    public Shutdown(int total) {
        this.total = total;
    }

    public int getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return "Shutdown{" +
                "total=" + total +
                '}';
    }
}
