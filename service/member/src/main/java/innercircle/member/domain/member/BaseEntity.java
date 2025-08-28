package innercircle.member.domain.member;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    private Long id;

    @PrePersist
    private void ensureId() {
        if (this.id == null) {
            this.id = SnowFlakeGenerator.GENERATOR.nextId();
        }
    }

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();



}

