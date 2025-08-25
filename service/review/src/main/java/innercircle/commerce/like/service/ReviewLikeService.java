package innercircle.commerce.like.service;

import innercircle.commerce.like.entity.ReviewLike;
import innercircle.commerce.like.repository.ReviewLikeRepository;
import innercircle.commerce.review.entity.Review;
import innercircle.commerce.review.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewLikeService {
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String LIKE_COUNT_KEY_PREFIX = "review:like:count:";

    @Transactional
    public void addLike(long reviewId, long memberId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("해당 리뷰를 찾을 수 없습니다."));

        if(reviewLikeRepository.findByReviewAndMemberId(review, memberId).isPresent()) {
            return;
        }

        ReviewLike reviewLike = new ReviewLike(review, memberId);
        reviewLikeRepository.save(reviewLike);

        redisTemplate.opsForValue().increment(LIKE_COUNT_KEY_PREFIX + reviewId);
    }

    @Transactional
    public void removeLike(Long reviewId, Long memberId) {
        long deletedCount  = reviewLikeRepository.deleteByReviewIdAndMemberId(reviewId, memberId);

        if (deletedCount > 0) {
            redisTemplate.opsForValue().decrement(LIKE_COUNT_KEY_PREFIX + reviewId);
        }
    }
}
