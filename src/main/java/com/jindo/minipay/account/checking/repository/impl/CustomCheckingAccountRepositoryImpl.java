package com.jindo.minipay.account.checking.repository.impl;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.account.checking.repository.CustomCheckingAccountRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.jindo.minipay.account.checking.entity.QCheckingAccount.checkingAccount;
import static com.jindo.minipay.member.entity.QMember.member;

@RequiredArgsConstructor
public class CustomCheckingAccountRepositoryImpl implements CustomCheckingAccountRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<CheckingAccount> findByAccountNumberFetchJoin(String accountNumber) {
        return Optional.ofNullable(queryFactory.selectFrom(checkingAccount)
                .join(checkingAccount.member, member).fetchJoin()
                .where(checkingAccount.accountNumber.eq(accountNumber))
                .fetchOne());
    }
}
