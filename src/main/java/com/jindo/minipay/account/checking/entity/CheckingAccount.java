package com.jindo.minipay.account.checking.entity;

import com.jindo.minipay.account.common.exception.AccountException;
import com.jindo.minipay.global.entity.BaseTimeEntity;
import com.jindo.minipay.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import static com.jindo.minipay.global.exception.ErrorCode.INSUFFICIENT_BALANCE;
import static com.jindo.minipay.global.exception.ErrorCode.INVALID_REQUEST;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class CheckingAccount extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @ColumnDefault("0")
    private Long balance;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public CheckingAccount(String accountNumber, Member member) {
        this.accountNumber = accountNumber;
        this.member = member;
        this.balance = 0L;
    }

    public static CheckingAccount of(String accountNumber, Member member) {
        return new CheckingAccount(accountNumber, member);
    }

    public void increaseBalance(Long amount) {
        if (amount < 0) {
            throw new AccountException(INVALID_REQUEST);
        }
        balance += amount;
    }

    public void decreaseBalance(Long amount) {
        if (balance < amount) {
            throw new AccountException(INSUFFICIENT_BALANCE);
        }
        balance -= amount;
    }
}
