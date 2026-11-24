package com.example.movie.service;

import com.example.movie.common.BusinessException;
import com.example.movie.common.PageResult;
import com.example.movie.dto.CreateOrderRequest;
import com.example.movie.dto.OrderVO;
import com.example.movie.dto.TicketVO;
import com.example.movie.entity.Order;
import com.example.movie.entity.OrderSeat;
import com.example.movie.entity.OrderStatus;
import com.example.movie.entity.Payment;
import com.example.movie.entity.Seat;
import com.example.movie.entity.Showtime;
import com.example.movie.entity.User;
import com.example.movie.repository.OrderRepository;
import com.example.movie.repository.OrderSeatRepository;
import com.example.movie.repository.PaymentRepository;
import com.example.movie.repository.SeatRepository;
import com.example.movie.repository.ShowtimeRepository;
import com.example.movie.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderSeatRepository orderSeatRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final QrCodeService qrCodeService;

    @Value("${app.order.pay-timeout-minutes:30}")
    private long payTimeoutMinutes;

    @Value("${app.order.refund-advance-hours:2}")
    private long refundAdvanceHours;

    @Transactional
    public OrderVO create(Long userId, CreateOrderRequest request) {
        Showtime showtime = showtimeRepository.findById(request.showtimeId())
                .orElseThrow(() -> new BusinessException(404, "场次不存在"));
        if (showtime.getStatus() == null || showtime.getStatus() != 1) {
            throw new BusinessException("该场次已停售");
        }
        if (!showtime.getStartTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException("该场次已开场，无法购票");
        }

        List<Long> seatIds = request.seatIds().stream().distinct().toList();
        if (seatIds.size() != request.seatIds().size()) {
            throw new BusinessException("存在重复座位");
        }
        List<Seat> seats = seatRepository.findAllById(seatIds);
        if (seats.size() != seatIds.size()) {
            throw new BusinessException("座位不存在，请重新选座");
        }
        for (Seat seat : seats) {
            if (!seat.getShowtime().getId().equals(showtime.getId())) {
                throw new BusinessException("座位与场次不匹配");
            }
        }

        int locked = seatRepository.lockSeats(seatIds);
        if (locked != seatIds.size()) {
            throw new BusinessException(409, "所选座位已被他人锁定或售出，请重新选座");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUser(user);
        order.setShowtime(showtime);
        order.setTotalAmount(showtime.getPrice().multiply(BigDecimal.valueOf(seatIds.size())));
        order.setStatus(OrderStatus.CREATED);
        order.setExpireAt(LocalDateTime.now().plusMinutes(payTimeoutMinutes));
        orderRepository.save(order);

        List<Seat> sortedSeats = seats.stream()
                .sorted(Comparator.comparing(Seat::getRowNo).thenComparing(Seat::getColNo))
                .toList();
        List<OrderSeat> orderSeats = sortedSeats.stream().map(seat -> {
            OrderSeat item = new OrderSeat();
            item.setOrder(order);
            item.setSeat(seat);
            item.setPrice(showtime.getPrice());
            return item;
        }).toList();
        orderSeatRepository.saveAll(orderSeats);

        return toVO(order, orderSeats);
    }

    @Transactional(readOnly = true)
    public PageResult<OrderVO> myOrders(Long userId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0),
                Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> result;
        if (status == null || status.isBlank()) {
            result = orderRepository.findByUserId(userId, pageable);
        } else {
            result = orderRepository.findByUserIdAndStatus(userId, parseStatus(status), pageable);
        }

        List<Long> orderIds = result.getContent().stream().map(Order::getId).toList();
        Map<Long, List<OrderSeat>> seatsByOrder = orderIds.isEmpty() ? Map.of()
                : orderSeatRepository.findWithSeatByOrderIds(orderIds).stream()
                .collect(Collectors.groupingBy(item -> item.getOrder().getId()));

        List<OrderVO> records = result.getContent().stream()
                .map(order -> toVO(order, seatsByOrder.getOrDefault(order.getId(), List.of())))
                .toList();
        return new PageResult<>(records, result.getTotalElements(), result.getNumber() + 1, result.getSize());
    }

    @Transactional(readOnly = true)
    public OrderVO detail(Long userId, Long orderId) {
        Order order = getOwnedOrder(userId, orderId);
        return toVO(order, orderSeatRepository.findByOrderId(orderId));
    }

    @Transactional
    public TicketVO pay(Long userId, Long orderId) {
        Order order = getOwnedOrder(userId, orderId);
        if (order.getStatus() == OrderStatus.PAID) {
            return toTicketVO(order, orderSeatRepository.findByOrderId(orderId));
        }
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException("当前订单状态不可支付");
        }
        if (order.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("订单已超时，请重新选座");
        }

        List<OrderSeat> orderSeats = orderSeatRepository.findByOrderId(orderId);
        List<Long> seatIds = orderSeats.stream().map(item -> item.getSeat().getId()).toList();
        int sold = seatRepository.sellSeats(seatIds);
        if (sold != seatIds.size()) {
            throw new BusinessException(409, "座位状态异常，支付失败");
        }

        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        order.setTicketCode(generateTicketCode());

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setChannel("MOCK");
        payment.setAmount(order.getTotalAmount());
        payment.setStatus("SUCCESS");
        payment.setTransactionNo("PAY" + System.currentTimeMillis()
                + ThreadLocalRandom.current().nextInt(100, 999));
        paymentRepository.save(payment);

        return toTicketVO(order, orderSeats);
    }

    @Transactional
    public OrderVO cancel(Long userId, Long orderId) {
        Order order = getOwnedOrder(userId, orderId);
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException("当前订单不可取消");
        }
        List<OrderSeat> orderSeats = orderSeatRepository.findByOrderId(orderId);
        List<Long> seatIds = orderSeats.stream().map(item -> item.getSeat().getId()).toList();
        seatRepository.releaseSeats(seatIds);
        order.setStatus(OrderStatus.CANCELLED);
        return toVO(order, orderSeats);
    }

    @Transactional(readOnly = true)
    public TicketVO ticket(Long userId, Long orderId) {
        Order order = getOwnedOrder(userId, orderId);
        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException("订单尚未支付，暂不可查看电子票");
        }
        return toTicketVO(order, orderSeatRepository.findByOrderId(orderId));
    }

    /**
     * 退票：仅已支付且距开场不少于 2 小时可退，座位重新可售
     */
    @Transactional
    public OrderVO refund(Long userId, Long orderId) {
        Order order = getOwnedOrder(userId, orderId);
        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException("仅已出票订单可申请退票");
        }
        if (order.getShowtime().getStartTime().isBefore(LocalDateTime.now().plusHours(refundAdvanceHours))) {
            throw new BusinessException("距开场不足 " + refundAdvanceHours + " 小时，无法退票");
        }

        List<OrderSeat> orderSeats = orderSeatRepository.findByOrderId(orderId);
        List<Long> seatIds = orderSeats.stream().map(item -> item.getSeat().getId()).toList();
        int reopened = seatRepository.reopenSeats(seatIds);
        if (reopened != seatIds.size()) {
            throw new BusinessException(409, "座位状态异常，退票失败");
        }

        order.setStatus(OrderStatus.REFUNDED);
        order.setRefundedAt(LocalDateTime.now());

        Payment refund = new Payment();
        refund.setOrder(order);
        refund.setChannel("MOCK");
        refund.setAmount(order.getTotalAmount().negate());
        refund.setStatus("REFUND");
        refund.setTransactionNo("REF" + System.currentTimeMillis()
                + ThreadLocalRandom.current().nextInt(100, 999));
        paymentRepository.save(refund);

        return toVO(order, orderSeats);
    }

    private Order getOwnedOrder(Long userId, Long orderId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
    }

    private OrderStatus parseStatus(String status) {
        try {
            return OrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的订单状态");
        }
    }

    private OrderVO toVO(Order order, List<OrderSeat> orderSeats) {
        Showtime showtime = order.getShowtime();
        List<OrderVO.SeatLabelVO> seats = orderSeats.stream()
                .map(item -> new OrderVO.SeatLabelVO(
                        item.getSeat().getId(),
                        item.getSeat().getRowNo(),
                        item.getSeat().getColNo(),
                        item.getSeat().getSeatType().name()))
                .toList();
        return new OrderVO(
                order.getId(),
                order.getOrderNo(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getExpireAt(),
                order.getPaidAt(),
                order.getRefundedAt(),
                showtime.getMovie().getId(),
                showtime.getMovie().getTitle(),
                showtime.getMovie().getPosterUrl(),
                showtime.getHall().getCinema().getName(),
                showtime.getHall().getName(),
                showtime.getStartTime(),
                order.getTicketCode(),
                seats
        );
    }

    private TicketVO toTicketVO(Order order, List<OrderSeat> orderSeats) {
        OrderVO vo = toVO(order, orderSeats);
        String content = "MOVIE-TICKET|" + order.getOrderNo() + "|" + order.getTicketCode();
        return new TicketVO(
                vo.id(),
                vo.orderNo(),
                vo.movieTitle(),
                vo.posterUrl(),
                vo.cinemaName(),
                vo.hallName(),
                vo.startTime(),
                vo.seats(),
                order.getTicketCode(),
                qrCodeService.generateBase64(content, 320)
        );
    }

    private String generateOrderNo() {
        return "MT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(1000, 10000);
    }

    private String generateTicketCode() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    /**
     * 供定时任务调用：释放超时订单座位
     */
    @Transactional
    public List<Order> expireOrders(List<Order> orders) {
        List<Order> changed = new ArrayList<>();
        for (Order order : orders) {
            List<OrderSeat> orderSeats = orderSeatRepository.findByOrderId(order.getId());
            List<Long> seatIds = orderSeats.stream().map(item -> item.getSeat().getId()).toList();
            if (!seatIds.isEmpty()) {
                seatRepository.releaseSeats(seatIds);
            }
            order.setStatus(OrderStatus.EXPIRED);
            changed.add(order);
        }
        return changed;
    }
}
