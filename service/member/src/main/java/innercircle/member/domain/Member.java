package innercircle.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static innercircle.member.domain.ValidationMember.*;

@Entity
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(name = "UK_MEMBER_EMAIL_ADDRESS", columnNames = "email")
})
@NaturalIdCache
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @NaturalId
    @Column(name = "email", nullable = false)
    @Embedded
    private Email email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "gender", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Gender gender;

    @Column(name = "status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private MemberStatus status;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
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


    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof Member member)) return false;

        return Objects.equals(email, member.email);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(email);
    }

    @Override
    public String toString() {
        return "Member{" +
                "email=" + email +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", birthDate=" + birthDate +
                ", gender=" + gender +
                ", status=" + status +
                ", createAt=" + createAt +
                '}';
    }
}
