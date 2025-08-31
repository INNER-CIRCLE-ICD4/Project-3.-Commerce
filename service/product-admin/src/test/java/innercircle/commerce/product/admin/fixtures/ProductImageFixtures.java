package innercircle.commerce.product.admin.fixtures;

import innercircle.commerce.product.core.domain.ProductImage;

import java.util.List;

public class ProductImageFixtures {

	public static ProductImage createMainImage () {
		return ProductImage.create(
				1L,
				"http://example.com/main-image.jpg",
				"main-image.jpg",
				1
		);
	}

	public static ProductImage createSubImage () {
		return ProductImage.create(
				1L,
				"http://example.com/sub-image.jpg",
				"sub-image.jpg",
				2
		);
	}

	public static ProductImage createImage (String fileName, int sortOrder) {
		return ProductImage.create(
				1L,
				"http://example.com/" + fileName,
				fileName,
				sortOrder
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
				createImage("image1.jpg", 1),
				createImage("image2.jpg", 2)
		);
	}

	public static ProductImage createValidProductImage(Long productId) {
		return ProductImage.create(
				productId,
				"http://example.com/temp-image.jpg",
				"temp-image.jpg",
				1
		);
	}
}
