package innercircle.commerce.product.core.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Product 도메인 테스트")
class ProductTest {


    @Nested
    @DisplayName("이미지 개별 삭제")
    class RemoveImage {

        @Test
        @DisplayName("URL을 기반으로 이미지를 개별 삭제할 수 있다.")
        void URL_기반_이미지_삭제_성공() {
            // given
            Product product = createProductWithImages(List.of(
                    createProductImage("image1.jpg", 1), // 메인 이미지
                    createProductImageWithMain("image2.jpg", 2, true) // 다른 메인 이미지
            ));
            String targetUrl = "https://s3.amazonaws.com/image1.jpg";

            // when
            product.removeImageByUrl(targetUrl);

            // then
            assertThat(product.getImages()).hasSize(1);
            assertThat(product.getImages().get(0).getUrl()).isEqualTo("https://s3.amazonaws.com/image2.jpg");
        }

        @Test
        @DisplayName("마지막 이미지를 삭제하면 예외가 발생한다.")
        void 마지막_이미지_삭제_예외() {
            // given
            Product product = createProductWithImages(List.of(
                    createProductImage("image1.jpg", 1) // 메인 이미지
            ));
            String targetUrl = "https://s3.amazonaws.com/image1.jpg";

            // when & then
            assertThatThrownBy(() -> product.removeImageByUrl(targetUrl))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("상품 이미지는 필수 입니다.");
        }

        @Test
        @DisplayName("메인 이미지가 모두 삭제되면 예외가 발생한다.")
        void 메인이미지_모두_삭제_예외() {
            // given
            Product product = createProductWithImages(List.of(
                    createProductImageWithMain("image1.jpg", 1, true),  // 메인 이미지
                    createProductImageWithMain("image2.jpg", 2, false)  // 일반 이미지
            ));
            String targetUrl = "https://s3.amazonaws.com/image1.jpg"; // 메인 이미지 URL

            // when & then
            assertThatThrownBy(() -> product.removeImageByUrl(targetUrl))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("메인 이미지는 필수 입니다.");
        }

        @Test
        @DisplayName("존재하지 않는 URL로 삭제 시도하면 예외가 발생한다.")
        void 존재하지_않는_URL_삭제_예외() {
            // given
            Product product = createProductWithImages(List.of(
                    createProductImage("image1.jpg", 1)
            ));
            String nonExistentUrl = "https://s3.amazonaws.com/non-existent.jpg";

            // when & then
            assertThatThrownBy(() -> product.removeImageByUrl(nonExistentUrl))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("해당 URL의 이미지를 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("이미지 추가")
    class AddImages {

        @Test
        @DisplayName("새로운 이미지들을 추가할 수 있다.")
        void 이미지_추가_성공() {
            // given
            Product product = createProductWithImages(List.of(
                    createProductImage("existing.jpg", 1)
            ));
            List<ProductImage> newImages = List.of(
                    createProductImage("new1.jpg", 2),
                    createProductImage("new2.jpg", 3)
            );

            // when
            product.addImages(newImages);

            // then
            assertThat(product.getImages()).hasSize(3);
            assertThat(product.getImages().get(1).getUrl()).isEqualTo("https://s3.amazonaws.com/new1.jpg");
            assertThat(product.getImages().get(2).getUrl()).isEqualTo("https://s3.amazonaws.com/new2.jpg");
        }

        @Test
        @DisplayName("기존 이미지와 새 이미지의 총합이 6개를 초과하면 예외가 발생한다.")
        void 총_이미지_개수_초과_예외() {
            // given
            Product product = createProductWithImages(List.of(
                    createProductImage("existing1.jpg", 1),
                    createProductImage("existing2.jpg", 2),
                    createProductImage("existing3.jpg", 3),
                    createProductImage("existing4.jpg", 4)
            )); // 기존 4개
            
            List<ProductImage> newImages = List.of(
                    createProductImage("new1.jpg", 5),
                    createProductImage("new2.jpg", 6),
                    createProductImage("new3.jpg", 7) // 3개 더 추가 = 총 7개 (초과)
            );

            // when & then
            assertThatThrownBy(() -> product.addImages(newImages))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("상품 이미지는 6개까지 등록할 수 있습니다.");
        }

        @Test
        @DisplayName("빈 이미지 목록으로 추가하면 예외가 발생한다.")
        void 빈_이미지_목록_추가_예외() {
            // given
            Product product = createProductWithImages(List.of(
                    createProductImage("existing.jpg", 1)
            ));

            // when & then
            assertThatThrownBy(() -> product.addImages(List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("추가할 이미지는 최소 1개 이상이어야 합니다.");
        }
    }

    private Product createProductWithImages(List<ProductImage> images) {
        Product product = Product.create(
                "테스트 상품",
                1L, // categoryId
                1L, // brandId
                10000, // price
                100, // stock
                List.of(), // options
                "상품 상세 설명" // detailContent
        );
        
        if (images != null && !images.isEmpty()) {
            product.addImages(images);
        }
        
        return product;
    }

    private ProductImage createProductImage(String fileName, int displayOrder) {
        return ProductImage.create(
                1L, // productId
                "https://s3.amazonaws.com/" + fileName,
                fileName,
                displayOrder == 1, // 첫 번째 이미지를 메인으로 설정
                displayOrder
        );
    }

    private ProductImage createProductImageWithMain(String fileName, int displayOrder, boolean isMain) {
        return ProductImage.create(
                1L, // productId
                "https://s3.amazonaws.com/" + fileName,
                fileName,
                isMain,
                displayOrder
        );
    }
}