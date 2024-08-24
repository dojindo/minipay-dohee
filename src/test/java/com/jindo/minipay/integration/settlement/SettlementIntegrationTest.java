package com.jindo.minipay.integration.settlement;

import com.jindo.minipay.integration.BaseIntegrationTest;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.settlements.dto.SettleAccountsRequest;
import com.jindo.minipay.settlements.dto.SettleCalculateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("정산 통합테스트")
public class SettlementIntegrationTest extends BaseIntegrationTest {
    static final String URL = "/api/v1/settlements";

    @Test
    @DisplayName("[더치패이] 정산 금액을 계산한다.")
    void settleCalculate_dutchPay() {
        // given
        Member member = saveMember();

        SettleCalculateRequest request = SettleCalculateRequest.builder()
                .settlementType("DUTCH_PAY")
                .totalAmount(35000L)
                .numOfParticipants(3)
                .requesterId(member.getId())
                .build();

        // when
        // then
        given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post(URL + "/calculate")
                .then()
                .statusCode(HttpStatus.OK.value())
                .assertThat()
                .body("settlementType", equalTo("DUTCH_PAY"))
                .body("numOfParticipants", equalTo(3))
                .body("totalAmount", equalTo(35000))
                .body("requestAmounts[0]", is(11666))
                .body("requestAmounts[1]", is(11666))
                .body("requestAmounts[2]", is(11666))
                .body("remainingAmount", equalTo(2))
                .log().all();
    }

    @Test
    @DisplayName("[랜덤] 정산 금액을 계산한다.")
    void settleCalculate_random() {
        // given
        Member member = saveMember();

        SettleCalculateRequest request = SettleCalculateRequest.builder()
                .settlementType("RANDOM")
                .totalAmount(35000L)
                .numOfParticipants(3)
                .requesterId(member.getId())
                .build();

        // when
        // then
        given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post(URL + "/calculate")
                .then()
                .statusCode(HttpStatus.OK.value())
                .assertThat()
                .body("settlementType", equalTo("RANDOM"))
                .body("numOfParticipants", equalTo(3))
                .body("totalAmount", equalTo(35000))
                .body("requestAmounts", hasSize(3))
                .body("remainingAmount", equalTo(0))
                .log().all();
    }

    @Test
    @DisplayName("정산을 요청한다.")
    void settleAccounts() {
        // given
        Member member = saveMember();

        List<Member> participants = saveParticipants();

        SettleAccountsRequest request = SettleAccountsRequest.builder()
                .settlementType("RANDOM")
                .totalAmount(35000L)
                .numOfParticipants(2)
                .requesterId(member.getId())
                .participants(List.of(
                        new SettleAccountsRequest
                                .ParticipantRequest(participants.get(0).getId(),
                                10000L),
                        new SettleAccountsRequest
                                .ParticipantRequest(participants.get(1).getId(),
                                25000L)))
                .remainingAmount(0)
                .build();

        // when
        // then
        given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post(URL)
                .then()
                .statusCode(HttpStatus.OK.value())
                .assertThat()
                .body("settlementType", equalTo("RANDOM"))
                .body("totalAmount", equalTo(35000))
                .body("numOfParticipants", equalTo(2))
                .body("participants", hasSize(2))
                .body("remainingAmount", equalTo(0))
                .body("settlementStatus", equalTo("WAITING"))
                .log().all();
    }

    List<Member> saveParticipants() {
        List<Member> partsMembers = new ArrayList<>();

        Member participant1 = Member.builder()
                .email("part1@test.com")
                .password("test12345")
                .name("part1")
                .build();

        Member participant2 = Member.builder()
                .email("part2@test.com")
                .password("test12345")
                .name("part2")
                .build();

        partsMembers.add(memberRepository.save(participant1));
        partsMembers.add(memberRepository.save(participant2));

        return partsMembers;
    }
}
