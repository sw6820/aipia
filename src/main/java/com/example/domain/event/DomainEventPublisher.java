package com.example.domain.event;

/**
 * Interface for publishing domain events.
 * This allows the domain layer to publish events without depending on infrastructure.
 */
public interface DomainEventPublisher {
    
    /**
     * Publish a domain event.
     * @param event The domain event to publish
     */
    void publish(DomainEvent event);
}