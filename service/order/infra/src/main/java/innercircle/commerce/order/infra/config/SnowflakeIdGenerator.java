package innercircle.commerce.order.infra.config;

import innercircle.commerce.common.snowflake.Snowflake;
import innercircle.commerce.order.domain.services.IdGenerator;
import org.springframework.stereotype.Component;

@Component
public class SnowflakeIdGenerator implements IdGenerator {

    private final Snowflake snowflake;

    public SnowflakeIdGenerator(Snowflake snowflake) {
        this.snowflake = snowflake;
    }

    @Override
    public long generateId() {
        return snowflake.nextId();
    }
}
