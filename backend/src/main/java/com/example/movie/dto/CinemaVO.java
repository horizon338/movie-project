package com.example.movie.dto;

import com.example.movie.entity.Cinema;

public record CinemaVO(Long id, String name, String address, String phone) {

    public static CinemaVO from(Cinema cinema) {
        return new CinemaVO(cinema.getId(), cinema.getName(), cinema.getAddress(), cinema.getPhone());
    }
}
