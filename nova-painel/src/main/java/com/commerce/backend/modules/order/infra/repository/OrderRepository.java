package com.commerce.backend.modules.order.infra.repository;

import com.commerce.backend.modules.order.infra.entity.Order;
import com.commerce.backend.modules.user.infra.entity.User;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends PagingAndSortingRepository<Order, Long> {
    List<Order> findAllByUserOrderByDateDesc(User user, Pageable pageable);
    Optional<Integer> countAllByUser(User user);
}
