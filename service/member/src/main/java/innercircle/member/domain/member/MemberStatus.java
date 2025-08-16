package innercircle.member.domain.member;


import lombok.Getter;

@Getter
public enum MemberStatus {

    ACTIVE,
    INACTIVE,
    SUSPENDED,
    WITHDRAWN;

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isInactive() {
        return this == INACTIVE;
    }

    public boolean isSuspended() {
        return this == SUSPENDED;
    }

    public boolean isWithdrawn() {
        return this == WITHDRAWN;
    }
}
