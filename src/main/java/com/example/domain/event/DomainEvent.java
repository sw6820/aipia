package com.example.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base interface for domain events.
 * Domain events represent something important that happened in the domain.
 */
public interface DomainEvent {
    
    /**
     * Unique identifier for this event.
     */
    UUID getEventId();
    
    /**
     * When this event occurred.
     */
    LocalDateTime getOccurredOn();
    
    /**
     * The type of this event.
     */
    String getEventType();
}