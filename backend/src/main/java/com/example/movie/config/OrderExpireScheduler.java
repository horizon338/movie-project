package com.example.movie.config;

import com.example.movie.entity.Order;
import com.example.movie.entity.OrderStatus;
import com.example.movie.repository.OrderRepository;
import com.example.movie.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 每分钟释放超时未支付订单占用的座位
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpireScheduler {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    @Transactional
    public void releaseExpiredOrders() {
        List<Order> expired = orderRepository.findByStatusAndExpireAtBefore(
                OrderStatus.CREATED, LocalDateTime.now());
        if (expired.isEmpty()) {
            return;
        }
        orderService.expireOrders(expired);
        log.info("已释放 {} 笔超时未支付订单的座位", expired.size());
    }
}
