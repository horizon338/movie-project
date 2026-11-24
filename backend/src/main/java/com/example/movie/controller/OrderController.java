package com.example.movie.controller;

import com.example.movie.common.BusinessException;
import com.example.movie.common.PageResult;
import com.example.movie.common.Result;
import com.example.movie.dto.CreateOrderRequest;
import com.example.movie.dto.OrderVO;
import com.example.movie.dto.TicketVO;
import com.example.movie.security.SecurityUtils;
import com.example.movie.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<OrderVO> create(@Valid @RequestBody CreateOrderRequest request) {
        return Result.ok("下单成功，请在 30 分钟内完成支付", orderService.create(currentUserId(), request));
    }

    @GetMapping
    public Result<PageResult<OrderVO>> myOrders(@RequestParam(required = false) String status,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return Result.ok(orderService.myOrders(currentUserId(), status, page, size));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.ok(orderService.detail(currentUserId(), id));
    }

    @PostMapping("/{id}/pay")
    public Result<TicketVO> pay(@PathVariable Long id) {
        return Result.ok("支付成功", orderService.pay(currentUserId(), id));
    }

    @PostMapping("/{id}/cancel")
    public Result<OrderVO> cancel(@PathVariable Long id) {
        return Result.ok("订单已取消", orderService.cancel(currentUserId(), id));
    }

    @PostMapping("/{id}/refund")
    public Result<OrderVO> refund(@PathVariable Long id) {
        return Result.ok("退票成功，款项将原路退回", orderService.refund(currentUserId(), id));
    }

    @GetMapping("/{id}/ticket")
    public Result<TicketVO> ticket(@PathVariable Long id) {
        return Result.ok(orderService.ticket(currentUserId(), id));
    }

    private Long currentUserId() {
        Long userId = SecurityUtils.currentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return userId;
    }
}
