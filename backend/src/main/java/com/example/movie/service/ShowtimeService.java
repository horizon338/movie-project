package com.example.movie.service;

import com.example.movie.common.BusinessException;
import com.example.movie.dto.SeatMapVO;
import com.example.movie.entity.Seat;
import com.example.movie.entity.Showtime;
import com.example.movie.repository.SeatRepository;
import com.example.movie.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;

    @Transactional(readOnly = true)
    public SeatMapVO seatMap(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new BusinessException(404, "场次不存在"));

        var seats = seatRepository.findByShowtimeIdOrderByRowNoAscColNoAsc(showtimeId).stream()
                .map(seat -> new SeatMapVO.SeatItemVO(
                        seat.getId(),
                        seat.getRowNo(),
                        seat.getColNo(),
                        seat.getSeatType().name(),
                        seat.getStatus()))
                .toList();

        var hall = showtime.getHall();
        return new SeatMapVO(
                showtime.getId(),
                showtime.getMovie().getTitle(),
                showtime.getMovie().getPosterUrl(),
                hall.getCinema().getName(),
                hall.getName(),
                showtime.getStartTime(),
                showtime.getEndTime(),
                showtime.getPrice(),
                hall.getRowCount(),
                hall.getColCount(),
                seats
        );
    }
}
