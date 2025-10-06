package com.example.infrastructure.web;

import com.example.application.service.MemberService;
import com.example.domain.Member;
import com.example.dto.MemberDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Member Management", description = "APIs for managing members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @Operation(summary = "Create a new member", description = "Creates a new member with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Member created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Member with email already exists")
    })
    public ResponseEntity<MemberDto> createMember(@Valid @RequestBody CreateMemberRequest request) {
        log.info("Creating member with email: {}", request.getEmail());
        MemberDto member = memberService.createMember(
                request.getEmail(),
                request.getName(),
                request.getPhoneNumber()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get member by ID", description = "Retrieves a member by their unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member found"),
            @ApiResponse(responseCode = "404", description = "Member not found")
    })
    public ResponseEntity<MemberDto> getMemberById(
            @Parameter(description = "Member ID") @PathVariable Long id) {
        log.info("Retrieving member with ID: {}", id);
        return memberService.getMemberById(id)
                .map(member -> ResponseEntity.ok(member))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<MemberDto> getMemberByEmail(@PathVariable String email) {
        log.info("Retrieving member with email: {}", email);
        return memberService.getMemberByEmail(email)
                .map(member -> ResponseEntity.ok(member))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<MemberDto>> getAllMembers() {
        log.info("Retrieving all members");
        List<MemberDto> members = memberService.getAllMembers();
        return ResponseEntity.ok(members);
    }

    @GetMapping("/search")
    public ResponseEntity<List<MemberDto>> searchMembersByName(@RequestParam String name) {
        log.info("Searching members by name: {}", name);
        List<MemberDto> members = memberService.getMembersByName(name);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<MemberDto>> getMembersByStatus(@PathVariable Member.MemberStatus status) {
        log.info("Retrieving members by status: {}", status);
        List<MemberDto> members = memberService.getMembersByStatus(status);
        return ResponseEntity.ok(members);
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<MemberDto> deactivateMember(@PathVariable Long id) {
        log.info("Deactivating member with ID: {}", id);
        MemberDto member = memberService.deactivateMember(id);
        return ResponseEntity.ok(member);
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<MemberDto> activateMember(@PathVariable Long id) {
        log.info("Activating member with ID: {}", id);
        MemberDto member = memberService.activateMember(id);
        return ResponseEntity.ok(member);
    }

    @Data
    public static class CreateMemberRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$", message = "Phone number must be in format XXX-XXXX-XXXX")
        private String phoneNumber;
    }
}