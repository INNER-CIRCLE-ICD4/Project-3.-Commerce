package innercircle.member.domain;

import innercircle.member.infrastructure.persistence.MemberJpaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class MemberRepositoryTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    void saveMember() {

        Member member = Member.create("asdz453@gmail.com", "노성웅", "password1234", "2025-07-21", "MAIL");

        Member save = memberJpaRepository.save(member);

        assertThat(save.getId()).isNotNull();
        assertThat(save.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(save.getRoles()).hasSize(1);
        assertThat(save.getRoles().get(0).getRoleType()).isEqualTo(RoleType.BUYER);
    }

    @Test
    void findByEmail() {

        Member member = Member.create("asdz453@gmail.com", "노성웅", "password1234", "2025-07-21", "MAIL");
        memberJpaRepository.save(member);

        entityManager.flush();
        entityManager.clear();

        Optional<Member> byEmail = memberJpaRepository.findByEmail(new Email("asdz453@gmail.com"));

        assertThat(byEmail.isPresent()).isTrue();
        assertThat(byEmail.get().getRoles()).hasSize(1);
    }

}