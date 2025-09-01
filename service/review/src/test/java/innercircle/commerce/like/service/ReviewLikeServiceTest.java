package innercircle.commerce.like.service;

import innercircle.commerce.like.entity.ReviewLike;
import innercircle.commerce.like.repository.ReviewLikeRepository;
import innercircle.commerce.review.entity.Review;
import innercircle.commerce.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewLikeServiceTest {

    @InjectMocks
    private ReviewLikeService reviewLikeService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewLikeRepository reviewLikeRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock // ⭐️ opsForValue()가 반환할 가짜 객체도 필요합니다.
    private ValueOperations<String, String> valueOperations;

    private final long reviewId = 1L;
    private final long memberId = 1L;

    @Test
    @DisplayName("좋아요 추가에 성공하고 DB에 기록이 남는다")
    void addLike_success() {
        long reviewId = 1L;
        long memberId = 1L;
        Review mockReview = Review.builder().build();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));
        when(reviewLikeRepository.findByReviewAndMemberId(mockReview, memberId)).thenReturn(Optional.empty());

        // ⭐️ "redisTemplate.opsForValue()"가 호출되면, 우리가 만든 가짜 valueOperations 객체를 반환하라고 정의
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when (실행)
        reviewLikeService.addLike(reviewId, memberId);

        // then (검증)
        verify(reviewLikeRepository, times(1)).save(any(ReviewLike.class));

        // ⭐️ 최종적으로 valueOperations.increment()가 호출되었는지 검증
        verify(valueOperations, times(1)).increment(anyString());
    }

    @Test
    @DisplayName("좋아요 취소에 성공한다")
    void removeLike_success() {
        // given
        long reviewId = 1L;
        long memberId = 1L;
        Review mockReview = Review.builder().build();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));
        // ⭐️ delete 메서드가 1을 반환하도록 설정 (1개가 삭제되었음)
        when(reviewLikeRepository.deleteByReviewIdAndMemberId(reviewId, memberId)).thenReturn(1L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        reviewLikeService.removeLike(reviewId, memberId);

        // then
        // ⭐️ delete와 decrement가 각각 한 번씩 호출되었음을 검증
        verify(reviewLikeRepository, times(1)).deleteByReviewIdAndMemberId(reviewId, memberId);
        verify(valueOperations, times(1)).decrement(anyString());
    }
}