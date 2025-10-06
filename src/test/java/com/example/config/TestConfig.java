package com.example.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@TestConfiguration
@Profile("test")
public class TestConfig {

    /**
     * 테스트용 고정된 Clock을 제공하여 시간 관련 테스트의 일관성을 보장합니다.
     */
    @Bean
    @Primary
    public Clock testClock() {
        // 테스트용 고정 시간: 2024-01-01 12:00:00 UTC
        return Clock.fixed(
                Instant.parse("2024-01-01T12:00:00Z"),
                ZoneId.of("UTC")
        );
    }
}