package innercircle.commerce.review.entity;

import innercircle.commerce.like.entity.ReviewLike;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long memberId;
    private Long productId;

    private Long rating = 0L;
    @Column(columnDefinition = "TEXT")
    private String content;
    private Long likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewLike> likes = new ArrayList<>();

    @Builder
    public Review(Long memberId, Long productId, long rating, String content) {
        this.memberId = memberId;
        this.productId = productId;
        this.rating = rating;
        this.content = content;
    }
}
