package com.example.domain.specification;

import com.example.domain.Member;
import com.example.domain.valueobject.Email;

/**
 * Specifications for Member domain objects.
 * Encapsulates business rules related to member validation and filtering.
 */
public class MemberSpecifications {
    
    /**
     * Specification for active members.
     */
    public static Specification<Member> isActive() {
        return new Specification<Member>() {
            @Override
            public boolean isSatisfiedBy(Member member) {
                return member.getStatus() == Member.MemberStatus.ACTIVE;
            }
        };
    }
    
    /**
     * Specification for inactive members.
     */
    public static Specification<Member> isInactive() {
        return new Specification<Member>() {
            @Override
            public boolean isSatisfiedBy(Member member) {
                return member.getStatus() == Member.MemberStatus.INACTIVE;
            }
        };
    }
    
    /**
     * Specification for members with a specific email domain.
     */
    public static Specification<Member> hasEmailDomain(String domain) {
        return new Specification<Member>() {
            @Override
            public boolean isSatisfiedBy(Member member) {
                try {
                    Email email = Email.of(member.getEmail());
                    return email.hasDomain(domain);
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        };
    }
    
    /**
     * Specification for members with a specific name pattern.
     */
    public static Specification<Member> hasNameContaining(String namePattern) {
        return new Specification<Member>() {
            @Override
            public boolean isSatisfiedBy(Member member) {
                return member.getName().toLowerCase().contains(namePattern.toLowerCase());
            }
        };
    }
    
    /**
     * Specification for members with Korean phone numbers.
     */
    public static Specification<Member> hasKoreanPhoneNumber() {
        return new Specification<Member>() {
            @Override
            public boolean isSatisfiedBy(Member member) {
                try {
                    com.example.domain.valueobject.PhoneNumber phoneNumber = 
                        com.example.domain.valueobject.PhoneNumber.korean(member.getPhoneNumber());
                    return phoneNumber.isKorean();
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        };
    }
    
    /**
     * Specification for members with orders.
     */
    public static Specification<Member> hasOrders() {
        return new Specification<Member>() {
            @Override
            public boolean isSatisfiedBy(Member member) {
                return member.getOrders() != null && !member.getOrders().isEmpty();
            }
        };
    }
    
    /**
     * Specification for members with a minimum number of orders.
     */
    public static Specification<Member> hasMinimumOrders(int minimumOrders) {
        return new Specification<Member>() {
            @Override
            public boolean isSatisfiedBy(Member member) {
                return member.getOrders() != null && 
                       member.getOrders().size() >= minimumOrders;
            }
        };
    }
    
    /**
     * Specification for premium members (active with multiple orders).
     */
    public static Specification<Member> isPremiumMember() {
        return isActive().and(hasMinimumOrders(3));
    }
    
    /**
     * Specification for corporate members (email domain based).
     */
    public static Specification<Member> isCorporateMember() {
        return isActive().and(
            hasEmailDomain("company.com")
                .or(hasEmailDomain("corp.com"))
                .or(hasEmailDomain("enterprise.com"))
        );
    }
}