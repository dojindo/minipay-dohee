package com.jindo.minipay.account.checking.service;

import com.jindo.minipay.account.checking.dto.ChargeRequest;
import com.jindo.minipay.account.checking.dto.ChargeResponse;
import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.checking.service.charge.ChargeService;
import com.jindo.minipay.account.common.exception.AccountException;
import com.jindo.minipay.account.common.type.AccountType;
import com.jindo.minipay.account.common.util.AccountNumberComponent;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.jindo.minipay.global.exception.ErrorCode.NOT_FOUND_MEMBER;

@RequiredArgsConstructor
@Service
public class CheckingAccountService {
    private final CheckingAccountRepository checkingAccountRepository;
    private final MemberRepository memberRepository;
    private final AccountNumberComponent accountNumberComponent;
    private final ChargeService chargeService;

    public void createAccount(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AccountException(NOT_FOUND_MEMBER));

        String accountNumber =
                accountNumberComponent.getAccountNumber(AccountType.CHECKING);

        checkingAccountRepository.save(CheckingAccount.of(accountNumber, member));
    }

    public ChargeResponse charge(ChargeRequest request) {
        return chargeService.charge(request);
    }
}
