package com.example.testdata;

import com.example.domain.Member;

import java.util.ArrayList;
import java.util.List;

public class MemberTestDataBuilder {

    private String email = "test@example.com";
    private String name = "Test User";
    private String phoneNumber = "010-1234-5678";
    private Member.MemberStatus status = Member.MemberStatus.ACTIVE;

    public static MemberTestDataBuilder aMember() {
        return new MemberTestDataBuilder();
    }

    public MemberTestDataBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public MemberTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public MemberTestDataBuilder withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public MemberTestDataBuilder withStatus(Member.MemberStatus status) {
        this.status = status;
        return this;
    }

    public MemberTestDataBuilder active() {
        this.status = Member.MemberStatus.ACTIVE;
        return this;
    }

    public MemberTestDataBuilder inactive() {
        this.status = Member.MemberStatus.INACTIVE;
        return this;
    }

    public Member build() {
        Member member = Member.builder()
                .email(email)
                .name(name)
                .phoneNumber(phoneNumber)
                .build();
        
        if (status == Member.MemberStatus.INACTIVE) {
            member.deactivate();
        }
        
        return member;
    }

    public static List<Member> createMultipleMembers(int count) {
        List<Member> members = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            members.add(aMember()
                    .withEmail("test" + i + "@example.com")
                    .withName("Test User " + i)
                    .withPhoneNumber("010-" + String.format("%04d", i) + "-" + String.format("%04d", i))
                    .build());
        }
        return members;
    }

    public static Member createActiveMember() {
        return aMember().active().build();
    }

    public static Member createInactiveMember() {
        return aMember().inactive().build();
    }

    public static Member createMemberWithEmail(String email) {
        return aMember().withEmail(email).build();
    }

    public static Member createMemberWithName(String name) {
        return aMember().withName(name).build();
    }
}