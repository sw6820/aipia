package com.example.domain;

import com.example.domain.event.DomainEventPublisher;
import com.example.domain.event.MemberEvents;
import com.example.domain.valueobject.Email;
import com.example.domain.valueobject.PhoneNumber;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Member {
    
    // Static field for event publisher - injected by Spring
    private static DomainEventPublisher eventPublisher;
    
    /**
     * Set the event publisher (called by Spring configuration).
     */
    public static void setEventPublisher(DomainEventPublisher eventPublisher) {
        Member.eventPublisher = eventPublisher;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", nullable = false, unique = true, length = 50))
    private Email email;

    @Column(nullable = false, length = 100)
    private String name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "phone_number", nullable = false, length = 20))
    @AttributeOverride(name = "countryCode", column = @Column(name = "country_code", length = 3))
    private PhoneNumber phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Member(String email, String name, String phoneNumber) {
        if (name == null || name.trim().isEmpty()) {
            throw new NullPointerException("Name cannot be null or empty");
        }
        
        // Handle null email case
        if (email == null) {
            throw new NullPointerException("Email cannot be null");
        }
        
        // Handle empty/whitespace email case
        if (email.trim().isEmpty()) {
            throw new NullPointerException("Email cannot be empty");
        }
        
        // Handle null phone number case
        if (phoneNumber == null) {
            throw new NullPointerException("Phone number cannot be null");
        }
        
        this.email = Email.of(email);
        this.name = name;
        this.phoneNumber = PhoneNumber.korean(phoneNumber);
        
        // Publish domain event after construction
        if (eventPublisher != null) {
            eventPublisher.publish(new MemberEvents.MemberCreated(this));
        }
    }

    public void addOrder(Order order) {
        this.orders.add(order);
        order.setMember(this);
    }

    public void deactivate() {
        this.status = MemberStatus.INACTIVE;
        
        // Publish domain event
        if (eventPublisher != null) {
            eventPublisher.publish(new MemberEvents.MemberDeactivated(this));
        }
    }

    public void activate() {
        this.status = MemberStatus.ACTIVE;
        
        // Publish domain event
        if (eventPublisher != null) {
            eventPublisher.publish(new MemberEvents.MemberActivated(this));
        }
    }

    public void updateInfo(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = PhoneNumber.korean(phoneNumber);
    }
    
    /**
     * Gets the email as a string for backward compatibility.
     */
    public String getEmail() {
        return email != null ? email.toString() : null;
    }
    
    /**
     * Gets the phone number as a string for backward compatibility.
     */
    public String getPhoneNumber() {
        return phoneNumber != null ? phoneNumber.toString() : null;
    }

    public enum MemberStatus {
        ACTIVE, INACTIVE
    }
}