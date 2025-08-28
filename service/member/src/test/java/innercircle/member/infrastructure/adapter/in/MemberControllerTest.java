package innercircle.member.infrastructure.adapter.in;

import com.fasterxml.jackson.databind.ObjectMapper;
import innercircle.commerce.common.snowflake.Snowflake;
import innercircle.member.infrastructure.adapter.in.web.member.dto.MemberCreateRequest;
import innercircle.member.infrastructure.adapter.in.web.member.dto.MemberCreateResponse;
import innercircle.member.application.port.in.MemberUseCase;
import innercircle.member.domain.member.*;
import innercircle.member.infrastructure.adapter.in.web.member.mapper.MemberWebMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
    private MemberWebMapper memberWebMapper;

    @MockitoBean
    private MemberUseCase memberUseCase;



    @Test
    @DisplayName("회원가입 요청 - 성공")
    void memberCreate() throws Exception {


        MemberCreateRequest memberCreateRequest = new MemberCreateRequest("asdz453@gmail.com", "노성웅", "12345678a", "1996-04-23", "MAIL");

        Member member = Member.create("asdz453@gmail.com", "노성웅", "12345678a", "1996-04-23", "MAIL");
        Member encodedMember = member.withEncodedPassword("encodedPassword1234");

        long id = SnowFlakeGenerator.GENERATOR.nextId();
        ReflectionTestUtils.setField(member, "id", id);

        MemberCreateResponse response = new MemberCreateResponse(new Snowflake().nextId(), memberCreateRequest.email(), memberCreateRequest.name(), LocalDate.of(1996, Month.APRIL, 23), Gender.MAIL.name(), MemberStatus.ACTIVE, LocalDateTime.now(), List.of(RoleType.BUYER.name()));

        when(memberWebMapper.createRequestToEntity(memberCreateRequest)).thenReturn(member);
        when(memberUseCase.createMember(member)).thenReturn(encodedMember);
        when(memberWebMapper.entityToCreateResponse(encodedMember)).thenReturn(response);  // ✅ 누락된 Mock 추가!

        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").value(response.memberId()))
                .andExpect(jsonPath("$.email").value(response.email()))
                .andDo(print());

        verify(memberUseCase, times(1)).createMember(any(Member.class));
        verifyNoMoreInteractions(memberUseCase);
    }
}