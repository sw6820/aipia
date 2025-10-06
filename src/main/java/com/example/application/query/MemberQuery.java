package com.example.application.query;

import com.example.domain.Member;
import com.example.infrastructure.persistence.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Query handler for member read operations.
 * Implements CQRS pattern for read operations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MemberQuery {

    private final MemberRepository memberRepository;

    /**
     * Finds a member by ID.
     */
    public Optional<Member> findById(Long id) {
        log.info("Querying member by ID: {}", id);
        return memberRepository.findById(id);
    }

    /**
     * Finds a member by email.
     */
    public Optional<Member> findByEmail(String email) {
        log.info("Querying member by email: {}", email);
        return memberRepository.findByEmail(email);
    }

    /**
     * Checks if a member exists by email.
     */
    public boolean existsByEmail(String email) {
        log.info("Checking if member exists by email: {}", email);
        return memberRepository.existsByEmail(email);
    }

    /**
     * Finds all members.
     */
    public List<Member> findAll() {
        log.info("Querying all members");
        return memberRepository.findAll();
    }

    /**
     * Finds members by name containing the given text.
     */
    public List<Member> findByNameContaining(String name) {
        log.info("Querying members by name containing: {}", name);
        return memberRepository.findByNameContaining(name);
    }

    /**
     * Finds active members.
     */
    public List<Member> findActiveMembers() {
        log.info("Querying active members");
        return memberRepository.findByStatus(Member.MemberStatus.ACTIVE);
    }

    /**
     * Finds inactive members.
     */
    public List<Member> findInactiveMembers() {
        log.info("Querying inactive members");
        return memberRepository.findByStatus(Member.MemberStatus.INACTIVE);
    }
}