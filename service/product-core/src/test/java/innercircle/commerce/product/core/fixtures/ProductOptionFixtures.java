package innercircle.commerce.product.core.fixtures;

import innercircle.commerce.product.core.domain.entity.ProductOption;
import innercircle.commerce.product.core.domain.entity.ProductOptionItem;

import java.util.Collections;
import java.util.List;

public class ProductOptionFixtures {

	public static ProductOptionItem createColorOptionItem (String colorName, Integer additionalPrice) {
		return ProductOptionItem.builder()
								.name(colorName)
								.additionalPrice(additionalPrice)
								.sortOrder(1)
								.build();
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

		return ProductOption.builder()
							.name("색상")
							.isRequired(true)
							.sortOrder(1)
							.items(items)
							.build();
	}

	public static ProductOption createSizeOption () {
		List<ProductOptionItem> items = List.of(
				ProductOptionItem.builder()
								 .name("S")
								 .additionalPrice(0)
								 .sortOrder(1)
								 .build(),
				ProductOptionItem.builder()
								 .name("M")
								 .additionalPrice(0)
								 .sortOrder(2)
								 .build(),
				ProductOptionItem.builder()
								 .name("L")
								 .additionalPrice(2000)
								 .sortOrder(3)
								 .build()
		);

		return ProductOption.builder()
							.name("사이즈")
							.isRequired(true)
							.sortOrder(2)
							.items(items)
							.build();
	}

	public static ProductOption createOptionWithoutItems (String optionName, boolean isRequired) {
		return ProductOption.builder()
							.name(optionName)
							.isRequired(isRequired)
							.sortOrder(1)
							.items(Collections.emptyList())
							.build();
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
