package com.jindo.minipay.integration.account;

import com.jindo.minipay.account.checking.dto.ChargeRequest;
import com.jindo.minipay.account.checking.dto.RemitRequest;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.integration.BaseIntegrationTest;
import com.jindo.minipay.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("메인 계좌 통합테스트")
public class CheckingAccountIntegrationTest extends BaseIntegrationTest {
    static final String URL = "/api/v1/account/checking";

    @Test
    @DisplayName("메인 계좌에 충전한다.")
    void charge() {
        // given
        Member member = saveMember();
        saveCheckingAccount(member);

        String accountNumber = "8888-01-1234567";
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

    @Test
    @DisplayName("메인 계좌에서 친구 계좌로 송금한다.")
    void remit() {
        // given
        Member member = saveMember();
        saveCheckingAccount(member);

        saveFriendAndAccount();

        String myAccountNumber = "8888-01-1234567";
        String receiverAccountNumber = "8888-02-7654321";

        RemitRequest request = RemitRequest.builder()
                .myAccountNumber(myAccountNumber)
                .receiverAccountNumber(receiverAccountNumber)
                .amount(10000L)
                .build();

        // when
        given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post(URL + "/remit")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("accountNumber", equalTo(myAccountNumber))
                .body("balance", equalTo(0))
                .log().all();

        // then
        Optional<CheckingAccount> receiverAccount =
                checkingAccountRepository.findByAccountNumber(receiverAccountNumber);

        assertTrue(receiverAccount.isPresent());
        assertEquals(10000L, receiverAccount.get().getBalance());
    }

    void saveFriendAndAccount() {
        Member member = Member.builder()
                .email("friend@test.com")
                .password("test12345")
                .name("tester2")
                .build();
        memberRepository.save(member);

        CheckingAccount account =
                CheckingAccount.of("8888-02-7654321", member);
        checkingAccountRepository.save(account);
    }
}
