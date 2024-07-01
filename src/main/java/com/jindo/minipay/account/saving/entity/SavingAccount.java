package com.jindo.minipay.account.saving.entity;

import com.jindo.minipay.account.common.exception.AccountException;
import com.jindo.minipay.account.saving.type.SavingType;
import com.jindo.minipay.global.entity.BaseTimeEntity;
import com.jindo.minipay.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import static com.jindo.minipay.global.exception.ErrorCode.INVALID_REQUEST;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class SavingAccount extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @ColumnDefault("0")
    private long amount;

    @Enumerated(EnumType.STRING)
    private SavingType savingType; // 이후 스탭에서 사용 예정

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private SavingAccount(String accountNumber, Member member) {
        this.accountNumber = accountNumber;
        this.member = member;
    }

    public static SavingAccount of(String accountNumber, Member member) {
        return new SavingAccount(accountNumber, member);
    }

    public void increaseAmount(Long amount) {
        if (amount < 0) {
            throw new AccountException(INVALID_REQUEST);
        }
        this.amount += amount;
    }
}
