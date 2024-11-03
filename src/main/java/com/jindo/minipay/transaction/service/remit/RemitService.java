package com.jindo.minipay.transaction.service.remit;

import com.jindo.minipay.account.checking.entity.CheckingAccount;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import com.jindo.minipay.setting.entity.Setting;
import com.jindo.minipay.setting.repository.SettingRepository;
import com.jindo.minipay.setting.type.RemitSettingType;
import com.jindo.minipay.transaction.dto.RemitRequest;
import com.jindo.minipay.transaction.dto.RemitResponse;
import com.jindo.minipay.transaction.exception.TransactionException;
import com.jindo.minipay.transaction.repository.TransactionRepository;
import com.jindo.minipay.transaction.service.remit.strategy.RemitStrategy;
import com.jindo.minipay.transaction.service.remit.strategy.impl.ImmediatelyRemitStrategy;
import com.jindo.minipay.transaction.service.remit.strategy.impl.PendingRemitStrategy;
import com.jindo.minipay.transaction.type.TransactionStatus;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.EnumMap;

import static com.jindo.minipay.global.exception.ErrorCode.NOT_FOUND_MEMBER;
import static com.jindo.minipay.global.exception.ErrorCode.NOT_FOUND_SETTING;
import static com.jindo.minipay.setting.type.RemitSettingType.IMMEDIATE;
import static com.jindo.minipay.setting.type.RemitSettingType.PENDING;

@Service
public class RemitService {
    private final MemberRepository memberRepository;
    private final SettingRepository settingRepository;
    private final TransactionRepository transactionRepository;

    private final EnumMap<RemitSettingType, RemitStrategy> remitStrategyEnumMap
            = new EnumMap<>(RemitSettingType.class);

    public RemitService(MemberRepository memberRepository,
                        SettingRepository settingRepository,
                        ImmediatelyRemitStrategy immediatelyRemitStrategy,
                        PendingRemitStrategy pendingRemitStrategy,
                        TransactionRepository transactionRepository) {
        this.memberRepository = memberRepository;
        this.settingRepository = settingRepository;
        this.transactionRepository = transactionRepository;

        remitStrategyEnumMap.put(IMMEDIATE, immediatelyRemitStrategy);
        remitStrategyEnumMap.put(PENDING, pendingRemitStrategy);
    }

    public RemitResponse remit(RemitRequest request) {
        Member sender = memberRepository.findByAccountNumber(
                        request.senderAccountNumber())
                .orElseThrow(() -> new TransactionException(NOT_FOUND_MEMBER));

        Setting setting = settingRepository.findByMember(sender)
                .orElseThrow(() -> new TransactionException(NOT_FOUND_SETTING));

        RemitStrategy remitStrategy =
                remitStrategyEnumMap.get(setting.getRemitSettingType());

        Pair<CheckingAccount, CheckingAccount> pair =
                remitStrategy.remit(request, sender.getEmail());

        transactionRepository.save(request.toEntity(pair.getFirst(), pair.getSecond(),
                        getStatus(setting.getRemitSettingType())));

        // todo 메시지 전송
        return RemitResponse.fromEntity(pair.getFirst());
    }

    private TransactionStatus getStatus(RemitSettingType settingType) {
        return switch (settingType) {
            case PENDING -> TransactionStatus.PENDING;
            case IMMEDIATE -> TransactionStatus.COMPLETE;
        };
    }
}
