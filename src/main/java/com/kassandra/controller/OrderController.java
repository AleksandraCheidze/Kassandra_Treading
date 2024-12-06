package com.kassandra.controller;

import com.kassandra.domain.OrderType;
import com.kassandra.modal.Coin;
import com.kassandra.modal.Order;
import com.kassandra.modal.User;
import com.kassandra.request.CreateOrderRequest;
import com.kassandra.service.CoinService;
import com.kassandra.service.OrderService;
import com.kassandra.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private CoinService coinService;

//    @Autowired
//    private WalletTransactionService transactionService;

    @PostMapping("/pay")
    public ResponseEntity<Order> payOrderPayment(
            @RequestHeader("Authorization") String jwt,
            @RequestBody CreateOrderRequest req

            ) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        Coin coin = coinService.findById(req.getCoinId());

        Order order = orderService.processOrder(coin,
                req.getQuantity(),
                req.getOrderType(),
                user);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(
            @RequestHeader("Authorization") String jwtToken,
            @PathVariable Long orderId
    ) throws Exception {

        User user = userService.findUserProfileByJwt(jwtToken);

        Order order = orderService.getOrderById(orderId);
        if (order.getUser().getId().equals(user.getId())) {
            return ResponseEntity.ok(order);
        }else {
            throw new Exception("you don't have access");
        }
    }

    @GetMapping()
    public ResponseEntity<List<Order>> getAllOrdersOfUser(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(required = false)OrderType order_type,
            @RequestParam(required = false)String asset_symbol
    ) throws Exception {

        Long userId = userService.findUserProfileByJwt(jwt).getId();

        List<Order> userOrders = orderService.getAllOrdersOfUser(userId, order_type, asset_symbol);
        return ResponseEntity.ok(userOrders);
    }



}
