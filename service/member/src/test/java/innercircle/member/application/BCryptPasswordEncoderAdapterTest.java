package innercircle.member.application;

import innercircle.member.infrastructure.adapter.out.BCryptPasswordEncoderAdapter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class BCryptPasswordEncoderAdapterTest {

    private BCryptPasswordEncoderAdapter passwordEncoderAdapter;

    @BeforeEach
    public void setup() {
        this.passwordEncoderAdapter = new BCryptPasswordEncoderAdapter();
    }


    @Test
    void encodePassword() {

        String rawPassword = "12345678A";

        String encode = passwordEncoderAdapter.encode(rawPassword);

        System.out.println("encode = " + encode);

        Assertions.assertThat(passwordEncoderAdapter.matches(rawPassword, encode)).isTrue();
    }

    @Test
    void encodeFailed() {

        String rawPassword1 = "123456";
        assertThatThrownBy(() -> passwordEncoderAdapter.encode(rawPassword1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("비밀번호는 8자 이상이어야 합니다.");

        String rawPassword2 = "12345678aa";
        assertThatThrownBy(() -> passwordEncoderAdapter.encode(rawPassword2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호는 대문자를 포함해야 합니다.");

        String rawPassword3 = "asdasdasdasdAA";
        assertThatThrownBy(() -> passwordEncoderAdapter.encode(rawPassword3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호는 숫자를 포함해야 합니다.");

    }

}