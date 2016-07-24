package com.gft.digitalbank.exchange.engine;

import com.gft.digitalbank.exchange.domain.CancellationOrder;
import com.gft.digitalbank.exchange.domain.ModificationOrder;
import com.gft.digitalbank.exchange.domain.Order;
import com.gft.digitalbank.exchange.domain.Side;
import com.gft.digitalbank.exchange.engine.OrderBook;
import com.gft.digitalbank.exchange.engine.OrderBookImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class OrderBookImplTest {

    OrderBook orderBook;

    @Before
    public void init() {

    }

    @Test
    public void testAddingBuySideOrder() {
        // given
        orderBook = new OrderBookImpl(Side.BUY);

        // when
        orderBook.addOrder(new Order(1, Side.BUY, 100, 1, 1000, "broker-1"));

        // then
        assertEquals(1, orderBook.getOrders().size());
    }

    @Test
    public void testAddingSellSideOrder() {
        // given
        orderBook = new OrderBookImpl(Side.SELL);

        // when
        orderBook.addOrder(new Order(1, Side.SELL, 100, 1, 1000, "broker-1"));

        // then
        assertEquals(1, orderBook.getOrders().size());
    }


    @Test
    public void testBuySideOrdersOrderingByPriceDesc() {
        // given
        orderBook = new OrderBookImpl(Side.BUY);

        Order order1 = new Order(1, Side.BUY, 110, 1, 1000, "broker-1");
        Order order2 = new Order(2, Side.BUY, 200, 2, 1000, "broker-1");
        Order order3 = new Order(3, Side.BUY, 190, 3, 1000, "broker-1");
        List<Order> sortedOrders = Arrays.asList(order2, order3, order1);

        // when
        orderBook.addOrder(order1);
        orderBook.addOrder(order2);
        orderBook.addOrder(order3);

        // then
        assertThat(orderBook.getOrders(), is(sortedOrders));
    }

    @Test
    public void testSellSideOrdersOrderingByPriceAsc() {
        // given
        orderBook = new OrderBookImpl(Side.SELL);

        Order order1 = new Order(1, Side.SELL, 110, 1, 1000, "broker-1");
        Order order2 = new Order(2, Side.SELL, 200, 2, 1000, "broker-1");
        Order order3 = new Order(3, Side.SELL, 190, 3, 1000, "broker-1");
        List<Order> sortedOrders = Arrays.asList(order1, order3, order2);

        // when
        orderBook.addOrder(order1);
        orderBook.addOrder(order2);
        orderBook.addOrder(order3);

        // then
        assertThat(orderBook.getOrders(), is(sortedOrders));
    }

    @Test
    public void testBuySideOrdersOrderingByPriceDescTimestampAsc() {
        // given
        orderBook = new OrderBookImpl(Side.BUY);

        Order order1 = new Order(1, Side.BUY, 210, 11, 1000, "broker-1");
        Order order2 = new Order(2, Side.BUY, 210, 22, 1000, "broker-1");
        Order order3 = new Order(3, Side.BUY, 210, 3, 1000,"broker-1");
        List<Order> sortedOrders = Arrays.asList(order3, order1, order2);

        // when
        orderBook.addOrder(order1);
        orderBook.addOrder(order2);
        orderBook.addOrder(order3);

        // then
        assertThat(orderBook.getOrders(), is(sortedOrders));
    }

    @Test
    public void testSellSideOrdersOrderingByPriceAscTimestampAsc() {
        // given
        orderBook = new OrderBookImpl(Side.SELL);

        Order order1 = new Order(1, Side.SELL, 210, 11, 1000, "broker-1");
        Order order2 = new Order(2, Side.SELL, 210, 22, 1000, "broker-1");
        Order order3 = new Order(3, Side.SELL, 210, 3, 1000,"broker-1");
        List<Order> sortedOrders = Arrays.asList(order3, order1, order2);

        // when
        orderBook.addOrder(order1);
        orderBook.addOrder(order2);
        orderBook.addOrder(order3);

        // then
        assertThat(orderBook.getOrders(), is(sortedOrders));
    }

    @Test
    public void testCancellingSingleBuyOrder() {
        // given
        orderBook = new OrderBookImpl(Side.BUY);
        CancellationOrder cancellationOrder = new CancellationOrder(2, "broker-1", 2);
        Order order1 = new Order(1, Side.BUY, 110, 1, 1000, "broker-1");
        Order order2 = new Order(2, Side.BUY, 200, 2, 1000, "broker-1");
        Order order3 = new Order(3, Side.BUY, 190, 3, 1000, "broker-1");
        List<Order> expectedOrders = Arrays.asList(order3, order1);

        // when
        orderBook.addOrder(order1);
        orderBook.addOrder(order2);
        orderBook.addOrder(order3);
        orderBook.cancelOrder(cancellationOrder);

        // then
        assertThat(orderBook.getOrders(), is(expectedOrders));
    }

    @Test
    public void testCancellingSingleSellOrder() {
        // given
        orderBook = new OrderBookImpl(Side.SELL);
        CancellationOrder cancellationOrder = new CancellationOrder(2, "broker-1", 2);
        Order order1 = new Order(1, Side.SELL, 110, 1, 1000, "broker-1");
        Order order2 = new Order(2, Side.SELL, 200, 2, 1000, "broker-1");
        Order order3 = new Order(3, Side.SELL, 190, 3, 1000, "broker-1");
        List<Order> expectedOrders = Arrays.asList(order1, order3);

        // when
        orderBook.addOrder(order1);
        orderBook.addOrder(order2);
        orderBook.addOrder(order3);
        orderBook.cancelOrder(cancellationOrder);

        // then
        assertThat(orderBook.getOrders(), is(expectedOrders));
    }


    @Test
    public void testCancellingMultipleBuyOrders() {
        // given
        orderBook = new OrderBookImpl(Side.BUY);
        CancellationOrder cancellationOrder2 = new CancellationOrder(2, "broker-1", 2);
        CancellationOrder cancellationOrder3 = new CancellationOrder(3, "broker-1", 3);
        Order order1 = new Order(1, Side.BUY, 110, 1, 1000, "broker-1");
        Order order2 = new Order(2, Side.BUY, 200, 2, 1000, "broker-1");
        Order order3 = new Order(3, Side.BUY, 190, 3, 1000, "broker-1");
        List<Order> expectedOrders = Arrays.asList(order1);

        // when
        orderBook.addOrder(order1);
        orderBook.addOrder(order2);
        orderBook.addOrder(order3);
        orderBook.cancelOrder(cancellationOrder2);
        orderBook.cancelOrder(cancellationOrder3);

        // then
        assertThat(orderBook.getOrders(), is(expectedOrders));
    }

    @Test
    public void testCancellingMultipleSellOrders() {
        // given
        orderBook = new OrderBookImpl(Side.SELL);
        CancellationOrder cancellationOrder2 = new CancellationOrder(2, "broker-1", 2);
        CancellationOrder cancellationOrder3 = new CancellationOrder(3, "broker-1", 3);
        Order order1 = new Order(1, Side.SELL, 110, 1, 1000, "broker-1");
        Order order2 = new Order(2, Side.SELL, 200, 2, 1000, "broker-1");
        Order order3 = new Order(3, Side.SELL, 190, 3, 1000, "broker-1");
        List<Order> expectedOrders = Arrays.asList(order1);

        // when
        orderBook.addOrder(order1);
        orderBook.addOrder(order2);
        orderBook.addOrder(order3);
        orderBook.cancelOrder(cancellationOrder2);
        orderBook.cancelOrder(cancellationOrder3);

        // then
        assertThat(orderBook.getOrders(), is(expectedOrders));
    }


    @Test
    public void testCancellingNonExistingOrder() {
        // given
        orderBook = new OrderBookImpl(Side.BUY);
        CancellationOrder cancellationOrder = new CancellationOrder(4, "broker-1", 4);

        // when
        orderBook.cancelOrder(cancellationOrder);

        // then
        assertThat(orderBook.getOrders().size(), is(0));
    }

    @Test
    public void testModifyingSingleBuyOrder() {
        // given
        orderBook = new OrderBookImpl(Side.BUY);
        ModificationOrder modificationOrder = new ModificationOrder(4, 3, "broker-1", 120, 290, 4);
        Order order1 = new Order(1, Side.BUY, 110, 1, 1000, "broker-1");
        Order order2 = new Order(2, Side.BUY, 200, 2, 1000, "broker-1");
        Order order3 = new Order(3, Side.BUY, 190, 3, 1000, "broker-1");
        Order order3AfterModification = new Order(3, Side.BUY, 290, 4, 120, "broker-1");

        List<Order> expectedOrders = Arrays.asList(order3AfterModification, order2, order1);

        // when
        orderBook.addOrder(order1);
        orderBook.addOrder(order2);
        orderBook.addOrder(order3);
        orderBook.modifyOrder(modificationOrder);

        // then
        assertThat(orderBook.getOrders(), is(expectedOrders));
    }

    @Test
    public void testModifyingSingleSellOrder() {
        // given
        orderBook = new OrderBookImpl(Side.SELL);
        ModificationOrder modificationOrder = new ModificationOrder(4, 3, "broker-1", 120, 290, 4);
        Order order1 = new Order(1, Side.SELL, 110, 1, 1000, "broker-1");
        Order order2 = new Order(2, Side.SELL, 200, 2, 1000, "broker-1");
        Order order3 = new Order(3, Side.SELL, 190, 3, 1000, "broker-1");
        Order order3AfterModification = new Order(3, Side.SELL, 290, 4, 120, "broker-1");

        List<Order> expectedOrders = Arrays.asList(order1, order2, order3AfterModification);

        // when
        orderBook.addOrder(order1);
        orderBook.addOrder(order2);
        orderBook.addOrder(order3);
        orderBook.modifyOrder(modificationOrder);

        // then
        assertThat(orderBook.getOrders(), is(expectedOrders));
    }

    @Test
    public void testGettingTopSellOrder() {
        // given
        orderBook = new OrderBookImpl(Side.SELL);
        Order order1 = new Order(1, Side.SELL, 110, 1, 1000, "broker-1");
        Order order2 = new Order(2, Side.SELL, 200, 2, 1000, "broker-1");
        Order order3 = new Order(3, Side.SELL, 190, 3, 1000, "broker-1");

        // when
        orderBook.addOrder(order1);
        orderBook.addOrder(order2);
        orderBook.addOrder(order3);

        // then
        assertThat(orderBook.getTopOrder(), is(order1));
    }

    @Test
    public void testGettingTopBuyOrder() {
        // given
        orderBook = new OrderBookImpl(Side.BUY);
        Order order1 = new Order(1, Side.BUY, 110, 1, 1000, "broker-1");
        Order order2 = new Order(2, Side.BUY, 200, 2, 1000, "broker-1");
        Order order3 = new Order(3, Side.BUY, 190, 3, 1000, "broker-1");

        // when
        orderBook.addOrder(order1);
        orderBook.addOrder(order2);
        orderBook.addOrder(order3);

        // then
        assertThat(orderBook.getTopOrder(), is(order2));
    }
}