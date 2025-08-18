package innercircle.commerce.product.infra.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

/**
 * Brand JPA 엔티티
 */
@Entity
@Table(name = "brands")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandJpaEntity {

	@Id
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String name;

	@Column(length = 500)
	private String description;

	@Column(name = "logo_url", length = 255)
	private String logoUrl;

	@Column(name = "website_url", length = 255)
	private String websiteUrl;

	@Column(nullable = false)
	private Boolean active;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}