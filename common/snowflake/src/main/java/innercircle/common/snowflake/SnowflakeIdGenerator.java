package innercircle.common.snowflake;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 임시 ID 생성기 (나중에 실제 Snowflake 알고리즘으로 교체 예정)
 */
public class SnowflakeIdGenerator {
    
    private static final AtomicLong counter = new AtomicLong(1);
    
    public static Long generate() {
        return counter.getAndIncrement();
    }
}
