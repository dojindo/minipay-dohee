package com.jindo.minipay.setting.entity;

import com.jindo.minipay.global.entity.BaseTimeEntity;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.setting.type.RemitSettingType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Setting extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RemitSettingType remitSettingType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private Setting(RemitSettingType remitSettingType, Member member) {
        this.remitSettingType = remitSettingType;
        this.member = member;
    }

    // create default setting
    public static Setting create(Member member) {
        return new Setting(RemitSettingType.IMMEDIATE, member);
    }

    public void changeRemitTypeToPending() {
        this.remitSettingType = RemitSettingType.PENDING;
    }
}
