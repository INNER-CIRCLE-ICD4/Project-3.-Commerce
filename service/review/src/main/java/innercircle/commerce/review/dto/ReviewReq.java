package innercircle.commerce.review.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewReq {
    private Long productId;
    private int rating;
    private String content;

    public ReviewReq(Long productId, int rating, String content) {
        this.productId = productId;
        this.rating = rating;
        this.content = content;
    }
}
