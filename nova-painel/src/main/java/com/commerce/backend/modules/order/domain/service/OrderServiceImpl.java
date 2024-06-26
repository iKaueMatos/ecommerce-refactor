package com.commerce.backend.modules.order.domain.service;

import com.commerce.backend.modules.cart.infra.entity.Cart;
import com.commerce.backend.core.error.exception.InvalidArgumentException;
import com.commerce.backend.core.error.exception.ResourceFetchException;
import com.commerce.backend.modules.order.application.converter.OrderResponseConverter;
import com.commerce.backend.modules.order.application.service.CartService;
import com.commerce.backend.modules.order.application.service.OrderService;
import com.commerce.backend.modules.order.application.useCases.dto.OrderResponse;
import com.commerce.backend.modules.order.application.useCases.dto.PostOrderRequest;
import com.commerce.backend.modules.order.infra.entity.Order;
import com.commerce.backend.modules.order.infra.entity.OrderDetail;
import com.commerce.backend.modules.order.infra.repository.OrderRepository;
import com.commerce.backend.modules.user.application.useCases.service.IUserService;
import com.commerce.backend.modules.user.infra.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final IUserService userService;
    private final CartService cartService;
    private final OrderResponseConverter orderResponseConverter;


    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            IUserService userService,
                            CartService cartService,
                            OrderResponseConverter orderResponseConverter) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.cartService = cartService;
        this.orderResponseConverter = orderResponseConverter;
    }

    @Override
    public Integer getAllOrdersCount() {
        User user = userService.getUser();
        return orderRepository.countAllByUser(user)
                .orElseThrow(() -> new ResourceFetchException("An error occurred whilst fetching orders count"));
    }

    @Override
    public List<OrderResponse> getAllOrders(Integer page, Integer pageSize) {
        User user = userService.getUser();
        List<Order> orders = orderRepository.findAllByUserOrderByDateDesc(user, PageRequest.of(page, pageSize));
        return orders
                .stream()
                .map(orderResponseConverter)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse postOrder(PostOrderRequest postOrderRequest) {
        User user = userService.getUser();
        Cart cart = user.getCart();
        if (Objects.isNull(cart) || Objects.isNull(cart.getCartItemList())) {
            throw new InvalidArgumentException("Cart is not valid");
        }

        if (cart.getCartItemList().stream().anyMatch(cartItem -> cartItem.getProductVariant().getStock() < cartItem.getAmount())) {
            throw new InvalidArgumentException("A product in your cart is out of stock.");
        }

        Order saveOrder = new Order();
        saveOrder.setUser(user);
        saveOrder.setShipName(postOrderRequest.getShipName());
        saveOrder.setPhone(postOrderRequest.getPhone());
        saveOrder.setShipAddress(postOrderRequest.getShipAddress());
        saveOrder.setBillingAddress(postOrderRequest.getBillingAddress());
        saveOrder.setCity(postOrderRequest.getCity());
        saveOrder.setCountry(postOrderRequest.getCountry());
        saveOrder.setState(postOrderRequest.getState());
        saveOrder.setZip(postOrderRequest.getZip());

        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        saveOrder.setDate(date);

        saveOrder.setOrderDetailList(new ArrayList<>());

        cart.getCartItemList().forEach(cartItem -> {
            cartItem.getProductVariant().setSellCount(cartItem.getProductVariant().getSellCount() + cartItem.getAmount());
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setAmount(cartItem.getAmount());
            orderDetail.setOrder(saveOrder);
            orderDetail.setProductVariant(cartItem.getProductVariant());
            saveOrder.getOrderDetailList().add(orderDetail);
        });

        saveOrder.setTotalPrice(cart.getTotalPrice());
        saveOrder.setTotalCargoPrice(cart.getTotalCargoPrice());
        saveOrder.setDiscount(cart.getDiscount());
        saveOrder.setShipped(0);


        Order order = orderRepository.save(saveOrder);
        cartService.emptyCart();
        return orderResponseConverter.apply(order);
    }
}
