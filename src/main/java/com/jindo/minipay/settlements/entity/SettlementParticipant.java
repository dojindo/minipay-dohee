package com.jindo.minipay.settlements.entity;

import com.jindo.minipay.global.entity.BaseTimeEntity;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.settlements.type.SettlementStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class SettlementParticipant extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ColumnDefault("0")
    private long requestAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus settlementStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ColumnDefault("false")
    private boolean isRequester;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;

    private SettlementParticipant(long requestAmount, Member member) {
        this.requestAmount = requestAmount;
        this.member = member;
        this.settlementStatus = SettlementStatus.WAITING;
    }

    public static SettlementParticipant of(long requestAmount, Member member) {
        return new SettlementParticipant(requestAmount, member);
    }

    public static SettlementParticipant ofRequester(long requestAmount,
                                                    Member member) {
        SettlementParticipant settlementParticipant =
                new SettlementParticipant(requestAmount, member);
        settlementParticipant.isRequester = true;
        return settlementParticipant;
    }
}
