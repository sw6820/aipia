package com.example;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * 전체 테스트 스위트를 실행하는 클래스
 * 
 * 이 클래스는 프로젝트의 모든 테스트를 체계적으로 실행하기 위해 사용됩니다.
 * TDD 원칙에 따라 다음과 같은 테스트 카테고리들을 포함합니다:
 * 
 * 1. Unit Tests (단위 테스트)
 *    - Domain Entity Tests
 *    - Service Layer Tests
 * 
 * 2. Integration Tests (통합 테스트)
 *    - Repository Tests
 *    - Controller Tests
 *    - Complete Workflow Tests
 * 
 * 3. Performance Tests (성능 테스트)
 *    - Bulk Data Processing
 *    - Query Performance
 *    - Concurrency Tests
 * 
 * 실행 방법:
 * - IDE에서 이 클래스를 우클릭하여 "Run TestSuite" 선택
 * - 또는 명령줄에서: ./gradlew test
 */
@Suite
@SuiteDisplayName("Member-Order-Payment System Test Suite")
@SelectPackages({
    "com.example.domain",           // Domain entity tests
    "com.example.service",          // Service layer tests
    "com.example.repository",       // Repository tests
    "com.example.controller",       // Controller tests
    "com.example.dto",             // DTO tests
    "com.example.integration",      // Integration tests
    "com.example.performance",      // Performance tests
    "com.example.testdata"          // Test data builders
})
public class TestSuite {
    // 테스트 스위트 실행을 위한 빈 클래스
    // JUnit 5의 @Suite 어노테이션이 모든 테스트를 실행합니다
}