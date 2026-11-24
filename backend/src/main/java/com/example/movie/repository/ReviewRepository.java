package com.example.movie.repository;

import com.example.movie.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByMovieIdOrderByCreatedAtDesc(Long movieId);

    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Review> findByUserIdAndMovieId(Long userId, Long movieId);

    @Query("select avg(r.rating) from Review r where r.movie.id = :movieId")
    Double avgRating(@Param("movieId") Long movieId);
}
