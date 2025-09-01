package innercircle.commerce.product.core.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ImageDeletionTarget 도메인 엔티티")
class ImageDeletionTargetTest {

    @Nested
    @DisplayName("생성")
    class Create {
        
        @Test
        @DisplayName("유효한 파라미터로 삭제 대상을 정상적으로 생성할 수 있다")
        void 삭제_대상_생성_성공() {
            // given
            String imageUrl = "https://example.com/image.jpg";
            int delayDays = 30;
            
            // when
            ImageDeletionTarget target = ImageDeletionTarget.create(imageUrl, delayDays);
            
            // then
            assertThat(target).isNotNull();
            assertThat(target.getId()).isNotNull();
            assertThat(target.getImageUrl()).isEqualTo(imageUrl);
            assertThat(target.getRetryCount()).isZero();
            assertThat(target.getCreatedAt()).isNotNull();
            assertThat(target.getUpdatedAt()).isNotNull();
            assertThat(target.getScheduledDeletionAt())
                    .isAfter(LocalDateTime.now().plusDays(delayDays).minusMinutes(1))
                    .isBefore(LocalDateTime.now().plusDays(delayDays).plusMinutes(1));
        }
        
        @Test
        @DisplayName("이미지 URL이 null인 경우 예외가 발생한다")
        void 이미지_URL_null_예외() {
            // given
            String imageUrl = null;
            int delayDays = 30;
            
            // when & then
            assertThatThrownBy(() -> ImageDeletionTarget.create(imageUrl, delayDays))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미지 URL은 필수입니다.");
        }
        
        @Test
        @DisplayName("이미지 URL이 빈 문자열인 경우 예외가 발생한다")
        void 이미지_URL_빈문자열_예외() {
            // given
            String imageUrl = "";
            int delayDays = 30;
            
            // when & then
            assertThatThrownBy(() -> ImageDeletionTarget.create(imageUrl, delayDays))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미지 URL은 필수입니다.");
        }
        
        @Test
        @DisplayName("지연 일수가 음수인 경우 예외가 발생한다")
        void 지연_일수_음수_예외() {
            // given
            String imageUrl = "https://example.com/image.jpg";
            int delayDays = -1;
            
            // when & then
            assertThatThrownBy(() -> ImageDeletionTarget.create(imageUrl, delayDays))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("지연 일수는 0 이상이어야 합니다.");
        }
        
        @Test
        @DisplayName("이미지 URL 앞뒤 공백이 제거된다")
        void 이미지_URL_공백_제거() {
            // given
            String imageUrl = "  https://example.com/image.jpg  ";
            int delayDays = 30;
            
            // when
            ImageDeletionTarget target = ImageDeletionTarget.create(imageUrl, delayDays);
            
            // then
            assertThat(target.getImageUrl()).isEqualTo("https://example.com/image.jpg");
        }
    }

    @Nested
    @DisplayName("복원")
    class Restore {
        
        @Test
        @DisplayName("모든 필드를 포함하여 삭제 대상을 복원할 수 있다")
        void 삭제_대상_복원_성공() {
            // given
            Long id = 123L;
            String imageUrl = "https://example.com/image.jpg";
            LocalDateTime scheduledDeletionAt = LocalDateTime.now().plusDays(30);
            int retryCount = 2;
            LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
            LocalDateTime updatedAt = LocalDateTime.now();
            
            // when
            ImageDeletionTarget target = ImageDeletionTarget.restore(
                    id, imageUrl, scheduledDeletionAt, retryCount, createdAt, updatedAt);
            
            // then
            assertThat(target.getId()).isEqualTo(id);
            assertThat(target.getImageUrl()).isEqualTo(imageUrl);
            assertThat(target.getScheduledDeletionAt()).isEqualTo(scheduledDeletionAt);
            assertThat(target.getRetryCount()).isEqualTo(retryCount);
            assertThat(target.getCreatedAt()).isEqualTo(createdAt);
            assertThat(target.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    @Nested
    @DisplayName("재시도 카운트 증가")
    class IncrementRetryCount {
        
        @Test
        @DisplayName("재시도 카운트를 증가시키고 수정 시간을 갱신한다")
        void 재시도_카운트_증가() {
            // given
            ImageDeletionTarget target = ImageDeletionTarget.create("https://example.com/image.jpg", 30);
            int initialRetryCount = target.getRetryCount();
            LocalDateTime initialUpdatedAt = target.getUpdatedAt();
            
            // when
            target.incrementRetryCount();
            
            // then
            assertThat(target.getRetryCount()).isEqualTo(initialRetryCount + 1);
            assertThat(target.getUpdatedAt()).isAfter(initialUpdatedAt);
        }
    }

    @Nested
    @DisplayName("실패 후 재스케줄링")
    class RescheduleAfterFailure {
        
        @Test
        @DisplayName("실패 후 지정된 시간만큼 지연하여 재스케줄링한다")
        void 실패_후_재스케줄링() {
            // given
            ImageDeletionTarget target = ImageDeletionTarget.create("https://example.com/image.jpg", 30);
            LocalDateTime initialScheduledAt = target.getScheduledDeletionAt();
            LocalDateTime initialUpdatedAt = target.getUpdatedAt();
            int delayHours = 2;
            
            // when
            target.rescheduleAfterFailure(delayHours);
            
            // then
            assertThat(target.getScheduledDeletionAt())
                    .isAfter(LocalDateTime.now().plusHours(delayHours).minusMinutes(1))
                    .isBefore(LocalDateTime.now().plusHours(delayHours).plusMinutes(1));
            assertThat(target.getUpdatedAt()).isAfter(initialUpdatedAt);
        }
    }

    @Nested
    @DisplayName("삭제 예정 확인")
    class IsScheduledForDeletion {
        
        @Test
        @DisplayName("예정된 삭제 시간이 지났으면 true를 반환한다")
        void 삭제_시간_경과_true() {
            // given
            LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
            ImageDeletionTarget target = ImageDeletionTarget.restore(
                    123L, "https://example.com/image.jpg", pastTime, 0, 
                    LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
            
            // when
            boolean result = target.isScheduledForDeletion();
            
            // then
            assertThat(result).isTrue();
        }
        
        @Test
        @DisplayName("예정된 삭제 시간이 아직 지나지 않았으면 false를 반환한다")
        void 삭제_시간_미경과_false() {
            // given
            LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
            ImageDeletionTarget target = ImageDeletionTarget.restore(
                    123L, "https://example.com/image.jpg", futureTime, 0, 
                    LocalDateTime.now().minusDays(1), LocalDateTime.now());
            
            // when
            boolean result = target.isScheduledForDeletion();
            
            // then
            assertThat(result).isFalse();
        }
    }
}