package com.jindo.minipay.account.checking.service;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CheckingAccountRepository;
import com.jindo.minipay.account.common.exception.AccountException;
import com.jindo.minipay.account.common.type.AccountType;
import com.jindo.minipay.account.common.util.AccountNumberGenerator;
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

    public void createCheckingAccount(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AccountException(NOT_FOUND_MEMBER));

        String accountNumber = generateAccountNumber();
        checkingAccountRepository.save(CheckingAccount.of(accountNumber, member));
    }

    // TODO: 계좌가 많아질수록 중복 확인이 느려진다. 유저별로 계좌를 식별할 수 있으면 좋을듯
    private String generateAccountNumber() {
        String accountNumber;
        boolean isNew;

        do {
            accountNumber =
                    AccountNumberGenerator.generateAccountNumber(AccountType.CHECKING);
            isNew = !checkingAccountRepository.existsByAccountNumber(accountNumber);
        } while (!isNew);

        return accountNumber;
    }
}
