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

public class MatchingEngineImplTest {

    private MatchingEngine matchingEngine;
    private TransactionRegister transactionRegister;

    @Before
    public void init() {
        transactionRegister = mock(TransactionRegister.class);
    }

    @Test
    public void testEmptyOrderBooksAndTransactions() {
        // given
        matchingEngine = new MatchingEngineImpl("A", transactionRegister);

        // when

        // then
        assertThat(matchingEngine.getOrderBook().getBuyEntries().size(), is(0));
        assertThat(matchingEngine.getOrderBook().getSellEntries().size(), is(0));
        verifyZeroInteractions(transactionRegister);
    }

    @Test
    public void testOneBuyOrderAndNoTransactions() {
        // given
        matchingEngine = new MatchingEngineImpl("A", transactionRegister);
        Order order = new Order(1, "A", Side.BUY, 110, 1, 1000, "broker-1", "cl-01");

        // when
        matchingEngine.processOrder(order);

        // then
        assertThat(matchingEngine.getOrderBook().getBuyEntries().size(), is(1));
        assertThat(matchingEngine.getOrderBook().getSellEntries().size(), is(0));
        verifyZeroInteractions(transactionRegister);
    }

    @Test
    public void testOneBuyOneSellOrderOneTransaction() {
        // given
        matchingEngine = new MatchingEngineImpl("A", transactionRegister);
        Order buyOrder = new Order(1, "A", Side.BUY, 110, 1, 1000, "broker-1", "cl-01");
        Order sellOrder = new Order(2, "A", Side.SELL, 110, 1, 1000, "broker-2", "cl-02");
        Transaction tx = new Transaction(1, 1000, 110, "A", "broker-1", "broker-2", "cl-01", "cl-02");

        // when
        matchingEngine.processOrder(buyOrder);
        matchingEngine.processOrder(sellOrder);

        // then
        assertThat(matchingEngine.getOrderBook().getBuyEntries().size(), is(0));
        assertThat(matchingEngine.getOrderBook().getSellEntries().size(), is(0));
        verify(transactionRegister).register(tx);
    }

    @Test
    public void testOnePartialBuyOrderOneTransaction() {
        // given
        matchingEngine = new MatchingEngineImpl("A", transactionRegister);
        Order buyOrder = new Order(1, "A", Side.BUY, 110, 1, 2000, "broker-1", "cl-01");
        List<OrderEntry> partialBuyOrders = Arrays.asList(new OrderEntry(1, "broker-1", 1000, 110, "cl-01"));
        Order sellOrder = new Order(2, "A", Side.SELL, 110, 1, 1000, "broker-2", "cl-02");
        Transaction tx = new Transaction(1, 1000, 110, "A", "broker-1", "broker-2", "cl-01", "cl-02");

        // when
        matchingEngine.processOrder(buyOrder);
        matchingEngine.processOrder(sellOrder);

        // then
        assertThat(matchingEngine.getOrderBook().getBuyEntries(), is(partialBuyOrders));
        assertThat(matchingEngine.getOrderBook().getSellEntries().size(), is(0));
        verify(transactionRegister).register(tx);
    }

    @Test
    public void testOneBuyOrderTwoSellOrdersTwoTransactions() {
        // given
        matchingEngine = new MatchingEngineImpl("A", transactionRegister);
        Order buyOrder = new Order(1, "A", Side.BUY, 110, 1, 2000, "broker-1", "cl-01");
        Order sellOrder1 = new Order(2, "A", Side.SELL, 100, 2, 1000, "broker-2", "cl-02");
        Order sellOrder2 = new Order(3, "A", Side.SELL, 95, 3, 1000, "broker-2", "cl-02");
        Transaction tx = new Transaction(1, 1000, 100, "A", "broker-1", "broker-2", "cl-01", "cl-02");
        Transaction tx2 = new Transaction(2, 1000, 95, "A", "broker-1", "broker-2", "cl-01", "cl-02");

        // when
        matchingEngine.processOrder(sellOrder1);
        matchingEngine.processOrder(buyOrder);
        matchingEngine.processOrder(sellOrder2);

        // then
        assertThat(matchingEngine.getOrderBook().getBuyEntries().size(), is(0));
        assertThat(matchingEngine.getOrderBook().getSellEntries().size(), is(0));
        verify(transactionRegister).register(tx);
        verify(transactionRegister).register(tx2);
    }

    @Test
    public void testOnePartialBuyOrderFourSellOrdersFourTransactions() {
        // given
        matchingEngine = new MatchingEngineImpl("A", transactionRegister);
        Order buyOrder = new Order(1, "A", Side.BUY, 110, 1, 2500, "broker-1", "cl-01");
        List<OrderEntry> partialBuyOrders = Arrays.asList(new OrderEntry(1, "broker-1", 500, 110, "cl-01"));
        Order sellOrder1 = new Order(2, "A", Side.SELL, 100, 2, 500, "broker-2", "cl-02");
        Order sellOrder2 = new Order(3, "A", Side.SELL, 95, 3, 500, "broker-2", "cl-02");
        Order sellOrder3 = new Order(4, "A", Side.SELL, 90, 4, 500, "broker-2", "cl-02");
        Order sellOrder4 = new Order(5, "A", Side.SELL, 105, 5, 500, "broker-2", "cl-02");
        Transaction tx = new Transaction(1, 500, 100, "A", "broker-1", "broker-2", "cl-01", "cl-02");
        Transaction tx2 = new Transaction(2, 500, 95, "A", "broker-1", "broker-2", "cl-01", "cl-02");
        Transaction tx3 = new Transaction(3, 500, 90, "A", "broker-1", "broker-2", "cl-01", "cl-02");
        Transaction tx4 = new Transaction(4, 500, 105, "A", "broker-1", "broker-2", "cl-01", "cl-02");

        // when
        matchingEngine.processOrder(sellOrder1);
        matchingEngine.processOrder(buyOrder);
        matchingEngine.processOrder(sellOrder2);
        matchingEngine.processOrder(sellOrder3);
        matchingEngine.processOrder(sellOrder4);

        // then
        assertThat(matchingEngine.getOrderBook().getBuyEntries(), is(partialBuyOrders));
        assertThat(matchingEngine.getOrderBook().getSellEntries().size(), is(0));
        verify(transactionRegister).register(tx);
        verify(transactionRegister).register(tx2);
        verify(transactionRegister).register(tx3);
        verify(transactionRegister).register(tx4);
        verifyNoMoreInteractions(transactionRegister);
    }

}