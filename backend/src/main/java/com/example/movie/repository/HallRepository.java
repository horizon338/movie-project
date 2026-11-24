package com.example.movie.repository;

import com.example.movie.entity.Hall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HallRepository extends JpaRepository<Hall, Long> {

    List<Hall> findByCinemaIdOrderByIdAsc(Long cinemaId);
}
