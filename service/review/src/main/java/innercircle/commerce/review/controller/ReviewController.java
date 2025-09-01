package innercircle.commerce.review.controller;

import innercircle.commerce.review.dto.ReviewReq;
import innercircle.commerce.review.dto.ReviewResp;
import innercircle.commerce.review.service.ReviewService;
import innercircle.common.AuthenticatedUser;
import innercircle.common.CurrentUser;
import innercircle.common.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/my")
    public ResponseEntity<Page<ReviewResp>> getMyReviews(
            @CurrentUser AuthenticatedUser currentUser,
            Pageable pageable) {

        if(!currentUser.isUser()) {
            throw new ForbiddenException("접근 권한이 없습니다");
        }

        // 2. 서비스 로직 호출
        Page<ReviewResp> myReviews = reviewService.findReviewsByMemberId(currentUser.userId(), pageable);

        return ResponseEntity.ok(myReviews);
    }

    // 리뷰 작성 API
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResp createReview(
            @CurrentUser AuthenticatedUser currentUser,
            @RequestBody ReviewReq requestDto) {

        if(!currentUser.isUser()) {
            throw new ForbiddenException("접근 권한이 없습니다");
        }

        return reviewService.createReview(currentUser.userId(), requestDto);
    }
}
