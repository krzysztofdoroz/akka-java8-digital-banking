package com.gft.digitalbank.exchange.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by krzysztof on 30/07/16.
 */

@JsonIgnoreProperties("messageType")
public class ShutdownNotification {

    private int id;
    private int timestamp;
    private String broker;

    public ShutdownNotification() {
        // empty constructor for Jackson
    }

    public int getId() {
        return id;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getBroker() {
        return broker;
    }

    @Override
    public String toString() {
        return "ShutdownNotification{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", broker='" + broker + '\'' +
                '}';
    }
}
