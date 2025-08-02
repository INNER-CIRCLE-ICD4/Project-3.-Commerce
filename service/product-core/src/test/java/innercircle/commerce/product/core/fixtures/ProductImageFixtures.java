package innercircle.commerce.product.core.fixtures;

import innercircle.commerce.product.core.domain.entity.ProductImage;

import java.util.List;

public class ProductImageFixtures {

	public static ProductImage createMainImage () {
		return ProductImage.builder()
						   .url("http://example.com/main-image.jpg")
						   .originalName("main-image.jpg")
						   .isMain(true)
						   .sortOrder(1)
						   .build();
	}

	public static ProductImage createSubImage () {
		return ProductImage.builder()
						   .url("http://example.com/sub-image.jpg")
						   .originalName("sub-image.jpg")
						   .isMain(false)
						   .sortOrder(2)
						   .build();
	}

	public static ProductImage createImage (String fileName, boolean isMain) {
		return ProductImage.builder()
						   .url("http://example.com/" + fileName)
						   .originalName(fileName)
						   .isMain(isMain)
						   .sortOrder(1)
						   .build();
	}

	public static List<ProductImage> createValidImages () {
		return List.of(
				createMainImage(),
				createSubImage()
		);
	}

	public static List<ProductImage> createImagesWithoutMain () {
		return List.of(
				createImage("image1.jpg", false),
				createImage("image2.jpg", false)
		);
	}
}
