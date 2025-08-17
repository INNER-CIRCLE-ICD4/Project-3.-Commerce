package innercircle.commerce.product.core.domain;

import innercircle.commerce.common.snowflake.Snowflake;

public class IdGenerator {
	private static final Snowflake snowflake = new Snowflake();

	private IdGenerator () {}
	
	public static Long generateId() {
		return snowflake.nextId();
	}
}