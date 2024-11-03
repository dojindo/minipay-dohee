package com.jindo.minipay.account.checking.service.charge;

import com.jindo.minipay.account.checking.dto.ChargeRequest;
import com.jindo.minipay.account.checking.dto.ChargeResponse;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.common.exception.AccountException;
import com.jindo.minipay.lock.annotation.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.jindo.minipay.global.exception.ErrorCode.EXCEEDED_DAILY_CHARGING_LIMIT;
import static com.jindo.minipay.global.exception.ErrorCode.NOT_FOUND_ACCOUNT_NUMBER;

@RequiredArgsConstructor
@Service
public class ChargeService {
    private final CheckingAccountRepository checkingAccountRepository;
    private final ValueOperations<String, Object> redisValueOps;

    private static final Integer DAILY_CHARGING_LIMIT = 3_000_000;
    private static final int CHARGING_UNIT = 10_000;
    private static final String CHARGE_KEY_PREFIX = "CHARGE:";

    @DistributedLock(keyField = "accountNumber")
    @Transactional
    public ChargeResponse charge(ChargeRequest request) {
        CheckingAccount checkingAccount = checkingAccountRepository
                .findByAccountNumberFetchJoin(request.accountNumber())
                .orElseThrow(() -> new AccountException(NOT_FOUND_ACCOUNT_NUMBER));

        chargeToAccount(checkingAccount,
                checkingAccount.getMember().getEmail(), request.amount());
        return ChargeResponse.fromEntity(checkingAccount);
    }

    public void autoChargingOrNot(CheckingAccount checkingAccount,
                                  String email,
                                  Long amount) {
        long balance = checkingAccount.getBalance();

        if (balance < amount) {
            long insufficientAmount = amount - balance; // 부족한 금액
            long share = insufficientAmount / CHARGING_UNIT;
            long remainder = insufficientAmount % CHARGING_UNIT;
            long chargeAmount = share * CHARGING_UNIT;

            if (remainder > 0) {
                chargeAmount += CHARGING_UNIT;
            }

            chargeToAccount(checkingAccount, email, chargeAmount);
        }
    }

    private void chargeToAccount(CheckingAccount checkingAccount,
                                 String email,
                                 Long amount) {
        validateChargeLimit(email, amount);
        checkingAccount.increaseBalance(amount);
    }

    private void validateChargeLimit(String email, Long amount) {
        String key = CHARGE_KEY_PREFIX + email;
        Integer accumulatedAmount = (Integer) redisValueOps.get(key);

        if (accumulatedAmount != null) {
            if (accumulatedAmount + amount > DAILY_CHARGING_LIMIT) {
                throw new AccountException(EXCEEDED_DAILY_CHARGING_LIMIT);
            }
            redisValueOps.increment(key, amount);
        } else {
            long secondsUntilMidnight = getSecondsUntilMidnight();
            redisValueOps.set(key, amount, secondsUntilMidnight, TimeUnit.SECONDS);
        }
    }

    private static long getSecondsUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        Duration duration = Duration.between(now, midnight);
        return duration.getSeconds();
    }
}
