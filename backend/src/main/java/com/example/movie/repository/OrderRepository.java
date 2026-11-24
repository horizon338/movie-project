package com.example.movie.repository;

import com.example.movie.entity.Order;
import com.example.movie.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);

    List<Order> findByStatusAndExpireAtBefore(OrderStatus status, LocalDateTime time);

    @Query("""
            select count(o) from Order o
            where o.user.id = :userId and o.showtime.movie.id = :movieId and o.status = :status
            """)
    long countByUserAndMovieAndStatus(@Param("userId") Long userId,
                                      @Param("movieId") Long movieId,
                                      @Param("status") OrderStatus status);

    @Query("""
            select coalesce(sum(o.totalAmount), 0) from Order o
            where o.status = :status and o.paidAt >= :from
            """)
    java.math.BigDecimal sumAmountByStatusSince(@Param("status") OrderStatus status,
                                                @Param("from") LocalDateTime from);

    @Query("select count(o) from Order o where o.status = :status and o.paidAt >= :from")
    long countByStatusSince(@Param("status") OrderStatus status, @Param("from") LocalDateTime from);

    @Query("""
            select m.id, m.title,
                   coalesce(sum(case when o.status = :paid then o.totalAmount
                                     when o.status = :refunded then -o.totalAmount else 0 end), 0),
                   coalesce(sum(case when o.status = :paid then 1
                                     when o.status = :refunded then -1 else 0 end), 0)
            from Order o join o.showtime st join st.movie m
            where o.status in (:paid, :refunded)
            group by m.id, m.title
            """)
    List<Object[]> statsByMovie(@Param("paid") OrderStatus paid,
                                @Param("refunded") OrderStatus refunded);

    @Query("""
            select c.id, c.name,
                   coalesce(sum(case when o.status = :paid then o.totalAmount
                                     when o.status = :refunded then -o.totalAmount else 0 end), 0),
                   coalesce(sum(case when o.status = :paid then 1
                                     when o.status = :refunded then -1 else 0 end), 0)
            from Order o join o.showtime st join st.hall h join h.cinema c
            where o.status in (:paid, :refunded)
            group by c.id, c.name
            """)
    List<Object[]> statsByCinema(@Param("paid") OrderStatus paid,
                                 @Param("refunded") OrderStatus refunded);

    @Query("""
            select function('date', o.paidAt),
                   coalesce(sum(case when o.status = :paid then o.totalAmount
                                     when o.status = :refunded then -o.totalAmount else 0 end), 0)
            from Order o
            where o.status in (:paid, :refunded) and o.paidAt >= :from
            group by function('date', o.paidAt)
            order by function('date', o.paidAt)
            """)
    List<Object[]> dailyRevenue(@Param("paid") OrderStatus paid,
                                @Param("refunded") OrderStatus refunded,
                                @Param("from") LocalDateTime from);
}
