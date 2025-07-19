package innercircle.member.domain;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    private Long memberId;

    @PrePersist
    private void ensureId() {
        if (this.memberId == null) {
            this.memberId = SnowFlakeGenerator.GENERATOR.nextId();
        }
    }

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();
}

