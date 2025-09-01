package innercircle.commerce.product.core.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageDeletionTarget {
    
    private Long id;
    private String imageUrl;
    private LocalDateTime scheduledDeletionAt;
    private int retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private ImageDeletionTarget(
            Long id, 
            String imageUrl, 
            LocalDateTime scheduledDeletionAt, 
            int retryCount, 
            LocalDateTime createdAt, 
            LocalDateTime updatedAt
    ) {
        this.id = id == null ? IdGenerator.generateId() : id;
        setImageUrl(imageUrl);
        this.scheduledDeletionAt = scheduledDeletionAt;
        this.retryCount = retryCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ImageDeletionTarget create(String imageUrl, int delayDays) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("이미지 URL은 필수입니다.");
        }
        if (delayDays < 0) {
            throw new IllegalArgumentException("지연 일수는 0 이상이어야 합니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        return ImageDeletionTarget.builder()
                .id(null)
                .imageUrl(imageUrl.trim())
                .scheduledDeletionAt(now.plusDays(delayDays))
                .retryCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static ImageDeletionTarget restore(
            Long id, 
            String imageUrl, 
            LocalDateTime scheduledDeletionAt, 
            int retryCount, 
            LocalDateTime createdAt, 
            LocalDateTime updatedAt
    ) {
        return ImageDeletionTarget.builder()
                .id(id)
                .imageUrl(imageUrl)
                .scheduledDeletionAt(scheduledDeletionAt)
                .retryCount(retryCount)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public void incrementRetryCount() {
        this.retryCount++;
        this.updatedAt = LocalDateTime.now();
    }

    public void rescheduleAfterFailure(int delayHours) {
        this.scheduledDeletionAt = LocalDateTime.now().plusHours(delayHours);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isScheduledForDeletion() {
        return LocalDateTime.now().isAfter(this.scheduledDeletionAt);
    }

    private void setImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("이미지 URL은 필수입니다.");
        }
        this.imageUrl = imageUrl.trim();
    }
}