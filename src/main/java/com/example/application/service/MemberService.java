package com.example.application.service;

import com.example.application.command.CreateMemberCommand;
import com.example.application.query.MemberQuery;
import com.example.application.usecase.MemberUseCase;
import com.example.domain.Member;
import com.example.dto.MemberDto;
import com.example.infrastructure.validation.MemberValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberService {

    private final MemberUseCase memberUseCase;
    private final MemberQuery memberQuery;
    private final MemberValidator memberValidator;

    @Transactional
    public MemberDto createMember(String email, String name, String phoneNumber) {
        log.info("Creating member with email: {}", email);
        
        // Validate input
        memberValidator.validateMemberCreation(email, name, phoneNumber);
        
        // Execute use case
        Member member = memberUseCase.createMember(email, name, phoneNumber);
        
        return MemberDto.from(member);
    }

    @Transactional
    public MemberDto createMember(CreateMemberCommand command) {
        log.info("Creating member with command: {}", command.getEmail());
        
        // Validate input
        memberValidator.validateMemberCreation(command.getEmail(), command.getName(), command.getPhoneNumber());
        
        // Execute use case
        Member member = memberUseCase.createMember(command.getEmail(), command.getName(), command.getPhoneNumber());
        
        return MemberDto.from(member);
    }

    public Optional<MemberDto> getMemberById(Long id) {
        log.info("Retrieving member with ID: {}", id);
        memberValidator.validateMemberId(id);
        return memberQuery.findById(id)
                .map(MemberDto::from);
    }

    public Optional<MemberDto> getMemberByEmail(String email) {
        log.info("Retrieving member with email: {}", email);
        return memberQuery.findByEmail(email)
                .map(MemberDto::from);
    }

    public List<MemberDto> getAllMembers() {
        log.info("Retrieving all members");
        return memberQuery.findAll().stream()
                .map(MemberDto::from)
                .toList();
    }

    public List<MemberDto> getMembersByName(String name) {
        log.info("Searching members by name: {}", name);
        return memberQuery.findByNameContaining(name).stream()
                .map(MemberDto::from)
                .toList();
    }

    public List<MemberDto> getMembersByStatus(Member.MemberStatus status) {
        log.info("Retrieving members by status: {}", status);
        return (status == Member.MemberStatus.ACTIVE ? 
                memberQuery.findActiveMembers() : 
                memberQuery.findInactiveMembers()).stream()
                .map(MemberDto::from)
                .toList();
    }

    @Transactional
    public MemberDto deactivateMember(Long id) {
        log.info("Deactivating member with ID: {}", id);
        memberValidator.validateMemberId(id);
        
        Member member = memberUseCase.deactivateMember(id);
        return MemberDto.from(member);
    }

    @Transactional
    public MemberDto activateMember(Long id) {
        log.info("Activating member with ID: {}", id);
        memberValidator.validateMemberId(id);
        
        Member member = memberUseCase.activateMember(id);
        return MemberDto.from(member);
    }
}