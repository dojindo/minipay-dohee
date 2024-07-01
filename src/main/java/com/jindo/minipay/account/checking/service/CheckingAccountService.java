package com.jindo.minipay.account.checking.service;

import com.jindo.minipay.account.checking.dto.ChargeRequest;
import com.jindo.minipay.account.checking.dto.ChargeResponse;
import com.jindo.minipay.account.checking.dto.RemitRequest;
import com.jindo.minipay.account.checking.dto.RemitResponse;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.common.exception.AccountException;
import com.jindo.minipay.account.common.type.AccountType;
import com.jindo.minipay.account.common.util.AccountNumberComponent;
import com.jindo.minipay.global.exception.ErrorCode;
import com.jindo.minipay.lock.annotation.DistributedLock;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.jindo.minipay.global.exception.ErrorCode.EXCEEDED_DAILY_CHARGING_LIMIT;
import static com.jindo.minipay.global.exception.ErrorCode.NOT_FOUND_MEMBER;

@RequiredArgsConstructor
@Service
public class CheckingAccountService {
    private final CheckingAccountRepository checkingAccountRepository;
    private final MemberRepository memberRepository;
    private final AccountNumberComponent accountNumberComponent;
    private final ValueOperations<String, Object> redisValueOps;

    private static final Integer DAILY_CHARGING_LIMIT = 3_000_000;
    private static final int CHARGING_UNIT = 10_000;

    public void createAccount(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AccountException(NOT_FOUND_MEMBER));

        String accountNumber =
                accountNumberComponent.getAccountNumber(AccountType.CHECKING);
        checkingAccountRepository.save(CheckingAccount.of(accountNumber, member));
    }

    @DistributedLock(keyField = "accountNumber")
    @Transactional
    public ChargeResponse charge(ChargeRequest request) {
        CheckingAccount checkingAccount = getCheckingAccount(request.accountNumber());
        chargeToMyAccount(checkingAccount, request.amount());
        return ChargeResponse.fromEntity(checkingAccount);
    }

    @DistributedLock(keyField = "myAccountNumber")
    @Transactional
    public RemitResponse remit(RemitRequest request) {
        CheckingAccount myCheckingAccount = getCheckingAccount(request.myAccountNumber());

        Long amount = request.amount();
        autoChargingOrNot(myCheckingAccount.getBalance(), amount, myCheckingAccount);

        CheckingAccount receiverCheckingAccount = checkingAccountRepository
                .findByAccountNumber(request.receiverAccountNumber())
                .orElseThrow(() ->
                        new AccountException(ErrorCode.NOT_FOUND_ACCOUNT_NUMBER));

        myCheckingAccount.decreaseBalance(amount);
        receiverCheckingAccount.increaseBalance(amount);

        return RemitResponse.fromEntity(myCheckingAccount);
    }

    private void autoChargingOrNot(long balance, Long amount,
                                   CheckingAccount checkingAccount) {
        if (balance < amount) {
            long insufficientAmount = amount - balance;
            long share = insufficientAmount / CHARGING_UNIT;
            long remainder = insufficientAmount % CHARGING_UNIT;
            long chargeAmount = share * CHARGING_UNIT;

            if (remainder > 0) {
                chargeAmount += CHARGING_UNIT;
            }

            chargeToMyAccount(checkingAccount, chargeAmount);
        }
    }

    private void chargeToMyAccount(CheckingAccount checkingAccount, Long amount) {
        String email = checkingAccount.getMember().getEmail();
        validateChargeLimit(email, amount);

        checkingAccount.increaseBalance(amount);
    }

    private CheckingAccount getCheckingAccount(String accountNumber) {
        return checkingAccountRepository
                .findByAccountNumberFetchJoin(accountNumber)
                .orElseThrow(() ->
                        new AccountException(ErrorCode.NOT_FOUND_ACCOUNT_NUMBER));
    }

    private void validateChargeLimit(String email, Long amount) {
        Integer accumulatedAmount = (Integer) redisValueOps.get(email);

        if (accumulatedAmount != null) {
            if (accumulatedAmount + amount > DAILY_CHARGING_LIMIT) {
                throw new AccountException(EXCEEDED_DAILY_CHARGING_LIMIT);
            }
            redisValueOps.increment(email, amount);
        } else {
            long secondsUntilMidnight = getSecondsUntilMidnight();
            redisValueOps.set(email, amount, secondsUntilMidnight, TimeUnit.SECONDS);
        }
    }

    private static long getSecondsUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        Duration duration = Duration.between(now, midnight);
        return duration.getSeconds();
    }
}
