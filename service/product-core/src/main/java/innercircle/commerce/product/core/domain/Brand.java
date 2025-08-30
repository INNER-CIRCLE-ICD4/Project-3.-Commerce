package innercircle.commerce.product.core.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Brand {
    private Long id;
    private String name;
    private String description;
    private String logoUrl;
    private String websiteUrl;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Brand(
            Long id, String name, String description, String logoUrl, 
            String websiteUrl, Boolean active, LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        this.id = id == null ? IdGenerator.generateId() : id;
        setName(name);
        this.description = description;
        this.logoUrl = logoUrl;
        this.websiteUrl = websiteUrl;
        this.active = active != null ? active : true;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 브랜드를 생성합니다.
     */
    public static Brand create(String name) {
        LocalDateTime now = LocalDateTime.now();
        
        return Brand.builder()
                   .id(null)
                   .name(name)
                   .active(true)
                   .createdAt(now)
                   .updatedAt(now)
                   .build();
    }

    /**
     * 브랜드를 복원합니다. (DB에서 조회 시 사용)
     */
    public static Brand restore(
            Long id, String name, String description, String logoUrl, 
            String websiteUrl, Boolean active, LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        return Brand.builder()
                   .id(id)
                   .name(name)
                   .description(description)
                   .logoUrl(logoUrl)
                   .websiteUrl(websiteUrl)
                   .active(active)
                   .createdAt(createdAt)
                   .updatedAt(updatedAt)
                   .build();
    }

    private void setName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("브랜드명은 필수입니다.");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("브랜드명은 100자를 초과할 수 없습니다.");
        }
        this.name = name;
    }
}