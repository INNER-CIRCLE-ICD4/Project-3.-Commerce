package innercircle.member.infrastructure.adapter.in;

import com.fasterxml.jackson.databind.ObjectMapper;
import innercircle.commerce.common.snowflake.Snowflake;
import innercircle.member.application.MemberCreateRequest;
import innercircle.member.application.MemberResponse;
import innercircle.member.application.port.in.MemberUseCase;
import innercircle.member.domain.member.Gender;
import innercircle.member.domain.member.MemberStatus;
import innercircle.member.domain.member.RoleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.context.annotation.Import;
import innercircle.config.SecurityConfig;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberUseCase memberUseCase;


    @Test
    @DisplayName("회원가입 요청 - 성공")
    void memberCreate() throws Exception {

        MemberCreateRequest memberCreateRequest = new MemberCreateRequest("swnoh@google.com", "노성웅", "12345678A", "1996-04-23", "MAIL");
        MemberResponse response = new MemberResponse(new Snowflake().nextId(), memberCreateRequest.email(), memberCreateRequest.name(), LocalDate.of(1996, Month.APRIL, 23), Gender.MAIL.name(), MemberStatus.ACTIVE, LocalDateTime.now(), List.of(RoleType.BUYER.name()));

        when(memberUseCase.createMember(memberCreateRequest))
                .thenReturn(response);

        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").value(response.memberId()));

        verify(memberUseCase, times(1)).createMember(any(MemberCreateRequest.class));
        verifyNoMoreInteractions(memberUseCase);
    }
}