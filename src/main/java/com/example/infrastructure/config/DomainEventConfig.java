package com.example.infrastructure.config;

import com.example.domain.Member;
import com.example.domain.event.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuration class to inject the event publisher into domain entities.
 * This allows domain entities to publish events without depending on Spring directly.
 */
@Configuration
@RequiredArgsConstructor
public class DomainEventConfig {
    
    private final DomainEventPublisher eventPublisher;
    
    @PostConstruct
    public void configureEventPublishers() {
        // Inject event publisher into domain entities
        Member.setEventPublisher(eventPublisher);
    }
}