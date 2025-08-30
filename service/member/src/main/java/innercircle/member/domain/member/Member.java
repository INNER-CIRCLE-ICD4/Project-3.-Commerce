package innercircle.member.domain.member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static innercircle.member.domain.member.ValidationMember.*;

@Entity
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(name = "UK_MEMBER_EMAIL_ADDRESS", columnNames = "email")
})
@NaturalIdCache
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @NaturalId
    @Embedded
    private Email email;

    private String name;

    private String password;

    private LocalDate birthDate;

    @Enumerated(value = EnumType.STRING)
    private Gender gender;

    @Enumerated(value = EnumType.STRING)
    private MemberStatus status;

    private LocalDateTime createAt;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<MemberRole> roles = new ArrayList<>();

    public static Member create(String email, String name, String password, String birthDate, String gender) {

        Member member = new Member();
        member.email = new Email(email);
        member.setName(name);
        member.setPassword(password);
        member.setBirthDate(birthDate);
        member.setGender(gender);
        member.status = MemberStatus.ACTIVE;
        member.createAt = LocalDateTime.now();
        member.assignBuyerRole();

        return member;
    }

    private void setName(String name) {
        validationNameCheck(name);
        this.name = name;
    }

    private void setPassword(String password) {
        validatePassword(password);
        this.password = password;
    }

    private void setBirthDate(String birthDate) {
        validBirthDate(birthDate);
        this.birthDate = LocalDate.parse(birthDate);
    }

    private void setGender(String gender) {
        validGender(gender);
        this.gender = Gender.valueOf(gender);
    }

    private void assignBuyerRole() {
        if (hasRole(RoleType.BUYER)) {
            throw new IllegalStateException("이미 구매자 역할이 할당되어 있습니다.");
        }
        MemberRole.signUp(this);
    }

    private boolean hasRole(RoleType roleType) {
        return roles.stream().anyMatch(role -> role.getRoleType() == roleType);
    }


    public void inActivate() {
        if (this.status == MemberStatus.INACTIVE) {
            throw new IllegalStateException("이미 비활성화 상태입니다.");
        }
        this.status = MemberStatus.INACTIVE;
    }

    public List<String> getRoleNames() {
        return roles.stream()
                .map(role -> role.getRoleType().name())
                .toList();
    }

    public Member withEncodedPassword(String encodedPassword) {
        return Member.create(
                this.email.email(),
                this.name,
                encodedPassword,
                this.birthDate != null ? this.birthDate.toString() : null,
                this.gender.name()
        );
    }

    // todo 추후 동시성 고려
    public void changeStatus(MemberStatus status) {
        this.status = status;
    }


    public boolean isActive() {
        return this.status == MemberStatus.ACTIVE;
    }


    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof Member member)) return false;

        return Objects.equals(getId(), member.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
