package com.example.movie.service;

import com.example.movie.common.BusinessException;
import com.example.movie.dto.CinemaVO;
import com.example.movie.dto.admin.CinemaSaveRequest;
import com.example.movie.dto.admin.HallSaveRequest;
import com.example.movie.entity.Cinema;
import com.example.movie.entity.Hall;
import com.example.movie.repository.CinemaRepository;
import com.example.movie.repository.HallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCinemaService {

    private final CinemaRepository cinemaRepository;
    private final HallRepository hallRepository;

    public record HallVO(Long id, Long cinemaId, String name, int rowCount, int colCount, int seatCount) {
        public static HallVO from(Hall hall) {
            return new HallVO(hall.getId(), hall.getCinema().getId(), hall.getName(),
                    hall.getRowCount(), hall.getColCount(), hall.getSeatCount());
        }
    }

    @Transactional(readOnly = true)
    public List<CinemaVO> listCinemas() {
        return cinemaRepository.findAllByOrderByIdAsc().stream().map(CinemaVO::from).toList();
    }

    @Transactional
    public CinemaVO createCinema(CinemaSaveRequest request) {
        Cinema cinema = new Cinema();
        apply(cinema, request);
        return CinemaVO.from(cinemaRepository.save(cinema));
    }

    @Transactional
    public CinemaVO updateCinema(Long id, CinemaSaveRequest request) {
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "影院不存在"));
        apply(cinema, request);
        return CinemaVO.from(cinemaRepository.save(cinema));
    }

    @Transactional(readOnly = true)
    public List<HallVO> listHalls(Long cinemaId) {
        if (!cinemaRepository.existsById(cinemaId)) {
            throw new BusinessException(404, "影院不存在");
        }
        return hallRepository.findByCinemaIdOrderByIdAsc(cinemaId).stream().map(HallVO::from).toList();
    }

    @Transactional
    public HallVO createHall(HallSaveRequest request) {
        Cinema cinema = cinemaRepository.findById(request.cinemaId())
                .orElseThrow(() -> new BusinessException(404, "影院不存在"));
        Hall hall = new Hall();
        hall.setCinema(cinema);
        hall.setName(request.name().trim());
        hall.setRowCount(request.rowCount());
        hall.setColCount(request.colCount());
        hall.setSeatCount(request.rowCount() * request.colCount());
        return HallVO.from(hallRepository.save(hall));
    }

    private void apply(Cinema cinema, CinemaSaveRequest request) {
        cinema.setName(request.name().trim());
        cinema.setAddress(request.address());
        cinema.setPhone(request.phone());
        cinema.setStatus(request.status());
    }
}
