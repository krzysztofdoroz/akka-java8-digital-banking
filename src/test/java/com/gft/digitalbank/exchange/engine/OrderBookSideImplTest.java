package com.gft.digitalbank.exchange.engine;

import com.gft.digitalbank.exchange.domain.CancellationOrder;
import com.gft.digitalbank.exchange.domain.ModificationOrder;
import com.gft.digitalbank.exchange.domain.Order;
import com.gft.digitalbank.exchange.domain.Side;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class OrderBookSideImplTest {

    OrderBookSide orderBookSide;

    @Before
    public void init() {

    }

    @Test
    public void testAddingBuySideOrder() {
        // given
        orderBookSide = new OrderBookSideImpl(Side.BUY);

        // when
        orderBookSide.addOrder(new Order(1, Side.BUY, 100, 1, 1000, "broker-1", "cl-01"));

        // then
        assertEquals(1, orderBookSide.getOrders().size());
    }

    @Test
    public void testAddingSellSideOrder() {
        // given
        orderBookSide = new OrderBookSideImpl(Side.SELL);

        // when
        orderBookSide.addOrder(new Order(1, Side.SELL, 100, 1, 1000, "broker-1", "cl-01"));

        // then
        assertEquals(1, orderBookSide.getOrders().size());
    }


    @Test
    public void testBuySideOrdersOrderingByPriceDesc() {
        // given
        orderBookSide = new OrderBookSideImpl(Side.BUY);

        Order order1 = new Order(1, Side.BUY, 110, 1, 1000, "broker-1", "cl-01");
        Order order2 = new Order(2, Side.BUY, 200, 2, 1000, "broker-1", "cl-01");
        Order order3 = new Order(3, Side.BUY, 190, 3, 1000, "broker-1", "cl-01");
        List<Order> sortedOrders = Arrays.asList(order2, order3, order1);

        // when
        orderBookSide.addOrder(order1);
        orderBookSide.addOrder(order2);
        orderBookSide.addOrder(order3);

        // then
        assertThat(orderBookSide.getOrders(), is(sortedOrders));
    }

    @Test
    public void testSellSideOrdersOrderingByPriceAsc() {
        // given
        orderBookSide = new OrderBookSideImpl(Side.SELL);

        Order order1 = new Order(1, Side.SELL, 110, 1, 1000, "broker-1", "cl-01");
        Order order2 = new Order(2, Side.SELL, 200, 2, 1000, "broker-1", "cl-01");
        Order order3 = new Order(3, Side.SELL, 190, 3, 1000, "broker-1", "cl-01");
        List<Order> sortedOrders = Arrays.asList(order1, order3, order2);

        // when
        orderBookSide.addOrder(order1);
        orderBookSide.addOrder(order2);
        orderBookSide.addOrder(order3);

        // then
        assertThat(orderBookSide.getOrders(), is(sortedOrders));
    }

    @Test
    public void testBuySideOrdersOrderingByPriceDescTimestampAsc() {
        // given
        orderBookSide = new OrderBookSideImpl(Side.BUY);

        Order order1 = new Order(1, Side.BUY, 210, 11, 1000, "broker-1", "cl-01");
        Order order2 = new Order(2, Side.BUY, 210, 22, 1000, "broker-1", "cl-01");
        Order order3 = new Order(3, Side.BUY, 210, 3, 1000,"broker-1", "cl-01");
        List<Order> sortedOrders = Arrays.asList(order3, order1, order2);

        // when
        orderBookSide.addOrder(order1);
        orderBookSide.addOrder(order2);
        orderBookSide.addOrder(order3);

        // then
        assertThat(orderBookSide.getOrders(), is(sortedOrders));
    }

    @Test
    public void testSellSideOrdersOrderingByPriceAscTimestampAsc() {
        // given
        orderBookSide = new OrderBookSideImpl(Side.SELL);

        Order order1 = new Order(1, Side.SELL, 210, 11, 1000, "broker-1", "cl-01");
        Order order2 = new Order(2, Side.SELL, 210, 22, 1000, "broker-1", "cl-01");
        Order order3 = new Order(3, Side.SELL, 210, 3, 1000,"broker-1", "cl-01");
        List<Order> sortedOrders = Arrays.asList(order3, order1, order2);

        // when
        orderBookSide.addOrder(order1);
        orderBookSide.addOrder(order2);
        orderBookSide.addOrder(order3);

        // then
        assertThat(orderBookSide.getOrders(), is(sortedOrders));
    }

    @Test
    public void testCancellingSingleBuyOrder() {
        // given
        orderBookSide = new OrderBookSideImpl(Side.BUY);
        CancellationOrder cancellationOrder = new CancellationOrder(2, "broker-1", 2);
        Order order1 = new Order(1, Side.BUY, 110, 1, 1000, "broker-1", "cl-01");
        Order order2 = new Order(2, Side.BUY, 200, 2, 1000, "broker-1", "cl-01");
        Order order3 = new Order(3, Side.BUY, 190, 3, 1000, "broker-1", "cl-01");
        List<Order> expectedOrders = Arrays.asList(order3, order1);

        // when
        orderBookSide.addOrder(order1);
        orderBookSide.addOrder(order2);
        orderBookSide.addOrder(order3);
        orderBookSide.cancelOrder(cancellationOrder);

        // then
        assertThat(orderBookSide.getOrders(), is(expectedOrders));
    }

    @Test
    public void testCancellingSingleSellOrder() {
        // given
        orderBookSide = new OrderBookSideImpl(Side.SELL);
        CancellationOrder cancellationOrder = new CancellationOrder(2, "broker-1", 2);
        Order order1 = new Order(1, Side.SELL, 110, 1, 1000, "broker-1", "cl-01");
        Order order2 = new Order(2, Side.SELL, 200, 2, 1000, "broker-1", "cl-01");
        Order order3 = new Order(3, Side.SELL, 190, 3, 1000, "broker-1", "cl-01");
        List<Order> expectedOrders = Arrays.asList(order1, order3);

        // when
        orderBookSide.addOrder(order1);
        orderBookSide.addOrder(order2);
        orderBookSide.addOrder(order3);
        orderBookSide.cancelOrder(cancellationOrder);

        // then
        assertThat(orderBookSide.getOrders(), is(expectedOrders));
    }


    @Test
    public void testCancellingMultipleBuyOrders() {
        // given
        orderBookSide = new OrderBookSideImpl(Side.BUY);
        CancellationOrder cancellationOrder2 = new CancellationOrder(2, "broker-1", 2);
        CancellationOrder cancellationOrder3 = new CancellationOrder(3, "broker-1", 3);
        Order order1 = new Order(1, Side.BUY, 110, 1, 1000, "broker-1", "cl-01");
        Order order2 = new Order(2, Side.BUY, 200, 2, 1000, "broker-1", "cl-01");
        Order order3 = new Order(3, Side.BUY, 190, 3, 1000, "broker-1", "cl-01");
        List<Order> expectedOrders = Arrays.asList(order1);

        // when
        orderBookSide.addOrder(order1);
        orderBookSide.addOrder(order2);
        orderBookSide.addOrder(order3);
        orderBookSide.cancelOrder(cancellationOrder2);
        orderBookSide.cancelOrder(cancellationOrder3);

        // then
        assertThat(orderBookSide.getOrders(), is(expectedOrders));
    }

    @Test
    public void testCancellingMultipleSellOrders() {
        // given
        orderBookSide = new OrderBookSideImpl(Side.SELL);
        CancellationOrder cancellationOrder2 = new CancellationOrder(2, "broker-1", 2);
        CancellationOrder cancellationOrder3 = new CancellationOrder(3, "broker-1", 3);
        Order order1 = new Order(1, Side.SELL, 110, 1, 1000, "broker-1", "cl-01");
        Order order2 = new Order(2, Side.SELL, 200, 2, 1000, "broker-1", "cl-01");
        Order order3 = new Order(3, Side.SELL, 190, 3, 1000, "broker-1", "cl-01");
        List<Order> expectedOrders = Arrays.asList(order1);

        // when
        orderBookSide.addOrder(order1);
        orderBookSide.addOrder(order2);
        orderBookSide.addOrder(order3);
        orderBookSide.cancelOrder(cancellationOrder2);
        orderBookSide.cancelOrder(cancellationOrder3);

        // then
        assertThat(orderBookSide.getOrders(), is(expectedOrders));
    }


    @Test
    public void testCancellingNonExistingOrder() {
        // given
        orderBookSide = new OrderBookSideImpl(Side.BUY);
        CancellationOrder cancellationOrder = new CancellationOrder(4, "broker-1", 4);

        // when
        orderBookSide.cancelOrder(cancellationOrder);

        // then
        assertThat(orderBookSide.getOrders().size(), is(0));
    }

    @Test
    public void testModifyingSingleBuyOrder() {
        // given
        orderBookSide = new OrderBookSideImpl(Side.BUY);
        ModificationOrder modificationOrder = new ModificationOrder(4, 3, "broker-1", 120, 290, 4);
        Order order1 = new Order(1, Side.BUY, 110, 1, 1000, "broker-1", "cl-01");
        Order order2 = new Order(2, Side.BUY, 200, 2, 1000, "broker-1", "cl-01");
        Order order3 = new Order(3, Side.BUY, 190, 3, 1000, "broker-1", "cl-01");
        Order order3AfterModification = new Order(3, Side.BUY, 290, 4, 120, "broker-1", "cl-01");

        List<Order> expectedOrders = Arrays.asList(order3AfterModification, order2, order1);

        // when
        orderBookSide.addOrder(order1);
        orderBookSide.addOrder(order2);
        orderBookSide.addOrder(order3);
        orderBookSide.modifyOrder(modificationOrder);

        // then
        assertThat(orderBookSide.getOrders(), is(expectedOrders));
    }

    @Test
    public void testModifyingSingleSellOrder() {
        // given
        orderBookSide = new OrderBookSideImpl(Side.SELL);
        ModificationOrder modificationOrder = new ModificationOrder(4, 3, "broker-1", 120, 290, 4);
        Order order1 = new Order(1, Side.SELL, 110, 1, 1000, "broker-1", "cl-01");
        Order order2 = new Order(2, Side.SELL, 200, 2, 1000, "broker-1", "cl-01");
        Order order3 = new Order(3, Side.SELL, 190, 3, 1000, "broker-1", "cl-01");
        Order order3AfterModification = new Order(3, Side.SELL, 290, 4, 120, "broker-1", "cl-01");

        List<Order> expectedOrders = Arrays.asList(order1, order2, order3AfterModification);

        // when
        orderBookSide.addOrder(order1);
        orderBookSide.addOrder(order2);
        orderBookSide.addOrder(order3);
        orderBookSide.modifyOrder(modificationOrder);

        // then
        assertThat(orderBookSide.getOrders(), is(expectedOrders));
    }

    @Test
    public void testGettingTopSellOrder() {
        // given
        orderBookSide = new OrderBookSideImpl(Side.SELL);
        Order order1 = new Order(1, Side.SELL, 110, 1, 1000, "broker-1", "cl-01");
        Order order2 = new Order(2, Side.SELL, 200, 2, 1000, "broker-1", "cl-01");
        Order order3 = new Order(3, Side.SELL, 190, 3, 1000, "broker-1", "cl-01");

        // when
        orderBookSide.addOrder(order1);
        orderBookSide.addOrder(order2);
        orderBookSide.addOrder(order3);

        // then
        assertThat(orderBookSide.getTopOrder().get(), is(order1));
    }

    @Test
    public void testGettingTopBuyOrder() {
        // given
        orderBookSide = new OrderBookSideImpl(Side.BUY);
        Order order1 = new Order(1, Side.BUY, 110, 1, 1000, "broker-1", "cl-01");
        Order order2 = new Order(2, Side.BUY, 200, 2, 1000, "broker-1", "cl-01");
        Order order3 = new Order(3, Side.BUY, 190, 3, 1000, "broker-1", "cl-01");

        // when
        orderBookSide.addOrder(order1);
        orderBookSide.addOrder(order2);
        orderBookSide.addOrder(order3);

        // then
        assertThat(orderBookSide.getTopOrder().get(), is(order2));
    }
}