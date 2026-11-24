package com.example.movie.service;

import com.example.movie.common.BusinessException;
import com.example.movie.common.PageResult;
import com.example.movie.dto.admin.AdminMovieVO;
import com.example.movie.dto.admin.MovieSaveRequest;
import com.example.movie.entity.Movie;
import com.example.movie.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminMovieService {

    private final MovieRepository movieRepository;

    @Transactional(readOnly = true)
    public PageResult<AdminMovieVO> list(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0),
                Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "createdAt"));
        String kw = keyword == null || keyword.isBlank() ? null : keyword.trim();
        Page<Movie> result = kw == null
                ? movieRepository.findAll(pageable)
                : movieRepository.findByTitleContainingIgnoreCase(kw, pageable);
        return PageResult.of(result, AdminMovieVO::from);
    }

    @Transactional
    public AdminMovieVO create(MovieSaveRequest request) {
        return AdminMovieVO.from(movieRepository.save(apply(new Movie(), request)));
    }

    @Transactional
    public AdminMovieVO update(Long id, MovieSaveRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "影片不存在"));
        return AdminMovieVO.from(movieRepository.save(apply(movie, request)));
    }

    private Movie apply(Movie movie, MovieSaveRequest request) {
        if (request.offShelfDate() != null && request.offShelfDate().isBefore(request.releaseDate())) {
            throw new BusinessException("下架时间不能早于上映时间");
        }
        movie.setTitle(request.title().trim());
        movie.setPosterUrl(request.posterUrl());
        movie.setDurationMinutes(request.durationMinutes());
        movie.setGenre(request.genre());
        movie.setDirector(request.director());
        movie.setActors(request.actors());
        movie.setSynopsis(request.synopsis());
        movie.setReleaseDate(request.releaseDate());
        movie.setOffShelfDate(request.offShelfDate());
        movie.setStatus(request.status());
        return movie;
    }
}
