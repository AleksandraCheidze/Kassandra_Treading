package com.kassandra.service;

import com.kassandra.domain.OrderType;
import com.kassandra.modal.Coin;
import com.kassandra.modal.Order;
import com.kassandra.modal.OrderItem;
import com.kassandra.modal.User;

import java.util.List;

public interface OrderService {

    Order createOrder(User user, OrderItem orderItem, OrderType orderType);

    Order getOrderById(Long orderId);

    List<Order> getAllOrdersOfUser(Long userId, OrderType orderType, String assetSymbol);

    Order processOrder(Coin coin, double quantity, OrderType orderType, User user);
}
