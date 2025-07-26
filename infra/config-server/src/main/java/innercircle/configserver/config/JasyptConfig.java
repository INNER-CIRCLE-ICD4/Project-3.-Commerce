package innercircle.configserver.config;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimplePBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class JasyptConfig {

    @Value("${jasypt.encryptor.password}")
    String password;

    final static String ALGORITHM = "PBEWithMD5AndDES";

    @Bean(name = "jasyptEncryptor")
    public StringEncryptor jasyptStringEncryptor() {
        log.info("password: {}", password);
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimplePBEConfig config = new SimplePBEConfig();

        config.setKeyObtentionIterations("1000");
        config.setPoolSize(1);
        config.setPassword(password);
        config.setProviderName("SunJCE");
        config.setAlgorithm(ALGORITHM);
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");

        encryptor.setConfig(config);
        return encryptor;
    }
}
