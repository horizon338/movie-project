package com.example.movie.repository;

import com.example.movie.entity.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CinemaRepository extends JpaRepository<Cinema, Long> {

    List<Cinema> findByStatusOrderByIdAsc(Integer status);

    List<Cinema> findAllByOrderByIdAsc();
}
