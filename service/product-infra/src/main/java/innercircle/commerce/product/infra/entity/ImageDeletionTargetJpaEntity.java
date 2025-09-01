package innercircle.commerce.product.infra.entity;

import innercircle.commerce.product.core.domain.ImageDeletionTarget;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "image_deletion_targets", indexes = {
    @Index(name = "idx_scheduled_deletion_at", columnList = "scheduledDeletionAt"),
    @Index(name = "idx_image_url", columnList = "imageUrl"),
    @Index(name = "idx_scheduled_deletion_at_retry_count", columnList = "scheduledDeletionAt, retryCount")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageDeletionTargetJpaEntity {

    @Id
    private Long id;

    @Column(nullable = false, length = 1000)
    private String imageUrl;

    @Column(nullable = false)
    private LocalDateTime scheduledDeletionAt;

    @Column(nullable = false)
    private Integer retryCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static ImageDeletionTargetJpaEntity from(ImageDeletionTarget target) {
        ImageDeletionTargetJpaEntity entity = new ImageDeletionTargetJpaEntity();
        entity.id = target.getId();
        entity.imageUrl = target.getImageUrl();
        entity.scheduledDeletionAt = target.getScheduledDeletionAt();
        entity.retryCount = target.getRetryCount();
        entity.createdAt = target.getCreatedAt();
        entity.updatedAt = target.getUpdatedAt();
        return entity;
    }

    public ImageDeletionTarget toDomain() {
        return ImageDeletionTarget.restore(
                this.id,
                this.imageUrl,
                this.scheduledDeletionAt,
                this.retryCount,
                this.createdAt,
                this.updatedAt
        );
    }
}