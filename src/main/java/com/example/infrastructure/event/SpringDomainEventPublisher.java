package com.example.infrastructure.event;

import com.example.domain.event.DomainEvent;
import com.example.domain.event.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Spring-based implementation of DomainEventPublisher.
 * Uses Spring's ApplicationEventPublisher to publish domain events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SpringDomainEventPublisher implements DomainEventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    @Override
    public void publish(DomainEvent event) {
        log.debug("Publishing domain event: {} with ID: {}", event.getEventType(), event.getEventId());
        applicationEventPublisher.publishEvent(event);
    }
}