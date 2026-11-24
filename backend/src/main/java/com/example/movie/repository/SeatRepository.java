package com.example.movie.repository;

import com.example.movie.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByShowtimeIdOrderByRowNoAscColNoAsc(Long showtimeId);

    /**
     * 条件更新占座：仅当座位可售时锁定，返回实际锁定数量
     */
    @Modifying
    @Query("update Seat s set s.status = 2 where s.id in :ids and s.status = 0")
    int lockSeats(@Param("ids") Collection<Long> ids);

    /**
     * 支付成功：锁定 -> 已售
     */
    @Modifying
    @Query("update Seat s set s.status = 1 where s.id in :ids and s.status = 2")
    int sellSeats(@Param("ids") Collection<Long> ids);

    /**
     * 取消/超时：锁定 -> 可售
     */
    @Modifying
    @Query("update Seat s set s.status = 0 where s.id in :ids and s.status = 2")
    int releaseSeats(@Param("ids") Collection<Long> ids);

    /**
     * 退票：已售 -> 可售
     */
    @Modifying
    @Query("update Seat s set s.status = 0 where s.id in :ids and s.status = 1")
    int reopenSeats(@Param("ids") Collection<Long> ids);


    /**
     * 批量统计各场次已售/锁定座位数，返回 [showtimeId, count]
     */
    @Query("""
            select s.showtime.id, count(s) from Seat s
            where s.showtime.id in :ids and s.status <> 0
            group by s.showtime.id
            """)
    List<Object[]> countUnavailableByShowtimeIds(@Param("ids") Collection<Long> ids);
}
