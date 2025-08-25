package innercircle.commerce.like.controller;

import innercircle.commerce.like.service.ReviewLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews/{reviewId}/like")
@RequiredArgsConstructor
public class ReviewLikeController {
    private final ReviewLikeService reviewLikeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void likeReview(@PathVariable long reviewId) {
        long memberId = 1L;
        reviewLikeService.addLike(reviewId, memberId);
    }

    // '좋아요' 삭제
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT) // 성공 시 204 No Content 응답
    public void removeLike(@PathVariable Long reviewId) {
        Long userId = 1L;
        reviewLikeService.removeLike(reviewId, userId);
    }
}
