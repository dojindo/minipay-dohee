package com.jindo.minipay.account.saving.repository.impl;

import com.jindo.minipay.account.saving.entity.SavingAccount;
import com.jindo.minipay.account.saving.repository.CustomSavingAccountRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.jindo.minipay.account.checking.entity.QCheckingAccount.checkingAccount;
import static com.jindo.minipay.account.saving.entity.QSavingAccount.savingAccount;
import static com.jindo.minipay.member.entity.QMember.member;

@RequiredArgsConstructor
public class CustomSavingAccountRepositoryImpl implements CustomSavingAccountRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<SavingAccount> findByAccountNumberFetchJoin(String accountNumber) {
        return Optional.ofNullable(queryFactory.selectFrom(savingAccount)
                .join(savingAccount.member, member).fetchJoin()
                .join(member.checkingAccount, checkingAccount).fetchJoin()
                .where(savingAccount.accountNumber.eq(accountNumber))
                .fetchOne());
    }
}
