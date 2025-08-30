package innercircle.commerce.product.admin.application.event;

import innercircle.commerce.product.core.domain.event.ProductDeletedEvent;
import innercircle.commerce.product.infra.s3.S3ImageStore;
import innercircle.commerce.product.infra.s3.S3UrlHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("상품 이벤트 리스너 테스트")
class ProductEventListenerTest {

    @Mock
    private S3ImageStore s3ImageStore;
    
    @Mock
    private S3UrlHelper s3UrlHelper;

    private ProductEventListener productEventListener;

    @BeforeEach
    void setUp() {
        productEventListener = new ProductEventListener(s3ImageStore, s3UrlHelper);
    }

    @Test
    @DisplayName("상품 삭제 이벤트 처리 시 S3 이미지들을 삭제한다.")
    void 상품_삭제_이벤트_처리_성공() {
        // given
        Long productId = 1L;
        List<String> imageUrls = List.of(
                "https://bucket.s3.amazonaws.com/commerce/products/1/image1.jpg",
                "https://bucket.s3.amazonaws.com/commerce/products/1/image2.jpg"
        );
        ProductDeletedEvent event = new ProductDeletedEvent(productId, imageUrls);
        
        given(s3UrlHelper.extractKeyFromUrl(imageUrls.get(0)))
                .willReturn("commerce/products/1/image1.jpg");
        given(s3UrlHelper.extractKeyFromUrl(imageUrls.get(1)))
                .willReturn("commerce/products/1/image2.jpg");

        // when
        productEventListener.onProductDeleted(event);

        // then
        verify(s3ImageStore).delete("commerce/products/1/image1.jpg");
        verify(s3ImageStore).delete("commerce/products/1/image2.jpg");
    }

    @Test
    @DisplayName("이미지 URL이 없는 경우 S3 삭제를 수행하지 않는다.")
    void 이미지_없는_경우_S3_삭제_안함() {
        // given
        Long productId = 1L;
        List<String> emptyImageUrls = List.of();
        ProductDeletedEvent event = new ProductDeletedEvent(productId, emptyImageUrls);

        // when
        productEventListener.onProductDeleted(event);

        // then
        verify(s3ImageStore, never()).delete(anyString());
    }

    @Test
    @DisplayName("S3 삭제 실패 시에도 다른 이미지는 계속 처리한다.")
    void S3_삭제_실패_시_다른_이미지_계속_처리() {
        // given
        Long productId = 1L;
        List<String> imageUrls = List.of(
                "https://bucket.s3.amazonaws.com/commerce/products/1/image1.jpg",
                "https://bucket.s3.amazonaws.com/commerce/products/1/image2.jpg"
        );
        ProductDeletedEvent event = new ProductDeletedEvent(productId, imageUrls);

        given(s3UrlHelper.extractKeyFromUrl(imageUrls.get(0)))
                .willReturn("commerce/products/1/image1.jpg");
        given(s3UrlHelper.extractKeyFromUrl(imageUrls.get(1)))
                .willReturn("commerce/products/1/image2.jpg");

        // 첫 번째 이미지 삭제 실패
        doThrow(new RuntimeException("S3 connection failed"))
                .when(s3ImageStore).delete("commerce/products/1/image1.jpg");

        // when
        productEventListener.onProductDeleted(event);

        // then
        verify(s3ImageStore).delete("commerce/products/1/image1.jpg");
        verify(s3ImageStore).delete("commerce/products/1/image2.jpg"); // 두 번째는 계속 처리
    }

    @Test
    @DisplayName("잘못된 URL 형식인 경우 예외가 발생하지만 다른 이미지는 계속 처리한다.")
    void 잘못된_URL_형식_처리() {
        // given
        Long productId = 1L;
        List<String> imageUrls = List.of(
                "invalid-url", // 잘못된 URL
                "https://bucket.s3.amazonaws.com/commerce/products/1/image2.jpg" // 정상 URL
        );
        ProductDeletedEvent event = new ProductDeletedEvent(productId, imageUrls);

        // 첫 번째 URL은 잘못된 URL이므로 예외 발생
        given(s3UrlHelper.extractKeyFromUrl("invalid-url"))
                .willThrow(new IllegalArgumentException("유효하지 않은 S3 URL입니다: invalid-url"));
        // 두 번째 URL은 정상 처리
        given(s3UrlHelper.extractKeyFromUrl(imageUrls.get(1)))
                .willReturn("commerce/products/1/image2.jpg");

        // when
        productEventListener.onProductDeleted(event);

        // then
        // 첫 번째 URL은 잘못되어 S3 삭제 호출 안됨
        verify(s3ImageStore, never()).delete("invalid-url");
        // 두 번째 URL은 정상 처리
        verify(s3ImageStore).delete("commerce/products/1/image2.jpg");
    }
}