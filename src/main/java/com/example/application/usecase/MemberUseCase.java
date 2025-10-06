package com.example.application.usecase;

import com.example.domain.Member;
import com.example.infrastructure.persistence.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for member management operations.
 * Encapsulates business logic and orchestrates domain operations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MemberUseCase {

    private final MemberRepository memberRepository;

    /**
     * Creates a new member with validation and business rules.
     */
    @Transactional
    public Member createMember(String email, String name, String phoneNumber) {
        log.info("Creating member with email: {}", email);
        
        // Business rule: Check for duplicate email
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Member with email " + email + " already exists");
        }

        // Create domain entity with validation
        Member member = Member.builder()
                .email(email)
                .name(name)
                .phoneNumber(phoneNumber)
                .build();

        Member savedMember = memberRepository.save(member);
        log.info("Member created successfully with ID: {}", savedMember.getId());
        
        return savedMember;
    }

    /**
     * Activates a member account.
     */
    @Transactional
    public Member activateMember(Long memberId) {
        log.info("Activating member with ID: {}", memberId);
        
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));
        
        member.activate();
        Member savedMember = memberRepository.save(member);
        
        log.info("Member activated successfully with ID: {}", memberId);
        return savedMember;
    }

    /**
     * Deactivates a member account.
     */
    @Transactional
    public Member deactivateMember(Long memberId) {
        log.info("Deactivating member with ID: {}", memberId);
        
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));
        
        member.deactivate();
        Member savedMember = memberRepository.save(member);
        
        log.info("Member deactivated successfully with ID: {}", memberId);
        return savedMember;
    }

    /**
     * Updates member information.
     */
    @Transactional
    public Member updateMember(Long memberId, String name, String phoneNumber) {
        log.info("Updating member with ID: {}", memberId);
        
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));
        
        member.updateInfo(name, phoneNumber);
        Member savedMember = memberRepository.save(member);
        
        log.info("Member updated successfully with ID: {}", memberId);
        return savedMember;
    }
}