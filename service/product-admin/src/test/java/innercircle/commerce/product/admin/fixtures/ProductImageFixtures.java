package innercircle.commerce.product.admin.fixtures;

import innercircle.commerce.product.core.domain.ProductImage;

import java.util.List;

public class ProductImageFixtures {

	public static ProductImage createMainImage () {
		return ProductImage.create(
				1L,
				"http://example.com/main-image.jpg",
				"main-image.jpg",
				true,
				1
		);
	}

	public static ProductImage createSubImage () {
		return ProductImage.create(
				1L,
				"http://example.com/sub-image.jpg",
				"sub-image.jpg",
				false,
				2
		);
	}

	public static ProductImage createImage (String fileName, boolean isMain) {
		return ProductImage.create(
				1L,
				"http://example.com/" + fileName,
				fileName,
				isMain,
				1
		);
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
