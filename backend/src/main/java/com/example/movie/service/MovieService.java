package com.example.movie.service;

import com.example.movie.common.BusinessException;
import com.example.movie.common.PageResult;
import com.example.movie.dto.MovieDetailVO;
import com.example.movie.dto.MovieListItemVO;
import com.example.movie.dto.ShowtimeVO;
import com.example.movie.entity.Movie;
import com.example.movie.entity.Seat;
import com.example.movie.entity.Showtime;
import com.example.movie.repository.MovieRepository;
import com.example.movie.repository.SeatRepository;
import com.example.movie.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;

    @Transactional(readOnly = true)
    public PageResult<MovieListItemVO> list(String keyword, LocalDate releaseFrom, LocalDate releaseTo,
                                            Long cinemaId, int page, int size) {
        int pageIndex = Math.max(page - 1, 0);
        int pageSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(pageIndex, pageSize,
                Sort.by(Sort.Order.desc("scoreAvg"), Sort.Order.desc("releaseDate")));

        String kw = keyword == null || keyword.isBlank() ? null : keyword.trim();
        Page<Movie> result = movieRepository.search(kw, releaseFrom, releaseTo, cinemaId,
                LocalDate.now(), LocalDateTime.now(), pageable);
        return PageResult.of(result, MovieListItemVO::from);
    }

    @Transactional(readOnly = true)
    public MovieDetailVO detail(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "影片不存在"));
        return MovieDetailVO.from(movie);
    }

    @Transactional(readOnly = true)
    public List<ShowtimeVO> showtimes(Long movieId, Long cinemaId, LocalDate date) {
        if (!movieRepository.existsById(movieId)) {
            throw new BusinessException(404, "影片不存在");
        }
        LocalDate day = date == null ? LocalDate.now() : date;
        LocalDateTime from = day.atStartOfDay();
        LocalDateTime to = from.plusDays(1);

        List<Showtime> showtimes = showtimeRepository.findForBooking(movieId, cinemaId, from, to);
        LocalDateTime now = LocalDateTime.now();
        List<Showtime> valid = showtimes.stream()
                .filter(st -> !st.getStartTime().isBefore(now))
                .toList();
        if (valid.isEmpty()) {
            return List.of();
        }

        List<Long> ids = valid.stream().map(Showtime::getId).toList();
        Map<Long, Integer> unavailable = new HashMap<>();
        for (Object[] row : seatRepository.countUnavailableByShowtimeIds(ids)) {
            unavailable.put((Long) row[0], ((Number) row[1]).intValue());
        }

        return valid.stream().map(st -> {
            int occupied = unavailable.getOrDefault(st.getId(), 0);
            return new ShowtimeVO(
                    st.getId(),
                    st.getStartTime(),
                    st.getEndTime(),
                    st.getPrice(),
                    st.getHall().getName(),
                    st.getHall().getCinema().getId(),
                    st.getHall().getCinema().getName(),
                    Math.max(st.getTotalSeats() - occupied, 0),
                    st.getTotalSeats()
            );
        }).collect(Collectors.toList());
    }
}
