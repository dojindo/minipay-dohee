package com.jindo.minipay.integration.transaction;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.integration.BaseIntegrationTest;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.setting.entity.Setting;
import com.jindo.minipay.transaction.dto.RemitRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionIntegrationTest extends BaseIntegrationTest {
    static final String URL = "/api/v1/transactions";

    @Test
    @DisplayName("메인 계좌에서 친구 계좌로 송금한다. - 즉시 송금")
    void remit_immediately() {
        // given
        Member sender = saveMember();
        saveCheckingAccount(sender);

        Member receiver = saveFriendAndAccount();

        String senderAccountNumber = sender.getAccountNumber();
        String receiverAccountNumber = receiver.getAccountNumber();

        RemitRequest request = RemitRequest.builder()
                .senderAccountNumber(senderAccountNumber)
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
                .body("accountNumber", equalTo(senderAccountNumber))
                .body("balance", equalTo(0))
                .log().all();

        // then
        Optional<CheckingAccount> senderAccount =
                checkingAccountRepository.findByAccountNumber(senderAccountNumber);

        Optional<CheckingAccount> receiverAccount =
                checkingAccountRepository.findByAccountNumber(receiverAccountNumber);

        assertTrue(senderAccount.isPresent());
        assertEquals(0L, senderAccount.get().getBalance());
        assertTrue(receiverAccount.isPresent());
        assertEquals(10000L, receiverAccount.get().getBalance());
    }

    @Test
    @DisplayName("메인 계좌에서 친구 계좌로 송금한다. - 대기 송금")
    void remit_pending() {
        // given
        Member sender = saveMember();
        saveCheckingAccount(sender);

        Optional<Setting> setting = settingRepository.findByMember(sender);
        setting.ifPresent(o -> {
            o.changeRemitTypeToPending();
            settingRepository.save(o);
        });

        Member receiver = saveFriendAndAccount();

        String senderAccountNumber = sender.getAccountNumber();
        String receiverAccountNumber = receiver.getAccountNumber();

        RemitRequest request = RemitRequest.builder()
                .senderAccountNumber(senderAccountNumber)
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
                .body("accountNumber", equalTo(senderAccountNumber))
                .body("balance", equalTo(0))
                .log().all();

        // then
        Optional<CheckingAccount> senderAccount =
                checkingAccountRepository.findByAccountNumber(senderAccountNumber);

        Optional<CheckingAccount> receiverAccount =
                checkingAccountRepository.findByAccountNumber(receiverAccountNumber);

        assertTrue(senderAccount.isPresent());
        assertEquals(0L, senderAccount.get().getBalance());
        assertTrue(receiverAccount.isPresent());
        assertEquals(0L, receiverAccount.get().getBalance());
    }

    private Member saveFriendAndAccount() {
        Member member = Member.builder()
                .email("friend@test.com")
                .password("test12345")
                .name("tester2")
                .build();
        memberRepository.save(member);

        CheckingAccount account =
                CheckingAccount.of("8888-02-7654321", member);
        checkingAccountRepository.save(account);

        memberRepository.save(member);
        return member;
    }
}
