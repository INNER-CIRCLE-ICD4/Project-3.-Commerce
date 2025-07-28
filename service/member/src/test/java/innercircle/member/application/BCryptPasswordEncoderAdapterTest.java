package innercircle.member.application;

import innercircle.member.infrastructure.BCryptPasswordEncoderAdapter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class BCryptPasswordEncoderAdapterTest {

    private BCryptPasswordEncoderAdapter passwordEncoderAdapter;

    @BeforeEach
    public void setup() {
        this.passwordEncoderAdapter = new BCryptPasswordEncoderAdapter();
    }


    @Test
    void encodePassword() {

        String rawPassword = "12345678a";

        String encode = passwordEncoderAdapter.encode(rawPassword);

        System.out.println("encode = " + encode);

        Assertions.assertThat(passwordEncoderAdapter.matches(rawPassword, encode)).isTrue();
    }

}