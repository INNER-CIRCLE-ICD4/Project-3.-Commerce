package innercircle.commerce.like.controller;

import innercircle.commerce.like.service.ReviewLikeService;
import innercircle.common.AuthenticatedUser;
import innercircle.common.CurrentUser;
import innercircle.common.ForbiddenException;
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
    public void likeReview(@PathVariable long reviewId,
                           @CurrentUser AuthenticatedUser currentUser) {

        if(!currentUser.isUser()) {
            throw new ForbiddenException("접근 권한이 없습니다");
        }

        reviewLikeService.addLike(reviewId, currentUser.userId());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLike(@PathVariable Long reviewId,
                           @CurrentUser AuthenticatedUser currentUser) {

        reviewLikeService.removeLike(reviewId, currentUser.userId());
    }
}
