package com.example.domain.event;

import com.example.domain.Member;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain events related to Member entity.
 */
public class MemberEvents {
    
    @Getter
    public static class MemberCreated implements DomainEvent {
        private final UUID eventId = UUID.randomUUID();
        private final LocalDateTime occurredOn = LocalDateTime.now();
        private final String eventType = "MemberCreated";
        
        private final Long memberId;
        private final String email;
        private final String name;
        
        public MemberCreated(Member member) {
            this.memberId = member.getId();
            this.email = member.getEmail();
            this.name = member.getName();
        }
        
        @Override
        public UUID getEventId() {
            return eventId;
        }
        
        @Override
        public LocalDateTime getOccurredOn() {
            return occurredOn;
        }
        
        @Override
        public String getEventType() {
            return eventType;
        }
    }
    
    @Getter
    public static class MemberDeactivated implements DomainEvent {
        private final UUID eventId = UUID.randomUUID();
        private final LocalDateTime occurredOn = LocalDateTime.now();
        private final String eventType = "MemberDeactivated";
        
        private final Long memberId;
        private final String email;
        
        public MemberDeactivated(Member member) {
            this.memberId = member.getId();
            this.email = member.getEmail();
        }
        
        @Override
        public UUID getEventId() {
            return eventId;
        }
        
        @Override
        public LocalDateTime getOccurredOn() {
            return occurredOn;
        }
        
        @Override
        public String getEventType() {
            return eventType;
        }
    }
    
    @Getter
    public static class MemberActivated implements DomainEvent {
        private final UUID eventId = UUID.randomUUID();
        private final LocalDateTime occurredOn = LocalDateTime.now();
        private final String eventType = "MemberActivated";
        
        private final Long memberId;
        private final String email;
        
        public MemberActivated(Member member) {
            this.memberId = member.getId();
            this.email = member.getEmail();
        }
        
        @Override
        public UUID getEventId() {
            return eventId;
        }
        
        @Override
        public LocalDateTime getOccurredOn() {
            return occurredOn;
        }
        
        @Override
        public String getEventType() {
            return eventType;
        }
    }
}