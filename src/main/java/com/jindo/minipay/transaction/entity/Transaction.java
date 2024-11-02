package com.jindo.minipay.transaction.entity;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.global.entity.BaseTimeEntity;
import com.jindo.minipay.transaction.type.TransactionStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Transaction extends BaseTimeEntity { // 거래 내역
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_account_id", nullable = false)
    private CheckingAccount sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_account_id", nullable = false)
    private CheckingAccount receiver;

    @Column(nullable = false)
    private long amount;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Builder
    public Transaction(CheckingAccount sender, CheckingAccount receiver, long amount,
                       TransactionStatus status) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.status = status;
    }
}
