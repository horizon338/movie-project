package com.example.movie.repository;

import com.example.movie.entity.OrderSeat;
import com.example.movie.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface OrderSeatRepository extends JpaRepository<OrderSeat, Long> {

    List<OrderSeat> findByOrderId(Long orderId);

    @Query("select os from OrderSeat os join fetch os.seat where os.order.id in :orderIds")
    List<OrderSeat> findWithSeatByOrderIds(@Param("orderIds") Collection<Long> orderIds);

    @Query("""
            select os.order.showtime.movie.id, count(os) from OrderSeat os
            where os.order.status = :status group by os.order.showtime.movie.id
            """)
    List<Object[]> countSeatsByMovie(@Param("status") OrderStatus status);

    @Query("""
            select os.order.showtime.hall.cinema.id, count(os) from OrderSeat os
            where os.order.status = :status group by os.order.showtime.hall.cinema.id
            """)
    List<Object[]> countSeatsByCinema(@Param("status") OrderStatus status);

    @Query("select count(os) from OrderSeat os where os.order.status = :status")
    long countByOrderStatus(@Param("status") OrderStatus status);
}
