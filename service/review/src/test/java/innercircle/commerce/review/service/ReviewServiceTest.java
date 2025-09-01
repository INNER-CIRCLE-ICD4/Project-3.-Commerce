package innercircle.commerce.review.service;

import innercircle.commerce.global.client.MemberServiceClient;
import innercircle.commerce.global.client.ProductServiceClient;
import innercircle.commerce.review.dto.ReviewReq;
import innercircle.commerce.review.dto.ReviewResp;
import innercircle.commerce.review.entity.Review;
import innercircle.commerce.review.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {
    private final String REVIEW_CONTENT = "상품 리뷰 내용입니다.";
    private final int RATING_SCORE_FIVE = 5;
    private final int RATING_SCORE_FOUR = 4;

    private final long MEMBER_ID = 1L;
    private final long INVALID_MEMBER_ID = 999L;

    private final long PRODUCT_ID = 100L;

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private MemberServiceClient memberServiceClient;

    @Mock
    private ProductServiceClient productServiceClient;

    @Nested
    @DisplayName("리뷰 생성 테스트")
    class CreateReviewTests {
        @Test
        @DisplayName("성공")
        void createReview_success() {
            ReviewReq requestDto = ReviewReq.builder()
                    .productId(PRODUCT_ID)
                    .rating(RATING_SCORE_FOUR)
                    .content(REVIEW_CONTENT)
                    .build();

            // Feign Client가 유효한 사용자/상품이라고 응답하도록 설정
            when(memberServiceClient.checkMemberExists(MEMBER_ID)).thenReturn(true);
            when(productServiceClient.checkProductExists(PRODUCT_ID)).thenReturn(true);
            // repository.save가 호출되면, 인자로 받은 review 객체를 그대로 반환하도록 설정
            when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            ReviewResp responseDto = reviewService.createReview(MEMBER_ID, requestDto);

            // then
            verify(memberServiceClient, times(1)).checkMemberExists(MEMBER_ID);
            verify(productServiceClient, times(1)).checkProductExists(PRODUCT_ID);
            verify(reviewRepository, times(1)).save(any(Review.class));

            assertThat(responseDto.getMemberId()).isEqualTo(MEMBER_ID);
            assertThat(responseDto.getProductId()).isEqualTo(PRODUCT_ID);
            assertThat(responseDto.getRating()).isEqualTo(RATING_SCORE_FOUR);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자")
        void createReview_fail_memberNotFound() {
            // given
            ReviewReq requestDto = ReviewReq.builder()
                    .productId(PRODUCT_ID)
                    .build();

            when(memberServiceClient.checkMemberExists(INVALID_MEMBER_ID)).thenReturn(false);

            // when & then
            assertThrows(EntityNotFoundException.class, () -> {
                reviewService.createReview(INVALID_MEMBER_ID, requestDto);
            });

            verify(reviewRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("리뷰 조회 테스트")
    class FindReviewsTests {

        @Test
        @DisplayName("특정 상품의 리뷰 목록을 페이징하여 조회 성공")
        void findReviewsByProductId_success() {
            // given\
            PageRequest pageable = PageRequest.of(0, 10);
            Review review1 = Review.builder().memberId(MEMBER_ID).productId(PRODUCT_ID).rating(RATING_SCORE_FIVE).content(REVIEW_CONTENT).build();
            Review review2 = Review.builder().memberId(2L).productId(PRODUCT_ID).rating(RATING_SCORE_FOUR).content(REVIEW_CONTENT + "_2").build();
            Page<Review> mockReviewPage = new PageImpl<>(List.of(review1, review2), pageable, 2);

            when(reviewRepository.findByProductId(PRODUCT_ID, pageable)).thenReturn(mockReviewPage);

            // when
            Page<ReviewResp> resultPage = reviewService.findReviewsByProductId(PRODUCT_ID, pageable);

            // then
            assertThat(resultPage.getTotalElements()).isEqualTo(2);
            assertThat(resultPage.getContent().get(0).getContent()).isEqualTo(REVIEW_CONTENT);
            assertThat(resultPage.getContent().get(1).getRating()).isEqualTo(RATING_SCORE_FOUR);
        }
    }

    @Nested
    @DisplayName("리뷰 삭제 테스트")
    class DeleteReviewTests {

        @Test
        @DisplayName("성공 - 자신의 리뷰를 삭제")
        void deleteReview_success() throws AccessDeniedException {
            // given
            long reviewId = 1L;
            Review mockReview = Review.builder().memberId(MEMBER_ID).build();

            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));

            // when
            reviewService.deleteReview(reviewId, MEMBER_ID);

            // then
            verify(reviewRepository, times(1)).delete(mockReview);
        }

        @Test
        @DisplayName("실패 - 다른 사람의 리뷰를 삭제 시도")
        void deleteReview_fail_accessDenied() {
            // given
            long reviewId = 1L;
            long requesterMemberId = 2L; // 삭제 요청자
            Review mockReview = Review.builder().memberId(MEMBER_ID).build();

            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));

            // when & then
            assertThrows(AccessDeniedException.class, () -> {
                reviewService.deleteReview(reviewId, requesterMemberId);
            });
            verify(reviewRepository, never()).delete(any());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 리뷰 삭제 시도")
        void deleteReview_fail_reviewNotFound() {
            // given
            long nonExistentReviewId = 999L;

            when(reviewRepository.findById(nonExistentReviewId)).thenReturn(Optional.empty());

            // when & then
            assertThrows(EntityNotFoundException.class, () -> {
                reviewService.deleteReview(nonExistentReviewId, MEMBER_ID);
            });
        }
    }
}
