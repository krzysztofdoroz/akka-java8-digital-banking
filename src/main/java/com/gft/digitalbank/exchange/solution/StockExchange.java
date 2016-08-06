package com.gft.digitalbank.exchange.solution;

import com.gft.digitalbank.exchange.Exchange;
import com.gft.digitalbank.exchange.listener.ProcessingListener;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.util.List;

/**
 * Your solution must implement the {@link Exchange} interface.
 */
public class StockExchange implements Exchange {

    private BrokersGateway brokersGateway;
    private ProcessingListener processingListener;
    private List<String> dests;

    @Override
    public void register(ProcessingListener processingListener) {
        this.processingListener = processingListener;
    }

    @Override
    public void setDestinations(List<String> list) {
          this.dests = list;
    }

    @Override
    public void start() {
        try {
            brokersGateway = new BrokersGateway(dests, processingListener);
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
