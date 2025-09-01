package innercircle.commerce.review.dto;

import innercircle.commerce.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReviewResp {
    private Long reviewId;
    private Long productId;
    private Long memberId;
    private int rating;
    private String content;
    private Long likeCount;

    public static ReviewResp of(Review review) {
        return ReviewResp.builder()
                    .reviewId(review.getId())
                    .productId(review.getProductId())
                    .memberId(review.getMemberId())
                    .rating(review.getRating())
                    .content(review.getContent())
                    .likeCount(review.getLikeCount())
                .build();
    }
}
