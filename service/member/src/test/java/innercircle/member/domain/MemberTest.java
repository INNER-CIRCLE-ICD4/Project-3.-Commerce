package innercircle.member.domain;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberTest {

    @Test
    @DisplayName("올바른 형식")
    public void validTest_success() {

        Member member = Member.create("commerce@gmail.com", "노성웅", "12345678", "1996-04-23", "MAIL");

        assertThat(member.getEmail().email()).isEqualTo("commerce@gmail.com");
        assertThat(member.getName()).isEqualTo("노성웅");
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않음.")
    void validTest1() {

        assertThatThrownBy(() -> Member.create("test", "노성웅", "12345678", "1996-04-23", "MAIL"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이메일 형식이 올바르지 않습니다. email");
    }

    @Test
    @DisplayName("이름이 너무 길다")
    void validTest2() {


        assertThatThrownBy(() -> Member.create("commerce@gmail.com", "", "12345678", "1996-04-23", "MAIL"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이름은 필수로 입력해야합니다.");

        assertThatThrownBy(() -> Member.create("commerce@gmail.com", "노성웅".repeat(10), "12345678", "1996-04-23", "MAIL"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이름은 10글자를 넘을 수 없습니다. name");
    }

    @Test
    @DisplayName("비밀번호가 8글자 이하이다.")
    public void validTest3() {
        assertThatThrownBy(() -> Member.create("commerce@gmail.com", "노성웅", "", "1996-04-23", "MAIL"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("생일 형식이 올바르지 않음")
    void validTest4() {

        assertThatThrownBy(() -> Member.create("commerce@gmail.com", "노성웅", "12345678", "1996-04-23222", "MAIL"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("생년월일 형식이 올바르지 않습니다. birth_date");
    }

    @Test
    @DisplayName("성별 형식이 올바르지 않음")
    void validTest5() {

        assertThatThrownBy(() -> Member.create("commerce@gmail.com", "노성웅", "12345678", "1996-04-23", "MAIL222"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("성별 형식 올바르지 않습니다. gender : ");
    }

}