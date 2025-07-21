package innercircle.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

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


    public static Member create(String email, String name, String password, String birthDate, String gender) {

        Member member = new Member();
        member.email = new Email(email);
        member.setName(name);
        member.setPassword(password);
        member.setBirthDate(birthDate);
        member.setGender(gender);
        member.status = MemberStatus.ACTIVE;
        member.createAt = LocalDateTime.now();

        return member;
    }

    private void setName(String name) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수로 입력해야합니다.");
        }

        if (name.length() > 10) {
            throw new IllegalArgumentException("이름은 10글자를 넘을 수 없습니다. name : " + name);
        }
        this.name = name;
    }

    private void setPassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8글자 이상 입력해주세요");
        }

        if (password.length() > 20) {
            throw new IllegalArgumentException("비밀번호는 20글자 이하로 입력해주세요");
        }
        //todo 암호화 필요
        this.password = password;
    }

    private void setBirthDate(String birthDate) {

        try {
            this.birthDate = LocalDate.parse(birthDate);
        }catch (Exception e) {
            throw new IllegalArgumentException("생년월일 형식이 올바르지 않습니다. birth_date : " + birthDate);
        }
    }

    private void setGender(String gender) {
        try {
            this.gender = Gender.valueOf(gender);
        } catch (Exception e) {
            throw new IllegalArgumentException("성별 형식 올바르지 않습니다. gender : " + gender);
        }
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
