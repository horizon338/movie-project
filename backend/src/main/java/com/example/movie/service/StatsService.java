package com.example.movie.service;

import com.example.movie.dto.admin.BoxOfficeVO;
import com.example.movie.entity.OrderStatus;
import com.example.movie.repository.OrderRepository;
import com.example.movie.repository.OrderSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final OrderRepository orderRepository;
    private final OrderSeatRepository orderSeatRepository;

    @Transactional(readOnly = true)
    public BoxOfficeVO boxOffice(int days) {
        int range = Math.min(Math.max(days, 1), 90);
        OrderStatus paid = OrderStatus.PAID;
        OrderStatus refunded = OrderStatus.REFUNDED;
        LocalDateTime epoch = LocalDateTime.of(2000, 1, 1, 0, 0);

        BigDecimal totalRevenue = orderRepository.sumAmountByStatusSince(paid, epoch)
                .subtract(orderRepository.sumAmountByStatusSince(refunded, epoch));
        long paidOrders = orderRepository.countByStatusSince(paid, epoch);
        long refundedOrders = orderRepository.countByStatusSince(refunded, epoch);
        long ticketCount = Math.max(orderSeatRepository.countByOrderStatus(paid)
                - orderSeatRepository.countByOrderStatus(refunded), 0);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        BigDecimal todayRevenue = orderRepository.sumAmountByStatusSince(paid, todayStart)
                .subtract(orderRepository.sumAmountByStatusSince(refunded, todayStart));

        Map<Long, Long> paidSeatsByMovie = toSeatMap(orderSeatRepository.countSeatsByMovie(paid));
        Map<Long, Long> refundedSeatsByMovie = toSeatMap(orderSeatRepository.countSeatsByMovie(refunded));
        List<BoxOfficeVO.MovieStat> movieStats = new ArrayList<>();
        for (Object[] row : orderRepository.statsByMovie(paid, refunded)) {
            Long movieId = ((Number) row[0]).longValue();
            String title = (String) row[1];
            BigDecimal revenue = (BigDecimal) row[2];
            long orders = ((Number) row[3]).longValue();
            long tickets = Math.max(paidSeatsByMovie.getOrDefault(movieId, 0L)
                    - refundedSeatsByMovie.getOrDefault(movieId, 0L), 0);
            movieStats.add(new BoxOfficeVO.MovieStat(movieId, title, revenue, orders, tickets));
        }
        movieStats.sort(Comparator.comparing(BoxOfficeVO.MovieStat::revenue).reversed());

        List<BoxOfficeVO.CinemaStat> cinemaStats = new ArrayList<>();
        for (Object[] row : orderRepository.statsByCinema(paid, refunded)) {
            cinemaStats.add(new BoxOfficeVO.CinemaStat(
                    ((Number) row[0]).longValue(),
                    (String) row[1],
                    (BigDecimal) row[2],
                    ((Number) row[3]).longValue()));
        }
        cinemaStats.sort(Comparator.comparing(BoxOfficeVO.CinemaStat::revenue).reversed());

        LocalDate fromDate = LocalDate.now().minusDays(range - 1L);
        Map<LocalDate, BigDecimal> daily = new HashMap<>();
        for (Object[] row : orderRepository.dailyRevenue(paid, refunded, fromDate.atStartOfDay())) {
            daily.put(toLocalDate(row[0]), (BigDecimal) row[1]);
        }
        List<BoxOfficeVO.DailyStat> dailyStats = new ArrayList<>();
        for (int i = 0; i < range; i++) {
            LocalDate date = fromDate.plusDays(i);
            dailyStats.add(new BoxOfficeVO.DailyStat(date.toString(),
                    daily.getOrDefault(date, BigDecimal.ZERO)));
        }

        return new BoxOfficeVO(totalRevenue, paidOrders, refundedOrders, ticketCount,
                todayRevenue, movieStats, cinemaStats, dailyStats);
    }

    private Map<Long, Long> toSeatMap(List<Object[]> rows) {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return map;
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate date) {
            return date;
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate();
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }
}
