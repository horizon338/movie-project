package com.example.movie.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 影片
 */
@Getter
@Setter
@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String posterUrl;

    /** 片长（分钟） */
    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(length = 100)
    private String genre;

    @Column(length = 100)
    private String director;

    @Column(length = 255)
    private String actors;

    @Column(length = 2000)
    private String synopsis;

    @Column(nullable = false)
    private LocalDate releaseDate;

    /** 下架时间，为空表示长期上映 */
    private LocalDate offShelfDate;

    /** 平均评分（0-10，保留一位小数） */
    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal scoreAvg = BigDecimal.ZERO;

    /** 1 上架，0 下架 */
    @Column(nullable = false)
    private Integer status = 1;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (scoreAvg == null) {
            scoreAvg = BigDecimal.ZERO;
        }
    }
}
