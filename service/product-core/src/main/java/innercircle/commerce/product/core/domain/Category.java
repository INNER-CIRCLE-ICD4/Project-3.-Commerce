package innercircle.commerce.product.core.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private Integer level;
    private Integer sortOrder;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Category(
            Long id, String name, String description, Long parentId,
            Integer level, Integer sortOrder, Boolean active, 
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        this.id = id == null ? IdGenerator.generateId() : id;
        setName(name);
        this.description = description;
        this.parentId = parentId;
        this.level = level != null ? level : 1;
        this.sortOrder = sortOrder;
        this.active = active != null ? active : true;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 카테고리를 생성합니다.
     */
    public static Category create(String name) {
        LocalDateTime now = LocalDateTime.now();
        
        return Category.builder()
                      .id(null)
                      .name(name)
                      .level(1)
                      .active(true)
                      .createdAt(now)
                      .updatedAt(now)
                      .build();
    }

    /**
     * 카테고리를 복원합니다. (DB에서 조회 시 사용)
     */
    public static Category restore(
            Long id, String name, String description, Long parentId,
            Integer level, Integer sortOrder, Boolean active,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        return Category.builder()
                      .id(id)
                      .name(name)
                      .description(description)
                      .parentId(parentId)
                      .level(level)
                      .sortOrder(sortOrder)
                      .active(active)
                      .createdAt(createdAt)
                      .updatedAt(updatedAt)
                      .build();
    }

    private void setName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("카테고리명은 필수입니다.");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("카테고리명은 100자를 초과할 수 없습니다.");
        }
        this.name = name;
    }
}