package com.jindo.minipay.integration;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.saving.repository.SavingAccountRepository;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import com.jindo.minipay.settlements.repository.SettlementParticipantRepository;
import com.jindo.minipay.settlements.repository.SettlementRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@Disabled
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {
    @LocalServerPort
    int port;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected CheckingAccountRepository checkingAccountRepository;

    @Autowired
    protected SavingAccountRepository savingAccountRepository;

    @Autowired
    protected SettlementRepository settlementRepository;

    @Autowired
    protected SettlementParticipantRepository settlementParticipantRepository;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
    }

    @AfterEach
    void teardown() {
        settlementParticipantRepository.deleteAllInBatch();
        settlementRepository.deleteAllInBatch();
        savingAccountRepository.deleteAllInBatch();
        checkingAccountRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    protected Member saveMember() {
        Member member = Member.builder()
                .email("test@test.com")
                .password("test12345")
                .name("tester1")
                .build();
        return memberRepository.save(member);
    }

    protected void saveCheckingAccount(Member member) {
        CheckingAccount account =
                CheckingAccount.of("8888-01-1234567", member);
        checkingAccountRepository.save(account);
    }
}
