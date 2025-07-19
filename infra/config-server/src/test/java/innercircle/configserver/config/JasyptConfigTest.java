package innercircle.configserver.config;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimplePBEConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JasyptConfigTest {

//    @Test
//    void jasyptTest() {
//
//        String value = "변경할 데이터";
//
//        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
//        SimplePBEConfig config = new SimplePBEConfig();
//
//        config.setPassword("암호화할 비밀번호");
//        config.setAlgorithm("PBEWithMD5AndDES");
//
//        encryptor.setConfig(config);
//
//        String encryptedText = encryptor.encrypt(value);
//        String decryptedText = encryptor.decrypt(encryptedText);
//
//
//        System.out.println(encryptedText);
//        System.out.println(decryptedText);
//
//        assertThat(encryptedText).isNotEqualTo(value);
//        assertThat(decryptedText).isEqualTo(value);
//    }
}