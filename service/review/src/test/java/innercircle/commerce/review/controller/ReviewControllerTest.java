package innercircle.commerce.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import innercircle.commerce.context.support.WithMockCustomUser;
import innercircle.commerce.review.dto.ReviewReq;
import innercircle.commerce.review.entity.Review;
import innercircle.commerce.review.repository.ReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // ⭐️ 1. 애플리케이션의 모든 Bean을 로드하는 통합 테스트로 변경
@AutoConfigureMockMvc
@Transactional
class ReviewControllerTest {
    private final String API_VERSION = "v1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    @DisplayName("로그인된 사용자가 리뷰 작성 API를 호출하면 성공적으로 DB에 저장된다")
    @WithMockCustomUser(memberId = 123L)
    void createReview_integration_test_success() throws Exception {
        // given (준비)
        // API 요청에 사용할 DTO 생성
        ReviewReq requestDto = ReviewReq.builder()
                .productId(100L)
                .rating(5)
                .content("통합 테스트 리뷰입니다.")
                .build();

        String requestBody = objectMapper.writeValueAsString(requestDto);

        // when (실행)
        // POST /reviews API를 실제처럼 호출
        mockMvc.perform(post("api/" + API_VERSION + "/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated()); // 201 Created 응답을 기대

        // then (검증)
        // ⭐️ DB에 데이터가 올바르게 저장되었는지 '상태'를 직접 검증
        List<Review> reviews = reviewRepository.findAll();
        assertThat(reviews).hasSize(1); // 리뷰가 1개 저장되었는지 확인

        Review savedReview = reviews.get(0);
        assertThat(savedReview.getMemberId()).isEqualTo(123L); // ⭐️ @WithMockCustomUser의 memberId와 일치하는지 확인
        assertThat(savedReview.getProductId()).isEqualTo(100L);
        assertThat(savedReview.getContent()).isEqualTo("통합 테스트 리뷰입니다.");
    }
}