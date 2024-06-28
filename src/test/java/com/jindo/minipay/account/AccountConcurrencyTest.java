package com.jindo.minipay.account;

import com.jindo.minipay.account.checking.dto.ChargeRequest;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.checking.service.CheckingAccountService;
import com.jindo.minipay.account.saving.dto.PayInRequest;
import com.jindo.minipay.account.saving.entity.SavingAccount;
import com.jindo.minipay.account.saving.repository.SavingAccountRepository;
import com.jindo.minipay.account.saving.service.SavingAccountService;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class AccountConcurrencyTest {
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    SavingAccountService savingAccountService;

    @Autowired
    SavingAccountRepository savingAccountRepository;

    @Autowired
    CheckingAccountService checkingAccountService;

    @Autowired
    CheckingAccountRepository checkingAccountRepository;

    @Test
    @DisplayName("적금 계좌 납입과 메인 계좌 충전 요청이 동시에 올 경우 차례로 실행된다.")
    void concurrency_payIn_charge() throws InterruptedException {
        // given
        Member member = memberRepository.save(Member.builder()
                .email("test@test.com")
                .password("test12345")
                .name("tester1")
                .build());

        String checkingAccountNumber = "8888-01-1234567";
        String savingAccountNumber = "8800-09-7654321";
        long amount = 10000L;

        // 메인 계좌
        checkingAccountRepository.save(
                CheckingAccount.of(checkingAccountNumber, member));

        // 적금 계좌
        savingAccountRepository.save(
                SavingAccount.of(savingAccountNumber, member));

        // request
        ChargeRequest chargeRequest =
                new ChargeRequest(checkingAccountNumber, amount);

        PayInRequest payInRequest = PayInRequest.builder()
                .savingAccountNumber(savingAccountNumber)
                .checkingAccountNumber(checkingAccountNumber)
                .amount(amount)
                .build();

        // when
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        IntStream.range(0, threadCount).forEach(i ->
                executorService.submit(() -> {
                    try {
                        checkingAccountService.charge(chargeRequest);
                        savingAccountService.payIn(payInRequest);
                    } finally {
                        countDownLatch.countDown();
                    }
                }));

        countDownLatch.await();
        executorService.shutdown();

        // then
        CheckingAccount checkingAccount = checkingAccountRepository.findById(1L).get();
        SavingAccount savingAccount = savingAccountRepository.findById(1L).get();

        assertEquals(0, checkingAccount.getBalance());
        assertEquals(threadCount * amount, savingAccount.getAmount());
    }
}
