package innercircle.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import innercircle.member.infrastructure.adapter.in.web.dto.MemberCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MemberApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void createMember_Integration() throws Exception {

        MemberCreateRequest memberCreateRequest = new MemberCreateRequest(
                "swnoh@commerce.com",
                "노성웅",
                "12345678A",
                "1996-04-23",
                "MAIL"
        );

        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("swnoh@commerce.com"))
                .andExpect(jsonPath("$.gender").value("MAIL"))
                .andExpect(jsonPath("$.memberRoles").isArray());
    }

}
