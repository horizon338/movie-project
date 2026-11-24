package com.example.movie.config;

import com.example.movie.entity.Cinema;
import com.example.movie.entity.Hall;
import com.example.movie.entity.Movie;
import com.example.movie.entity.Seat;
import com.example.movie.entity.SeatType;
import com.example.movie.entity.Showtime;
import com.example.movie.repository.CinemaRepository;
import com.example.movie.repository.HallRepository;
import com.example.movie.repository.MovieRepository;
import com.example.movie.repository.SeatRepository;
import com.example.movie.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 首次启动时初始化影片、影院、影厅、排片与座位数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MovieSeedInitializer implements ApplicationRunner {

    private static final String IMAGE_API = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=";

    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;
    private final HallRepository hallRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (movieRepository.count() > 0) {
            return;
        }

        List<Cinema> cinemas = cinemaRepository.saveAll(buildCinemas());
        List<Hall> halls = new ArrayList<>();
        for (Cinema cinema : cinemas) {
            halls.addAll(hallRepository.saveAll(buildHalls(cinema)));
        }
        List<Movie> movies = movieRepository.saveAll(buildMovies());

        int showtimeCount = seedShowtimes(movies, halls);
        log.info("已初始化影讯数据：{} 家影院、{} 个影厅、{} 部影片、{} 个场次",
                cinemas.size(), halls.size(), movies.size(), showtimeCount);
    }

    private List<Cinema> buildCinemas() {
        return List.of(
                cinema("星光国际影城（万象城店）", "江城区万象城 4 层", "0755-88880001"),
                cinema("百老汇影城（中心店）", "中心路 88 号购物中心 5 层", "0755-88880002"),
                cinema("时代影城（滨江店）", "滨江大道 199 号", "0755-88880003")
        );
    }

    private Cinema cinema(String name, String address, String phone) {
        Cinema cinema = new Cinema();
        cinema.setName(name);
        cinema.setAddress(address);
        cinema.setPhone(phone);
        cinema.setStatus(1);
        return cinema;
    }

    private List<Hall> buildHalls(Cinema cinema) {
        return List.of(
                hall(cinema, "1号激光厅", 8, 12),
                hall(cinema, "2号厅（情侣座）", 6, 10)
        );
    }

    private Hall hall(Cinema cinema, String name, int rows, int cols) {
        Hall hall = new Hall();
        hall.setCinema(cinema);
        hall.setName(name);
        hall.setRowCount(rows);
        hall.setColCount(cols);
        hall.setSeatCount(rows * cols);
        return hall;
    }

    private List<Movie> buildMovies() {
        LocalDate today = LocalDate.now();
        return List.of(
                movie("星际穿越者", "科幻 / 冒险", 169, "陈默", "李澈 / 周眠 / 王岚", 8.9, today.minusDays(12),
                        "一支深空探险队在虫洞另一侧发现了失联十年的母舰，为了回家，他们必须在时间与引力的缝隙里做出选择。",
                        "cinematic sci-fi movie poster, lone astronaut facing a colossal glowing planet, deep blue nebula, dramatic rim light, high detail"),
                movie("长安夜行", "古装 / 悬疑", 132, "顾行舟", "沈砚 / 苏绫 / 裴照", 8.4, today.minusDays(20),
                        "上元灯节前夜，长安城接连出现离奇命案，一名落魄仵作与大理寺少卿循着灯火追查真相。",
                        "cinematic ancient chinese mystery movie poster, lantern lit tang dynasty street at night, silhouette with sword, gold and crimson tones"),
                movie("深海回声", "悬疑 / 惊悚", 118, "林之遥", "许清 / 罗屿", 8.1, today.minusDays(6),
                        "深海科考站收到一段来自海底的求救信号，而当潜水员下潜后，信号却开始模仿他们自己的声音。",
                        "cinematic deep sea thriller poster, diver suspended in dark ocean, shaft of light from surface, teal tones, suspenseful"),
                movie("夏日列车", "爱情 / 青春", 106, "周南", "夏禾 / 江野", 8.6, today.minusDays(3),
                        "高考结束的夏天，两个陌生人在一列慢车上相遇，用七天时间走完了彼此的青春。",
                        "cinematic summer romance movie poster, young couple on a train through sunflower fields, warm sunset light, film grain"),
                movie("铁拳风暴", "动作 / 运动", 124, "韩铁", "赵锋 / 秦朗", 8.0, today.minusDays(30),
                        "退役拳手为守护街区拳馆重返擂台，面对的是曾经并肩作战的兄弟。",
                        "cinematic action movie poster, boxer shadow boxing in a dark ring, sweat and dust, orange spotlight, high contrast"),
                movie("熊猫小队", "动画 / 家庭", 96, "米粒", "配音：何乐 / 唐糖", 8.8, today.minusDays(9),
                        "三只性格迥异的熊猫组成救援小队，踏上寻找竹林之心的冒险旅程。",
                        "cinematic animated family movie poster, cute panda heroes in a bamboo forest, colorful playful illustration")
        );
    }

    private Movie movie(String title, String genre, int duration, String director, String actors,
                        double score, LocalDate releaseDate, String synopsis, String posterPrompt) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setGenre(genre);
        movie.setDurationMinutes(duration);
        movie.setDirector(director);
        movie.setActors(actors);
        movie.setScoreAvg(BigDecimal.valueOf(score));
        movie.setReleaseDate(releaseDate);
        movie.setOffShelfDate(releaseDate.plusDays(90));
        movie.setSynopsis(synopsis);
        movie.setPosterUrl(IMAGE_API
                + URLEncoder.encode(posterPrompt, StandardCharsets.UTF_8).replace("+", "%20")
                + "&image_size=portrait_4_3");
        movie.setStatus(1);
        return movie;
    }

    private int seedShowtimes(List<Movie> movies, List<Hall> halls) {
        LocalTime[] slots = {
                LocalTime.of(10, 30), LocalTime.of(13, 40), LocalTime.of(16, 20),
                LocalTime.of(19, 30), LocalTime.of(21, 50)
        };
        int count = 0;
        LocalDateTime now = LocalDateTime.now();

        for (int day = 0; day < 3; day++) {
            LocalDate date = LocalDate.now().plusDays(day);
            for (int i = 0; i < movies.size(); i++) {
                Movie movie = movies.get(i);
                Hall hall = halls.get((i + day) % halls.size());
                int[] slotIndexes = {(i + day) % slots.length, (i + day + 2) % slots.length};

                for (int slotIndex : slotIndexes) {
                    LocalDateTime start = LocalDateTime.of(date, slots[slotIndex]);
                    if (!start.isAfter(now)) {
                        continue;
                    }
                    Showtime showtime = new Showtime();
                    showtime.setMovie(movie);
                    showtime.setHall(hall);
                    showtime.setStartTime(start);
                    showtime.setEndTime(start.plusMinutes(movie.getDurationMinutes()));
                    showtime.setPrice(hall.getName().startsWith("1号") ? new BigDecimal("45.00")
                            : new BigDecimal("52.00"));
                    showtime.setTotalSeats(hall.getSeatCount());
                    showtime.setStatus(1);
                    showtimeRepository.save(showtime);
                    generateSeats(showtime, hall);
                    count++;
                }
            }
        }
        return count;
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
