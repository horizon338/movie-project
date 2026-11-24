package com.example.movie.service;

import com.example.movie.common.BusinessException;
import com.example.movie.dto.admin.AdminShowtimeVO;
import com.example.movie.dto.admin.ShowtimeCreateRequest;
import com.example.movie.entity.Hall;
import com.example.movie.entity.Movie;
import com.example.movie.entity.Seat;
import com.example.movie.entity.SeatType;
import com.example.movie.entity.Showtime;
import com.example.movie.repository.HallRepository;
import com.example.movie.repository.MovieRepository;
import com.example.movie.repository.SeatRepository;
import com.example.movie.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final HallRepository hallRepository;
    private final SeatRepository seatRepository;

    @Transactional(readOnly = true)
    public List<AdminShowtimeVO> list(Long movieId, Long cinemaId, LocalDate date) {
        LocalDateTime from = date != null
                ? date.atStartOfDay()
                : LocalDateTime.now().withSecond(0).withNano(0);
        LocalDateTime to = date != null ? from.plusDays(1) : from.plusDays(7);

        List<Showtime> showtimes = showtimeRepository.findAdmin(movieId, cinemaId, from, to);
        if (showtimes.isEmpty()) {
            return List.of();
        }
        Map<Long, Integer> sold = new HashMap<>();
        List<Long> ids = showtimes.stream().map(Showtime::getId).toList();
        for (Object[] row : seatRepository.countUnavailableByShowtimeIds(ids)) {
            sold.put((Long) row[0], ((Number) row[1]).intValue());
        }
        return showtimes.stream()
                .map(st -> AdminShowtimeVO.from(st, sold.getOrDefault(st.getId(), 0)))
                .toList();
    }

    @Transactional
    public AdminShowtimeVO create(ShowtimeCreateRequest request) {
        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() -> new BusinessException(404, "影片不存在"));
        Hall hall = hallRepository.findById(request.hallId())
                .orElseThrow(() -> new BusinessException(404, "影厅不存在"));

        LocalDateTime start = request.startTime();
        if (!start.isAfter(LocalDateTime.now())) {
            throw new BusinessException("开场时间必须晚于当前时间");
        }
        LocalDateTime end = start.plusMinutes(movie.getDurationMinutes());

        boolean conflict = !showtimeRepository
                .findByHallIdAndStartTimeLessThanAndEndTimeGreaterThan(hall.getId(), end, start)
                .isEmpty();
        if (conflict) {
            throw new BusinessException("该影厅在此时间段已有排片，请调整时间");
        }

        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setHall(hall);
        showtime.setStartTime(start);
        showtime.setEndTime(end);
        showtime.setPrice(request.price());
        showtime.setTotalSeats(hall.getSeatCount());
        showtime.setStatus(1);
        showtimeRepository.save(showtime);
        generateSeats(showtime, hall);

        return AdminShowtimeVO.from(showtime, 0);
    }

    private void generateSeats(Showtime showtime, Hall hall) {
        List<Seat> seats = new ArrayList<>(hall.getRowCount() * hall.getColCount());
        for (int row = 1; row <= hall.getRowCount(); row++) {
            for (int col = 1; col <= hall.getColCount(); col++) {
                Seat seat = new Seat();
                seat.setShowtime(showtime);
                seat.setRowNo(row);
                seat.setColNo(col);
                boolean loveRow = hall.getName().contains("情侣") && row == hall.getRowCount();
                seat.setSeatType(loveRow ? SeatType.LOVE : SeatType.NORMAL);
                seat.setStatus(Seat.STATUS_AVAILABLE);
                seats.add(seat);
            }
        }
        seatRepository.saveAll(seats);
    }
}
