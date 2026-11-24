package com.example.movie.repository;

import com.example.movie.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    @Query("""
            select m from Movie m
            where m.status = 1
              and m.releaseDate <= :today
              and (m.offShelfDate is null or m.offShelfDate >= :today)
              and (:keyword is null
                   or m.title like concat('%', :keyword, '%')
                   or m.actors like concat('%', :keyword, '%'))
              and (:releaseFrom is null or m.releaseDate >= :releaseFrom)
              and (:releaseTo is null or m.releaseDate <= :releaseTo)
              and (:cinemaId is null or exists (
                    select 1 from Showtime st
                    where st.movie = m and st.hall.cinema.id = :cinemaId
                      and st.startTime >= :now and st.status = 1))
            """)
    Page<Movie> search(@Param("keyword") String keyword,
                       @Param("releaseFrom") LocalDate releaseFrom,
                       @Param("releaseTo") LocalDate releaseTo,
                       @Param("cinemaId") Long cinemaId,
                       @Param("today") LocalDate today,
                       @Param("now") LocalDateTime now,
                       Pageable pageable);

    Page<Movie> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    List<Movie> findAllByOrderByCreatedAtDesc();
}
