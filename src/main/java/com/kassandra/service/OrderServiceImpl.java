package com.kassandra.service;

import com.kassandra.domain.OrderStatus;
import com.kassandra.domain.OrderType;
import com.kassandra.modal.Coin;
import com.kassandra.modal.Order;
import com.kassandra.modal.OrderItem;
import com.kassandra.modal.User;
import com.kassandra.repository.OrderItemRepository;
import com.kassandra.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Service
public class OrderServiceImpl implements OrderService{

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private WalletService walletService;
    @Override
    public Order createOrder(User user, OrderItem orderItem, OrderType orderType) {
        double price = orderItem.getCoin().getCurrentPrice()*orderItem.getQuantity();

        Order order = new Order();
        order.setUser(user);
        order.setOrderItem(orderItem);
        order.setOrderType(orderType);
        order.setPrice(BigDecimal.valueOf(price));
        order.setTimestamp(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        return orderRepository.save(order);
    }

    @Override
    public Order getOrderById(Long orderId) throws Exception {
        return orderRepository.findById(orderId)
                .orElseThrow(()-> new Exception("order not found"));
    }

    @Override
    public List<Order> getAllOrdersOfUser(Long userId, OrderType orderType, String assetSymbol) {
        return orderRepository.findByUserId(userId);
    }

    private OrderItem createOrderItem(Coin coin, double quantity, double buyPrice, double sellPrice){
        OrderItem orderItem = new OrderItem();
        orderItem.setQuantity(quantity);
        orderItem.setBuyPrice(buyPrice);
        orderItem.setCoin(coin);
        orderItem.setSellPrice(sellPrice);
        return orderItemRepository.save(orderItem);
    }

    public Order buyAsset(Coin coin, double quantity, User user) throws Exception {
        if (quantity<=0){
            throw new Exception("quantity should be >0");
        }
        double buyPrice = coin.getCurrentPrice();

        OrderItem orderItem = createOrderItem(coin, quantity, buyPrice, 0);

        Order order = createOrder(user, orderItem, OrderType.BUY);

    }

    @Override
    public Order processOrder(Coin coin, double quantity, OrderType orderType, User user) {
        return null;
    }
}
