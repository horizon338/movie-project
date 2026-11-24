package com.example.movie.controller;

import com.example.movie.common.BusinessException;
import com.example.movie.common.Result;
import com.example.movie.dto.MyReviewVO;
import com.example.movie.dto.ReviewVO;
import com.example.movie.dto.SubmitReviewRequest;
import com.example.movie.security.SecurityUtils;
import com.example.movie.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /** 影片评价列表（公开） */
    @GetMapping
    public Result<List<ReviewVO>> list(@RequestParam Long movieId) {
        return Result.ok(reviewService.listByMovie(movieId, SecurityUtils.currentUserId()));
    }

    /** 当前用户对某影片的评价（用于回填修改） */
    @GetMapping("/mine")
    public Result<ReviewVO> mine(@RequestParam Long movieId) {
        return Result.ok(reviewService.myReview(currentUserId(), movieId));
    }

    /** 我的全部评价 */
    @GetMapping("/my")
    public Result<List<MyReviewVO>> myReviews() {
        return Result.ok(reviewService.myReviews(currentUserId()));
    }

    @PostMapping
    public Result<ReviewVO> submit(@Valid @RequestBody SubmitReviewRequest request) {
        return Result.ok("评价成功", reviewService.submit(currentUserId(), request));
    }

    private Long currentUserId() {
        Long userId = SecurityUtils.currentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return userId;
    }
}
