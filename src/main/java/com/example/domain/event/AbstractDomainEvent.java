package com.example.domain.event;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Abstract base class for domain events providing common implementation.
 */
@Getter
public abstract class AbstractDomainEvent implements DomainEvent {
    
    private final UUID eventId;
    private final LocalDateTime occurredOn;
    private final String eventType;
    private final String aggregateId;
    private final Long aggregateVersion;
    
    protected AbstractDomainEvent(String aggregateId, Long aggregateVersion) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = LocalDateTime.now();
        this.eventType = this.getClass().getSimpleName();
        this.aggregateId = aggregateId;
        this.aggregateVersion = aggregateVersion;
    }
}