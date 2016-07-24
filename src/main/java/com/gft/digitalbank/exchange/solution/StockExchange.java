package com.gft.digitalbank.exchange.solution;

import com.gft.digitalbank.exchange.Exchange;
import com.gft.digitalbank.exchange.listener.ProcessingListener;
import com.gft.digitalbank.exchange.model.OrderBook;
import com.gft.digitalbank.exchange.model.SolutionResult;
import com.gft.digitalbank.exchange.model.Transaction;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Your solution must implement the {@link Exchange} interface.
 */
public class StockExchange implements Exchange {

    private AsyncJMS asyncJMS;
    private ProcessingListener processingListener;

    @Override
    public void register(ProcessingListener processingListener) {
        this.processingListener = processingListener;
    }

    @Override
    public void setDestinations(List<String> list) {

        try {
            asyncJMS = new AsyncJMS(list);
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
//        throw new UnsupportedOperationException();
    }

    @Override
    public void start() {

        Set<Transaction> transactions = new HashSet<>();
        transactions.add(new Transaction(1, 100, 100, "A",
                "1", "2", "100", "101"));
        Set<OrderBook> orderBooks = new HashSet<>();


        processingListener.processingDone(
                SolutionResult.builder()
                        .transactions(transactions) // transactions is a Set<Transaction>
                        .orderBooks(orderBooks) // orderBooks is a Set<OrderBook>
                        .build()
        );
    }
}
