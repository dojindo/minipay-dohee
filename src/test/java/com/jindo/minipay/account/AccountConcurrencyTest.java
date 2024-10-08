package com.jindo.minipay.account;

import com.jindo.minipay.account.checking.dto.ChargeRequest;
import com.jindo.minipay.account.checking.dto.RemitRequest;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.service.CheckingAccountService;
import com.jindo.minipay.account.common.exception.AccountException;
import com.jindo.minipay.account.saving.dto.PayInRequest;
import com.jindo.minipay.account.saving.entity.SavingAccount;
import com.jindo.minipay.account.saving.service.SavingAccountService;
import com.jindo.minipay.integration.BaseIntegrationTest;
import com.jindo.minipay.lock.exception.LockException;
import com.jindo.minipay.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountConcurrencyTest extends BaseIntegrationTest {
    @Autowired
    SavingAccountService savingAccountService;

    @Autowired
    CheckingAccountService checkingAccountService;

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
        CheckingAccount checkingAccount = checkingAccountRepository.save(
                CheckingAccount.of(checkingAccountNumber, member));

        // 적금 계좌
        SavingAccount savingAccount = savingAccountRepository.save(
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
        Optional<CheckingAccount> findCheckingAccount =
                checkingAccountRepository.findById(checkingAccount.getId());

        Optional<SavingAccount> findSavingAccount =
                savingAccountRepository.findById(savingAccount.getId());

        assertTrue(findCheckingAccount.isPresent());
        assertTrue(findSavingAccount.isPresent());

        assertEquals(0, findCheckingAccount.get().getBalance());
        assertEquals(threadCount * amount, findSavingAccount.get().getAmount());
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
                            checkingAccountService.remit(RemitRequest.builder()
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
        @DisplayName("내가 친구에게 송금하는 요청과 친구가 나에게 송금하는 요청이 동시에 올 경우 차례로 실행된다.")
        void concurrency_remit_deadlock() throws InterruptedException {
            // given
            setMeAndReceiver();

            // 내 계좌 충전
            checkingAccountService.charge(
                    new ChargeRequest(myAccountNumber, 10000L));

            // 친구 계좌 충전
            checkingAccountService.charge(
                    new ChargeRequest(receiverAccountNumber, 10000L));

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
            AtomicInteger exceptionCnt = new AtomicInteger();

            Thread thread = new Thread(() -> {
                try {
                    checkingAccountService.remit(request);
                } catch (LockException e) {
                    exceptionCnt.getAndIncrement();
                }
            });

            Thread thread2 = new Thread(() -> {
                try {
                    checkingAccountService.remit(request2);
                } catch (LockException e) {
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

            assertEquals(10000L, myCheckingAccount.getBalance());
            assertEquals(10000L, receiverCheckingAccount.getBalance());
            assertEquals(0, exceptionCnt.get());
        }

        @Test
        @DisplayName("친구에게 송금 요청과 친구가 충전하는 요청이 동시에 올 경우 차례로 실행된다.")
        void concurrency_remit_charge() throws InterruptedException {
            // given
            setMeAndReceiver();

            RemitRequest remitRequest = RemitRequest.builder()
                    .myAccountNumber(myAccountNumber)
                    .receiverAccountNumber(receiverAccountNumber)
                    .amount(10000L)
                    .build();

            ChargeRequest chargeRequest =
                    new ChargeRequest(receiverAccountNumber, 10000L);

            // when
            Thread thread = new Thread(() ->
                    checkingAccountService.remit(remitRequest));

            Thread thread2 = new Thread(() ->
                    checkingAccountService.charge(chargeRequest));

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
            assertEquals(20000, receiverCheckingAccount.getBalance());
        }

        @Test
        @DisplayName("친구에게 송금 요청과 친구가 적금 납입하는 요청이 동시에 올 경우 차례로 실행된다.")
        void concurrency_remit_payIn() throws InterruptedException {
            // given
            Member receiver = setMeAndReceiver();
            String savingAccountNumber = "8800-09-7654321";

            savingAccountRepository.save(
                    SavingAccount.of(savingAccountNumber, receiver));

            // 친구 계좌 충전
            checkingAccountService.charge(
                    new ChargeRequest(receiverAccountNumber, 10000L));

            RemitRequest remitRequest = RemitRequest.builder()
                    .myAccountNumber(myAccountNumber)
                    .receiverAccountNumber(receiverAccountNumber)
                    .amount(10000L)
                    .build();

            PayInRequest payInRequest = PayInRequest.builder()
                    .savingAccountNumber(savingAccountNumber)
                    .checkingAccountNumber(receiverAccountNumber)
                    .amount(10000L)
                    .build();

            // when
            Thread thread = new Thread(() ->
                    checkingAccountService.remit(remitRequest));

            Thread thread2 = new Thread(() ->
                    savingAccountService.payIn(payInRequest));

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
            assertEquals(10000, receiverCheckingAccount.getBalance());
        }

        private Member setMeAndReceiver() {
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

            return receiver;
        }
    }
}
