package com.example.movie.repository;

import com.example.movie.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    @Query("""
            select st from Showtime st
            join fetch st.hall h
            join fetch h.cinema c
            join fetch st.movie m
            where st.movie.id = :movieId
              and st.status = 1
              and st.startTime >= :from and st.startTime < :to
              and (:cinemaId is null or c.id = :cinemaId)
            order by c.id, st.startTime
            """)
    List<Showtime> findForBooking(@Param("movieId") Long movieId,
                                  @Param("cinemaId") Long cinemaId,
                                  @Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to);

    @Query("""
            select st from Showtime st
            join fetch st.movie m
            join fetch st.hall h
            join fetch h.cinema c
            where (:movieId is null or m.id = :movieId)
              and (:cinemaId is null or c.id = :cinemaId)
              and st.startTime >= :from and st.startTime < :to
            order by st.startTime
            """)
    List<Showtime> findAdmin(@Param("movieId") Long movieId,
                             @Param("cinemaId") Long cinemaId,
                             @Param("from") LocalDateTime from,
                             @Param("to") LocalDateTime to);

    List<Showtime> findByHallIdAndStartTimeLessThanAndEndTimeGreaterThan(
            Long hallId, LocalDateTime end, LocalDateTime start);
}
