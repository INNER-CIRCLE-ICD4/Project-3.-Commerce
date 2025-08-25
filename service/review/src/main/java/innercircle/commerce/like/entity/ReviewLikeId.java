package innercircle.commerce.like.entity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@EqualsAndHashCode
public class ReviewLikeId implements Serializable {
    private Long review;
    private Long memberId;
}
