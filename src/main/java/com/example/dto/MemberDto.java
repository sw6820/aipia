package com.example.dto;

import com.example.domain.Member;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MemberDto {
    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private Member.MemberStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderDto> orders;

    // Setters for testing
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public static MemberDto from(Member member) {
        return MemberDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .phoneNumber(member.getPhoneNumber())
                .status(member.getStatus())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .orders(member.getOrders().stream()
                        .map(OrderDto::from)
                        .toList())
                .build();
    }
}