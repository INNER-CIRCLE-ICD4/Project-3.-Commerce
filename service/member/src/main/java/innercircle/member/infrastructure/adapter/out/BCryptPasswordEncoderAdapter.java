package innercircle.member.infrastructure.adapter.out;


import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies;
import innercircle.member.application.port.out.PasswordEncoderPort;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoderPort {

    private static final int COST = 12;

    private final BCrypt.Hasher hasher;
    private final BCrypt.Verifyer verifyer;

    public BCryptPasswordEncoderAdapter() {
        this.hasher = BCrypt.with(LongPasswordStrategies.hashSha512(BCrypt.Version.VERSION_2A));
        this.verifyer = BCrypt.verifyer();
    }

    @Override
    public String encode(String rawPassword) {

        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("비밀번호는  null이거나 비어있을 수 없읍니다.");
        }

        if (rawPassword.length() < 8) {
            throw new IllegalArgumentException("비밀번호는  8자리 이상 입력할 수 있습니다.");
        }

        if (rawPassword.length() > 72) {
            throw new IllegalArgumentException("BCrypt는 72자를 초과하는 비밀번호를 지원하지 않습니다.");
        }

        return hasher.hashToString(COST, rawPassword.toCharArray());
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {

        if (rawPassword == null || encodedPassword == null) {
            return false;
        }

        try {
            BCrypt.Result result = verifyer.verify(rawPassword.toCharArray(), encodedPassword);
            return result.verified;
        } catch (Exception e) {
            return false;
        }
    }
}
