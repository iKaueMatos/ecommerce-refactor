package com.commerce.backend.modules.order.application.useCases.controller;

import com.commerce.backend.core.controller.ApiController;
import com.commerce.backend.core.error.exception.InvalidArgumentException;
import com.commerce.backend.modules.order.application.service.OrderService;
import com.commerce.backend.modules.order.application.useCases.dto.OrderResponse;
import com.commerce.backend.modules.order.application.useCases.dto.PostOrderRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

@RestController
public class OrderController extends ApiController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping(value = "/order/count")
    public ResponseEntity<Integer> getAllOrdersCount() {
        Integer orderCount = orderService.getAllOrdersCount();
        return new ResponseEntity<>(orderCount, HttpStatus.OK);
    }

    @GetMapping(value = "/order")
    public ResponseEntity<List<OrderResponse>> getAllOrders(@RequestParam("page") Integer page, @RequestParam("size") Integer pageSize) {
        if (Objects.isNull(page) || page < 0) {
            throw new InvalidArgumentException("Invalid page");
        }
        if (Objects.isNull(pageSize) || pageSize < 0) {
            throw new InvalidArgumentException("Invalid pageSize");
        }
        List<OrderResponse> orders = orderService.getAllOrders(page, pageSize);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @PostMapping(value = "/order")
    public ResponseEntity<OrderResponse> postOrder(@RequestBody @Valid PostOrderRequest postOrderRequest) {
        OrderResponse orderResponse = orderService.postOrder(postOrderRequest);
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }

}
