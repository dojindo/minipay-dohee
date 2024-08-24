package com.jindo.minipay.integration.account;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.saving.dto.CreateSavingAccountRequest;
import com.jindo.minipay.account.saving.dto.PayInRequest;
import com.jindo.minipay.account.saving.entity.SavingAccount;
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

@DisplayName("적금 계좌 통합테스트")
public class SavingAccountIntegrationTest extends BaseIntegrationTest {
    static final String URL = "/api/v1/account/saving";

    @Test
    @DisplayName("적금 계좌를 생성한다.")
    void create() {
        // given
        Member member = saveMember();

        CreateSavingAccountRequest request =
                new CreateSavingAccountRequest(member.getId());

        // when
        given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post(URL)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .log().all();

        // then
        assertTrue(savingAccountRepository.existsByMember(member));
    }

    @Test
    @DisplayName("적금 계좌에 납입한다.")
    void payIn() {
        // given
        Member member = saveMember();
        saveSavingAccountAndCheckingAccount(member);

        String savingAccountNumber = "8800-01-1234567";
        String checkingAccountNumber = "8888-01-1234567";

        PayInRequest request = new PayInRequest(savingAccountNumber,
                checkingAccountNumber, 10000L);

        // when
        given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post(URL + "/payin")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("accountNumber", equalTo(savingAccountNumber))
                .body("amount", equalTo(10000))
                .log().all();

        // then
        Optional<SavingAccount> savingAccount =
                savingAccountRepository.findByAccountNumber(savingAccountNumber);

        assertTrue(savingAccount.isPresent());
        assertEquals(10000, savingAccount.get().getAmount());
    }

    void saveSavingAccountAndCheckingAccount(Member member) {
        SavingAccount savingAccount =
                SavingAccount.of("8800-01-1234567", member);
        savingAccountRepository.save(savingAccount);

        CheckingAccount checkingAccount =
                CheckingAccount.of("8888-01-1234567", member);
        checkingAccount.increaseBalance(10000L);
        checkingAccountRepository.save(checkingAccount);
    }
}
