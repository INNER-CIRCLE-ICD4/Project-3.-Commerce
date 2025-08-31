package innercircle.config;

import innercircle.member.application.port.out.PasswordEncoderPort;
import innercircle.member.domain.member.Email;
import innercircle.member.domain.member.Member;
import innercircle.member.domain.member.MemberRole;
import innercircle.member.infrastructure.persistence.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitialDataLoader implements CommandLineRunner {

    private final MemberJpaRepository memberJpaRepository;
    private final PasswordEncoderPort passwordEncoderPort;


    @Override
    public void run(String... args) throws Exception {
        createInitialAdmin();
    }


    private void createInitialAdmin() {
        String adminEmail = "sw.noh@gmail.com";

        if (memberJpaRepository.findByEmail(new Email(adminEmail)).isPresent()) {
            log.info("Admin already exists: " + adminEmail);
            return;
        }

        try {
            Member admin = Member.create(
                    adminEmail,
                    "노성웅",
                    "Commerce1234",
                    "1996-04-23",
                    "MALE"
            );

            Member saveMember = admin.withEncodedPassword(passwordEncoderPort.encode(admin.getPassword()));

            MemberRole.grantAdminRole(saveMember);
            MemberRole.grantSellerRole(saveMember);

            memberJpaRepository.save(saveMember);
        } catch (Exception e) {
            log.error("관리자 계정 생성 실패: {}", e.getMessage());
        }
    }
}
