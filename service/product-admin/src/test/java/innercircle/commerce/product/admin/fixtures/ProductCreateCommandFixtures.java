package innercircle.commerce.product.admin.fixtures;

import innercircle.commerce.product.admin.application.dto.ImageUploadCommand;
import innercircle.commerce.product.admin.application.dto.ProductCreateCommand;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static innercircle.commerce.product.admin.fixtures.ProductFixtures.*;

public class ProductCreateCommandFixtures {
    
    public static ProductCreateCommand createValidCommand() {
        return new ProductCreateCommand(
                VALID_NAME,
                VALID_CATEGORY_ID,
                VALID_BRAND_ID,
                VALID_BASE_PRICE,
                VALID_STOCK,
                VALID_DETAIL_CONTENT,
                null,
                createValidImageUploadCommand()
        );
    }
    
    public static ProductCreateCommand createValidCommandWithOptions() {
        return new ProductCreateCommand(
                VALID_NAME,
                VALID_CATEGORY_ID,
                VALID_BRAND_ID,
                VALID_BASE_PRICE,
                VALID_STOCK,
                VALID_DETAIL_CONTENT,
                ProductOptionFixtures.createValidOptions(),
                createValidImageUploadCommand()
        );
    }

    /**
     * 예외 테스트용 - 이미지 검증을 우회하기 위한 Command 생성
     */
    public static ProductCreateCommand createValidCommandForExceptionTest() {
        // 리플렉션을 사용하여 검증을 우회하거나, 최소한의 이미지만 포함
        MockMultipartFile dummyFile = new MockMultipartFile("image", "dummy.jpg", "image/jpeg", "dummy".getBytes());
        ImageUploadCommand dummyImageCommand = new ImageUploadCommand(
                List.of(dummyFile),
                List.of(new ImageUploadCommand.ImageMetadata(true, 1))
        );
        
        return new ProductCreateCommand(
                VALID_NAME,
                VALID_CATEGORY_ID,
                VALID_BRAND_ID,
                VALID_BASE_PRICE,
                VALID_STOCK,
                VALID_DETAIL_CONTENT,
                null,
                dummyImageCommand
        );
    }

    private static ImageUploadCommand createValidImageUploadCommand() {
        MockMultipartFile file1 = new MockMultipartFile("image", "test1.jpg", "image/jpeg", "test content 1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("image", "test2.jpg", "image/jpeg", "test content 2".getBytes());
        
        return new ImageUploadCommand(
                List.of(file1, file2),
                List.of(
                        new ImageUploadCommand.ImageMetadata(true, 1),  // 메인 이미지
                        new ImageUploadCommand.ImageMetadata(false, 2)  // 서브 이미지
                )
        );
    }
}
