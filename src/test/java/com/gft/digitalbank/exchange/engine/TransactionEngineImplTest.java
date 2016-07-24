package com.gft.digitalbank.exchange.engine;

import com.gft.digitalbank.exchange.domain.Order;
import com.gft.digitalbank.exchange.domain.Side;
import com.gft.digitalbank.exchange.model.OrderEntry;
import com.gft.digitalbank.exchange.model.Transaction;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TransactionEngineImplTest {

    private TransactionEngine transactionEngine;
    private TransactionRegister transactionRegister;

    @Before
    public void init() {
        transactionRegister = mock(TransactionRegister.class);
    }

    @Test
    public void testEmptyOrderBooksAndTransactions() {
        // given
        transactionEngine = new TransactionEngineImpl("A", transactionRegister);

        // when

        // then
        assertThat(transactionEngine.getOrderBook().getBuyEntries().size(), is(0));
        assertThat(transactionEngine.getOrderBook().getSellEntries().size(), is(0));
        verifyZeroInteractions(transactionRegister);
    }

    @Test
    public void testOneBuyOrderAndNoTransactions() {
        // given
        transactionEngine = new TransactionEngineImpl("A", transactionRegister);
        Order order = new Order(1, Side.BUY, 110, 1, 1000, "broker-1", "cl-01");

        // when
        transactionEngine.processOrder(order);

        // then
        assertThat(transactionEngine.getOrderBook().getBuyEntries().size(), is(1));
        assertThat(transactionEngine.getOrderBook().getSellEntries().size(), is(0));
        verifyZeroInteractions(transactionRegister);
    }

    @Test
    public void testOneBuyOneSellOrderOneTransaction() {
        // given
        transactionEngine = new TransactionEngineImpl("A", transactionRegister);
        Order buyOrder = new Order(1, Side.BUY, 110, 1, 1000, "broker-1", "cl-01");
        Order sellOrder = new Order(2, Side.SELL, 110, 1, 1000, "broker-2", "cl-02");
        Transaction tx = new Transaction(1, 1000, 110, "A", "broker-1", "broker-2", "cl-01", "cl-02");

        // when
        transactionEngine.processOrder(buyOrder);
        transactionEngine.processOrder(sellOrder);

        // then
        assertThat(transactionEngine.getOrderBook().getBuyEntries().size(), is(0));
        assertThat(transactionEngine.getOrderBook().getSellEntries().size(), is(0));
        verify(transactionRegister).register(tx);
    }

    @Test
    public void testOnePartialBuyOrderOneTransaction() {
        // given
        transactionEngine = new TransactionEngineImpl("A", transactionRegister);
        Order buyOrder = new Order(1, Side.BUY, 110, 1, 2000, "broker-1", "cl-01");
        List<OrderEntry> partialBuyOrders = Arrays.asList(new OrderEntry(1, "broker-1", 1000, 110, "cl-01"));
        Order sellOrder = new Order(2, Side.SELL, 110, 1, 1000, "broker-2", "cl-02");
        Transaction tx = new Transaction(1, 1000, 110, "A", "broker-1", "broker-2", "cl-01", "cl-02");

        // when
        transactionEngine.processOrder(buyOrder);
        transactionEngine.processOrder(sellOrder);

        // then
        assertThat(transactionEngine.getOrderBook().getBuyEntries(), is(partialBuyOrders));
        assertThat(transactionEngine.getOrderBook().getSellEntries().size(), is(0));
        verify(transactionRegister).register(tx);
    }

}