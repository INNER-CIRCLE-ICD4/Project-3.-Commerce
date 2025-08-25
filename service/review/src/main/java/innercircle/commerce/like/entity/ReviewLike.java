package innercircle.commerce.like.entity;

import innercircle.commerce.review.entity.Review;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@IdClass(ReviewLikeId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewLike {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="review_id")
    private Review review;

    @Id
    @Column(name="member_id")
    private Long memberId;

    public ReviewLike(Review review, Long memberId) {
        this.review = review;
        this.memberId = memberId;
    }
}
