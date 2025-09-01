package innercircle.commerce.review.service;

import innercircle.commerce.global.client.MemberServiceClient;
import innercircle.commerce.global.client.ProductServiceClient;
import innercircle.commerce.review.dto.ReviewReq;
import innercircle.commerce.review.dto.ReviewResp;
import innercircle.commerce.review.entity.Review;
import innercircle.commerce.review.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final MemberServiceClient memberServiceClient;
    private final ProductServiceClient productServiceClient;

    /**
     * 리뷰 생성
     */
    @Transactional
    public ReviewResp createReview(Long memberId, ReviewReq requestDto) {
        if (!memberServiceClient.checkMemberExists(memberId)) {
            throw new EntityNotFoundException("존재하지 않는 사용자입니다. id: " + memberId);
        }
        if (!productServiceClient.checkProductExists(requestDto.getProductId())) {
            throw new EntityNotFoundException("존재하지 않는 상품입니다. id: " + requestDto.getProductId());
        }

        Review review = Review.builder()
                .memberId(memberId)
                .productId(requestDto.getProductId())
                .rating(requestDto.getRating())
                .content(requestDto.getContent())
                .build();

        Review savedReview = reviewRepository.save(review);
        return ReviewResp.of(savedReview);
    }

    /**
     * 특정 상품의 리뷰 목록 조회 (페이징)
     */
    public Page<ReviewResp> findReviewsByProductId(Long productId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByProductId(productId, pageable);
        // Page<Entity>를 Page<Dto>로 변환하는 표준적인 방법
        return reviews.map(ReviewResp::of);
    }

    /**
     * 리뷰 삭제
     */
    @Transactional
    public void deleteReview(Long reviewId, Long memberId) throws AccessDeniedException {
        // 1. 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다. id: " + reviewId));

        // 2. 권한 확인 (리뷰 작성자와 삭제 요청자가 동일한지)
        if (!review.getMemberId().equals(memberId)) {
            throw new AccessDeniedException("리뷰를 삭제할 권한이 없습니다.");
        }

        // 3. 삭제
        reviewRepository.delete(review);
    }

    public Page<ReviewResp> findReviewsByMemberId(Long memberId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByMemberId(memberId, pageable);

        return reviews.map(ReviewResp::of);
    }
}
