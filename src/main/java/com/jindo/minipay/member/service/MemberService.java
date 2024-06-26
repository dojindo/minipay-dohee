package com.jindo.minipay.member.service;

import com.jindo.minipay.account.checking.event.CheckingAccountEventPublisher;
import com.jindo.minipay.account.checking.event.CreateCheckingAccountEvent;
import com.jindo.minipay.member.dto.RegisterRequest;
import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.exception.MemberException;
import com.jindo.minipay.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.jindo.minipay.global.exception.ErrorCode.ALREADY_EXISTS_MEMBER;

@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final CheckingAccountEventPublisher accountEventPublisher;

    @Transactional
    public Long register(RegisterRequest request) {
        validateEmail(request);

        Member savedMember = memberRepository.save(request.toEntity());
        Long memberId = savedMember.getId();

        accountEventPublisher.publishCreateCheckingAccount(
                CreateCheckingAccountEvent.of(memberId));
        return memberId;
    }

    private void validateEmail(RegisterRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new MemberException(ALREADY_EXISTS_MEMBER);
        }
    }
}
