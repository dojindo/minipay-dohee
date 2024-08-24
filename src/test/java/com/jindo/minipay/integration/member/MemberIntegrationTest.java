package com.jindo.minipay.integration.member;

import com.jindo.minipay.integration.BaseIntegrationTest;
import com.jindo.minipay.member.dto.RegisterRequest;
import com.jindo.minipay.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("회원 통합테스트")
public class MemberIntegrationTest extends BaseIntegrationTest {
    static final String URL = "/api/v1/members";

    @Test
    @DisplayName("회원을 등록한다.")
    void register() {
        // given
        String email = "test@test.com";

        RegisterRequest request = RegisterRequest.builder()
                .email(email)
                .password("test12345")
                .name("tester1")
                .build();

        // when
        given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().post(URL)
                .then().log().all();

        // then
        Optional<Member> memberOptional = memberRepository.findByEmail(email);

        assertTrue(memberOptional.isPresent());
        assertTrue(checkingAccountRepository.existsByMember(memberOptional.get()));
    }
}
