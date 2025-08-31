package innercircle.commerce.product.admin.fixtures;

import innercircle.commerce.product.core.domain.ProductOption;
import innercircle.commerce.product.core.domain.ProductOptionItem;

import java.util.Collections;
import java.util.List;

public class ProductOptionFixtures {

	public static ProductOptionItem createColorOptionItem (String colorName, Integer additionalPrice) {
		return ProductOptionItem.create(
				1L,
				colorName,
				additionalPrice,
				1
		);
	}

	public static ProductOptionItem createRedOptionItem () {
		return createColorOptionItem("빨강", 0);
	}

	public static ProductOptionItem createBlueOptionItem () {
		return createColorOptionItem("파랑", 1000);
	}

	public static ProductOption createColorOption () {
		List<ProductOptionItem> items = List.of(
				createRedOptionItem(),
				createBlueOptionItem()
		);

		return ProductOption.create(
				1L,
				"색상",
				true,
				1,
				items
		);
	}

	public static ProductOption createSizeOption () {
		List<ProductOptionItem> items = List.of(
				ProductOptionItem.create(
						1L,
						"S",
						0,
						1
				),
				ProductOptionItem.create(
						1L,
						"M",
						0,
						2
				),
				ProductOptionItem.create(
						1L,
						"L",
						2000,
						3
				)
		);

		return ProductOption.create(
				1L,
				"사이즈",
				true,
				2,
				items
		);
	}

	public static ProductOption createOptionWithoutItems (String optionName, boolean isRequired) {
		return ProductOption.create(
				1L,
				optionName,
				isRequired,
				1,
				Collections.emptyList()
		);
	}

	public static List<ProductOption> createValidOptions () {
		return List.of(createColorOption());
	}

	public static List<ProductOption> createMultipleOptions () {
		return List.of(
				createColorOption(),
				createSizeOption()
		);
	}
}
