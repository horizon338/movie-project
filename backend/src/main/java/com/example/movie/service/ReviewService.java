package com.example.movie.service;

import com.example.movie.common.BusinessException;
import com.example.movie.dto.MyReviewVO;
import com.example.movie.dto.ReviewVO;
import com.example.movie.dto.SubmitReviewRequest;
import com.example.movie.entity.Movie;
import com.example.movie.entity.OrderStatus;
import com.example.movie.entity.Review;
import com.example.movie.repository.MovieRepository;
import com.example.movie.repository.OrderRepository;
import com.example.movie.repository.ReviewRepository;
import com.example.movie.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ReviewVO> listByMovie(Long movieId, Long currentUserId) {
        if (!movieRepository.existsById(movieId)) {
            throw new BusinessException(404, "影片不存在");
        }
        return reviewRepository.findByMovieIdOrderByCreatedAtDesc(movieId).stream()
                .map(review -> ReviewVO.from(review, currentUserId))
                .toList();
    }

    @Transactional(readOnly = true)
    public ReviewVO myReview(Long userId, Long movieId) {
        return reviewRepository.findByUserIdAndMovieId(userId, movieId)
                .map(review -> ReviewVO.from(review, userId))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<MyReviewVO> myReviews(Long userId) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(MyReviewVO::from)
                .toList();
    }

    @Transactional
    public ReviewVO submit(Long userId, SubmitReviewRequest request) {
        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() -> new BusinessException(404, "影片不存在"));

        long paidOrders = orderRepository.countByUserAndMovieAndStatus(
                userId, request.movieId(), OrderStatus.PAID);
        if (paidOrders == 0) {
            throw new BusinessException("购票观影后才可评价该影片");
        }

        Review review = reviewRepository.findByUserIdAndMovieId(userId, request.movieId())
                .orElseGet(() -> {
                    Review created = new Review();
                    created.setUser(userRepository.getReferenceById(userId));
                    created.setMovie(movie);
                    return created;
                });
        review.setRating(request.rating());
        review.setContent(request.content().trim());
        reviewRepository.save(review);

        Double avg = reviewRepository.avgRating(request.movieId());
        if (avg != null) {
            movie.setScoreAvg(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));
        }

        return ReviewVO.from(review, userId);
    }
}
