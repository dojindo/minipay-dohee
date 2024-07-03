package com.jindo.minipay.account;

import com.jindo.minipay.account.checking.dto.ChargeRequest;
import com.jindo.minipay.account.checking.dto.RemitRequest;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.checking.service.CheckingAccountService;
import com.jindo.minipay.account.checking.service.RemitService;
import com.jindo.minipay.account.common.exception.AccountException;
import com.jindo.minipay.account.saving.dto.PayInRequest;
import com.jindo.minipay.account.saving.entity.SavingAccount;
import com.jindo.minipay.account.saving.repository.SavingAccountRepository;
import com.jindo.minipay.account.saving.service.SavingAccountService;
import com.jindo.minipay.lock.exception.LockException;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
class AccountConcurrencyTest {
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

    @Autowired
    RemitService remitService;

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
                    } catch (LockException e) {
                        System.out.println(e.getMessage());
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

    @Nested
    @DisplayName("송금하기")
    class RemitMethod {
        String myAccountNumber = "8888-01-1234567";
        String receiverAccountNumber = "8888-02-7654321";

        @Test
        @DisplayName("동일한 친구에게 송금하는 요청이 동시에 올 경우 차례로 실행된다.")
        void concurrency_remit() throws InterruptedException {
            // given
            int threadCount = 100;

            Member receiver = memberRepository.save(Member.builder()
                    .email("test@test.com")
                    .password("test12345")
                    .name("tester1")
                    .build());

            checkingAccountRepository.save(
                    CheckingAccount.of(receiverAccountNumber, receiver));

            String[] accountNumbers = new String[threadCount];

            for (int i = 0; i < threadCount; i++) {
                Member member = memberRepository.save(Member.builder()
                        .email("test" + i + "@test.com")
                        .password("test12345")
                        .name("tester")
                        .build());

                String accountNumber = "8888-" + i + "-1234567";

                checkingAccountRepository.save(
                        CheckingAccount.of(accountNumber, member));

                accountNumbers[i] = accountNumber;
            }

            // when
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch countDownLatch = new CountDownLatch(threadCount);

            IntStream.range(0, threadCount).forEach(i ->
                    executorService.submit(() -> {
                        try {
                            remitService.remit(RemitRequest.builder()
                                    .myAccountNumber(accountNumbers[i])
                                    .receiverAccountNumber(receiverAccountNumber)
                                    .amount(10000L)
                                    .build());
                        } catch (AccountException | LockException e) {
                            System.out.println("exception is occurred" + e.getMessage());
                        } finally {
                            countDownLatch.countDown();
                        }
                    }));

            countDownLatch.await();
            executorService.shutdown();

            // then
            CheckingAccount receiverCheckingAccount =
                    checkingAccountRepository.findByAccountNumber(receiverAccountNumber).get();

            assertEquals(10000 * threadCount, receiverCheckingAccount.getBalance());
        }

        @Test
        @DisplayName("내가 친구에게 송금하는 요청과 친구가 나에게 송금하는 요청이 동시에 올 경우 모두 실패한다.")
        void concurrency_remit_deadlock() throws InterruptedException {
            // given
            Member me = memberRepository.save(Member.builder()
                    .email("test@test.com")
                    .password("test12345")
                    .name("tester1")
                    .build());

            Member receiver = memberRepository.save(Member.builder()
                    .email("test2@test.com")
                    .password("test12345")
                    .name("tester1")
                    .build());

            checkingAccountRepository.save(
                    CheckingAccount.of(myAccountNumber, me));

            checkingAccountRepository.save(
                    CheckingAccount.of(receiverAccountNumber, receiver));

            RemitRequest request = RemitRequest.builder()
                    .myAccountNumber(myAccountNumber)
                    .receiverAccountNumber(receiverAccountNumber)
                    .amount(10000L)
                    .build();

            RemitRequest request2 = RemitRequest.builder()
                    .myAccountNumber(receiverAccountNumber)
                    .receiverAccountNumber(myAccountNumber)
                    .amount(10000L)
                    .build();

            // when
            AtomicReference<Exception> exceptions = new AtomicReference<>();
            AtomicInteger exceptionCnt = new AtomicInteger();

            Thread thread = new Thread(() -> {
                try {
                    remitService.remit(request);
                } catch (LockException e) {
                    exceptions.set(e);
                    exceptionCnt.getAndIncrement();
                }
            });

            Thread thread2 = new Thread(() -> {
                try {
                    remitService.remit(request2);
                } catch (LockException e) {
                    exceptions.set(e);
                    exceptionCnt.getAndIncrement();
                }
            });

            thread.start();
            thread2.start();

            thread.join();
            thread2.join();

            // then
            CheckingAccount myCheckingAccount =
                    checkingAccountRepository.findByAccountNumber(myAccountNumber).get();

            CheckingAccount receiverCheckingAccount =
                    checkingAccountRepository.findByAccountNumber(receiverAccountNumber).get();

            assertEquals(0, myCheckingAccount.getBalance());
            assertEquals(0, receiverCheckingAccount.getBalance());
            assertInstanceOf(LockException.class, exceptions.get());
            assertEquals(2, exceptionCnt.get());
        }
    }
}
