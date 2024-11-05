package com.jindo.minipay.account;

import com.jindo.minipay.account.checking.dto.ChargeRequest;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.service.CheckingAccountService;
import com.jindo.minipay.account.common.exception.AccountException;
import com.jindo.minipay.account.saving.dto.PayInRequest;
import com.jindo.minipay.account.saving.entity.SavingAccount;
import com.jindo.minipay.account.saving.service.SavingAccountService;
import com.jindo.minipay.integration.BaseIntegrationTest;
import com.jindo.minipay.lock.exception.LockException;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.setting.entity.Setting;
import com.jindo.minipay.transaction.dto.RemitRequest;
import com.jindo.minipay.transaction.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountConcurrencyTest extends BaseIntegrationTest {
    @Autowired
    SavingAccountService savingAccountService;

    @Autowired
    CheckingAccountService checkingAccountService;

    @Autowired
    TransactionService transactionService;

    Logger logger = Logger.getLogger(AccountConcurrencyTest.class.getName());

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

        // 메인 계좌 미리 충전 (납입할 금액 만큼)
        checkingAccountService.charge(new ChargeRequest(checkingAccountNumber, 150_000L));

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
        int repeat = 30;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(repeat);

        IntStream.range(0, repeat).forEach(i ->
                executorService.submit(() -> {
                    try {
                        if (i % 2 == 0) {
                            checkingAccountService.charge(chargeRequest);
                        } else {
                            savingAccountService.payIn(payInRequest);
                        }
                    } catch (LockException | AccountException e) {
                        logger.info("error : " + e.getMessage());
                    } finally {
                        countDownLatch.countDown();
                    }
                })
        );

        countDownLatch.await();
        executorService.shutdown();

        // then
        Optional<CheckingAccount> findCheckingAccount =
                checkingAccountRepository.findById(checkingAccount.getId());

        Optional<SavingAccount> findSavingAccount =
                savingAccountRepository.findById(savingAccount.getId());

        assertTrue(findCheckingAccount.isPresent());
        assertTrue(findSavingAccount.isPresent());
        assertEquals(150_000L, findCheckingAccount.get().getBalance());
        assertEquals(150_000L, findSavingAccount.get().getAmount());
    }

    @Nested
    @DisplayName("송금하기")
    class RemitMethod {
        String senderAccountNumber = "8888-01-1234567";
        String receiverAccountNumber = "8888-02-7654321";

        @Test
        @DisplayName("동일한 친구에게 송금하는 요청이 동시에 올 경우 차례로 실행된다.")
        void concurrency_remit() throws InterruptedException {
            // given
            int threadCount = 50;
            int repeat = 100;

            Member receiver = memberRepository.save(Member.builder()
                    .email("test@test.com")
                    .password("test12345")
                    .name("tester1")
                    .build());

            checkingAccountRepository.save(
                    CheckingAccount.of(receiverAccountNumber, receiver));

            memberRepository.save(receiver);
            settingRepository.save(Setting.create(receiver));

            String[] accountNumbers = new String[repeat];

            for (int i = 0; i < repeat; i++) {
                Member member = memberRepository.save(Member.builder()
                        .email("test" + i + "@test.com")
                        .password("test12345")
                        .name("tester")
                        .build());

                String accountNumber = "8888-" + i + "-1234567";

                checkingAccountRepository.save(
                        CheckingAccount.of(accountNumber, member));

                memberRepository.save(member);
                settingRepository.save(Setting.create(member));

                accountNumbers[i] = accountNumber;
            }

            // when
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch countDownLatch = new CountDownLatch(repeat);

            IntStream.range(0, repeat).forEach(i ->
                    executorService.submit(() -> {
                        try {
                            transactionService.remit(RemitRequest.builder()
                                    .senderAccountNumber(accountNumbers[i])
                                    .receiverAccountNumber(receiverAccountNumber)
                                    .amount(10000L)
                                    .build());
                        } catch (AccountException | LockException e) {
                            logger.info("error : " + e.getMessage());
                        } finally {
                            countDownLatch.countDown();
                        }
                    }));

            countDownLatch.await();
            executorService.shutdown();

            // then
            Optional<CheckingAccount> receiverCheckingAccount =
                    checkingAccountRepository.findByAccountNumber(receiverAccountNumber);

            assertTrue(receiverCheckingAccount.isPresent());
            assertEquals(10000 * repeat, receiverCheckingAccount.get().getBalance());
        }

        @Test
        @DisplayName("내가 친구에게 송금하는 요청과 친구가 나에게 송금하는 요청이 동시에 올 경우 차례로 실행된다.")
        void concurrency_remit_deadlock() throws InterruptedException {
            // given
            setSenderAndReceiver();

            // 내 계좌 충전
            checkingAccountService.charge(
                    new ChargeRequest(senderAccountNumber, 10000L));

            // 친구 계좌 충전
            checkingAccountService.charge(
                    new ChargeRequest(receiverAccountNumber, 10000L));

            RemitRequest request = RemitRequest.builder()
                    .senderAccountNumber(senderAccountNumber)
                    .receiverAccountNumber(receiverAccountNumber)
                    .amount(10000L)
                    .build();

            RemitRequest request2 = RemitRequest.builder()
                    .senderAccountNumber(receiverAccountNumber)
                    .receiverAccountNumber(senderAccountNumber)
                    .amount(10000L)
                    .build();

            // when
            AtomicInteger exceptionCnt = new AtomicInteger();

            Thread thread = new Thread(() -> {
                try {
                    transactionService.remit(request);
                } catch (LockException e) {
                    exceptionCnt.getAndIncrement();
                }
            });

            Thread thread2 = new Thread(() -> {
                try {
                    transactionService.remit(request2);
                } catch (LockException e) {
                    exceptionCnt.getAndIncrement();
                }
            });

            thread.start();
            thread2.start();

            thread.join();
            thread2.join();

            // then
            CheckingAccount senderCheckingAccount =
                    checkingAccountRepository.findByAccountNumber(senderAccountNumber).get();

            CheckingAccount receiverCheckingAccount =
                    checkingAccountRepository.findByAccountNumber(receiverAccountNumber).get();

            assertEquals(10000L, senderCheckingAccount.getBalance());
            assertEquals(10000L, receiverCheckingAccount.getBalance());
            assertEquals(0, exceptionCnt.get());
        }

        @Test
        @DisplayName("친구에게 송금 요청과 친구가 충전하는 요청이 동시에 올 경우 차례로 실행된다.")
        void concurrency_remit_charge() throws InterruptedException {
            // given
            setSenderAndReceiver();

            RemitRequest remitRequest = RemitRequest.builder()
                    .senderAccountNumber(senderAccountNumber)
                    .receiverAccountNumber(receiverAccountNumber)
                    .amount(10000L)
                    .build();

            ChargeRequest chargeRequest =
                    new ChargeRequest(receiverAccountNumber, 10000L);

            // when
            Thread thread = new Thread(() ->
                    transactionService.remit(remitRequest));

            Thread thread2 = new Thread(() ->
                    checkingAccountService.charge(chargeRequest));

            thread.start();
            thread2.start();

            thread.join();
            thread2.join();

            // then
            CheckingAccount senderCheckingAccount =
                    checkingAccountRepository.findByAccountNumber(senderAccountNumber).get();

            CheckingAccount receiverCheckingAccount =
                    checkingAccountRepository.findByAccountNumber(receiverAccountNumber).get();

            assertEquals(0, senderCheckingAccount.getBalance());
            assertEquals(20000, receiverCheckingAccount.getBalance());
        }

        @Test
        @DisplayName("친구에게 송금 요청과 친구가 적금 납입하는 요청이 동시에 올 경우 차례로 실행된다.")
        void concurrency_remit_payIn() throws InterruptedException {
            // given
            Member receiver = setSenderAndReceiver();
            String savingAccountNumber = "8800-09-7654321";

            savingAccountRepository.save(
                    SavingAccount.of(savingAccountNumber, receiver));

            // 친구 계좌 충전
            checkingAccountService.charge(
                    new ChargeRequest(receiverAccountNumber, 10000L));

            RemitRequest remitRequest = RemitRequest.builder()
                    .senderAccountNumber(senderAccountNumber)
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
                    transactionService.remit(remitRequest));

            Thread thread2 = new Thread(() ->
                    savingAccountService.payIn(payInRequest));

            thread.start();
            thread2.start();

            thread.join();
            thread2.join();

            // then
            CheckingAccount senderCheckingAccount =
                    checkingAccountRepository.findByAccountNumber(senderAccountNumber).get();

            CheckingAccount receiverCheckingAccount =
                    checkingAccountRepository.findByAccountNumber(receiverAccountNumber).get();

            assertEquals(0, senderCheckingAccount.getBalance());
            assertEquals(10000, receiverCheckingAccount.getBalance());
        }

        private Member setSenderAndReceiver() {
            Member sender = memberRepository.save(Member.builder()
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
                    CheckingAccount.of(senderAccountNumber, sender));

            checkingAccountRepository.save(
                    CheckingAccount.of(receiverAccountNumber, receiver));

            memberRepository.save(sender);
            memberRepository.save(receiver);

            settingRepository.save(Setting.create(sender));
            settingRepository.save(Setting.create(receiver));
            return receiver;
        }
    }
}
