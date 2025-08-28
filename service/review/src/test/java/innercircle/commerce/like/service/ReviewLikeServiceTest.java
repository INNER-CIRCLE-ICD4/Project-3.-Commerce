package innercircle.commerce.like.service;

import innercircle.commerce.like.entity.ReviewLike;
import innercircle.commerce.like.repository.ReviewLikeRepository;
import innercircle.commerce.review.entity.Review;
import innercircle.commerce.review.repository.ReviewRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ReviewLikeServiceTest {
    @Autowired
    private ReviewLikeService reviewLikeService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewLikeRepository reviewLikeRepository;

    private Review savedReview;
    private final long memberId = 1L;

    @BeforeEach
    void setUp() {
        // 모든 테스트 전에 '좋아요'를 누를 대상 리뷰를 미리 생성
        Review review = Review.builder()
                .memberId(99L)
                .productId(100L)
                .rating(5)
                .content("테스트용 리뷰입니다.")
                .build();
        savedReview = reviewRepository.save(review);
    }

    @Test
    @DisplayName("좋아요 추가에 성공하고 DB에 기록이 남는다")
    void addLike_success() {
        // when
        reviewLikeService.addLike(savedReview.getId(), memberId);

        // then
        // DB에서 직접 조회하여 '좋아요' 기록이 실제로 저장되었는지 확인
        ReviewLike foundLike = reviewLikeRepository.findByReviewAndMemberId(savedReview, memberId)
                .orElseThrow(); // 기록이 없으면 테스트 실패

//        assertThat(foundLike.getReview().getId()).isEqualTo(savedReview.getId());
//        assertThat(foundLike.getMemberId()).isEqualTo(memberId);
    }
}