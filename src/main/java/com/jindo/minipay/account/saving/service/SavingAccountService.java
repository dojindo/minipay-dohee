package com.jindo.minipay.account.saving.service;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.common.exception.AccountException;
import com.jindo.minipay.account.common.type.AccountType;
import com.jindo.minipay.account.common.util.AccountNumberComponent;
import com.jindo.minipay.account.saving.dto.CreateSavingAccountRequest;
import com.jindo.minipay.account.saving.dto.PayInRequest;
import com.jindo.minipay.account.saving.dto.PayInResponse;
import com.jindo.minipay.account.saving.entity.SavingAccount;
import com.jindo.minipay.account.saving.repository.SavingAccountRepository;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.jindo.minipay.global.exception.ErrorCode.NOT_FOUND_ACCOUNT_NUMBER;
import static com.jindo.minipay.global.exception.ErrorCode.NOT_FOUND_MEMBER;

@RequiredArgsConstructor
@Service
public class SavingAccountService {
    private final SavingAccountRepository savingAccountRepository;
    private final MemberRepository memberRepository;
    private final AccountNumberComponent accountNumberComponent;

    @Transactional
    public Long createAccount(CreateSavingAccountRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new AccountException(NOT_FOUND_MEMBER));

        String accountNumber =
                accountNumberComponent.getAccountNumber(AccountType.SAVINGS);
        SavingAccount savedAccount = savingAccountRepository
                .save(SavingAccount.of(accountNumber, member));

        return savedAccount.getId();
    }

    @Transactional
    public PayInResponse payIn(PayInRequest request) {
        SavingAccount savingAccount = savingAccountRepository
                .findByAccountNumberFetchJoin(request.accountNumber())
                .orElseThrow(() -> new AccountException(NOT_FOUND_ACCOUNT_NUMBER));

        CheckingAccount checkingAccount = savingAccount.getMember()
                .getCheckingAccount();
        Long amount = request.amount();

        // TODO: 동시성 처리
        checkingAccount.decreaseBalance(amount);
        savingAccount.increaseAmount(amount);

        return PayInResponse.fromEntity(savingAccount);
    }
}
