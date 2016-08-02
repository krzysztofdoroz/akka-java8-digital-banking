package com.gft.digitalbank.exchange.actors.messages;

/**
 * Created by krzysztof on 01/08/16.
 */
public class Ack {

    private int number;

    public Ack(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return "Ack{" +
                "number=" + number +
                '}';
    }
}
