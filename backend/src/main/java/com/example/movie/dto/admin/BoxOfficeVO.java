package com.example.movie.dto.admin;

import java.math.BigDecimal;
import java.util.List;

public record BoxOfficeVO(
        BigDecimal totalRevenue,
        long paidOrders,
        long refundedOrders,
        long ticketCount,
        BigDecimal todayRevenue,
        List<MovieStat> movieStats,
        List<CinemaStat> cinemaStats,
        List<DailyStat> dailyStats
) {
    public record MovieStat(Long movieId, String title, BigDecimal revenue, long orders, long tickets) {
    }

    public record CinemaStat(Long cinemaId, String name, BigDecimal revenue, long orders) {
    }

    public record DailyStat(String date, BigDecimal revenue) {
    }
}
