package innercircle.commerce.like.repository;

import innercircle.commerce.like.entity.ReviewLike;
import innercircle.commerce.like.entity.ReviewLikeId;
import innercircle.commerce.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, ReviewLikeId> {

    Optional<ReviewLike> findByReviewAndMemberId(Review review, Long memberId);
    long deleteByReviewIdAndMemberId(Long reviewId, Long memberId);
}
