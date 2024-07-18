package com.jindo.minipay.settlements.entity;

import com.jindo.minipay.global.entity.BaseTimeEntity;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.settlements.type.SettlementType;
import com.jindo.minipay.settlements.type.SettlementStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Settlement extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementType settlementType;

    @ColumnDefault("1")
    private int numOfParticipants;

    @ColumnDefault("0")
    private long totalAmount;

    @ColumnDefault("0")
    private long remainingAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus settlementStatus;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private Member requester;

    @OneToMany(mappedBy = "settlement", cascade = CascadeType.PERSIST)
    private final List<SettlementParticipant> participants = new ArrayList<>();

    @Builder
    public Settlement(int numOfParticipants, long totalAmount, long remainingAmount,
                      SettlementType settlementType, Member requester) {
        this.numOfParticipants = numOfParticipants;
        this.totalAmount = totalAmount;
        this.remainingAmount = remainingAmount;
        this.settlementType = settlementType;
        this.requester = requester;
        this.settlementStatus = SettlementStatus.WAITING;
    }

    public void addParticipants(SettlementParticipant participant) {
        participants.add(participant);
        participant.setSettlement(this);
    }
}
