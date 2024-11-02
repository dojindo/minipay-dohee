package com.jindo.minipay.integration.account;

import com.jindo.minipay.account.checking.dto.ChargeRequest;
import com.jindo.minipay.integration.BaseIntegrationTest;
import com.jindo.minipay.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("메인 계좌 통합테스트")
class CheckingAccountIntegrationTest extends BaseIntegrationTest {
    static final String URL = "/api/v1/account/checking";

    @Test
    @DisplayName("메인 계좌에 충전한다.")
    void charge() {
        // given
        Member member = saveMember();
        saveCheckingAccount(member);

        String accountNumber = member.getAccountNumber();
        ChargeRequest request = new ChargeRequest(accountNumber, 10000L);

        // when
        // then
        given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post(URL + "/charge")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("accountNumber", equalTo(accountNumber))
                .body("balance", equalTo(10000))
                .log().all();
    }
}
