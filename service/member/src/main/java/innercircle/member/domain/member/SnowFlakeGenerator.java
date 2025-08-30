package innercircle.member.domain.member;

import innercircle.commerce.common.snowflake.Snowflake;

public enum SnowFlakeGenerator {
    GENERATOR;

    private final Snowflake snowflake;

    SnowFlakeGenerator() {
        this.snowflake = new Snowflake();
    }

    public long nextId() {
        return this.snowflake.nextId();
    }
}