package com.jindo.minipay.member.controller;

import com.jindo.minipay.member.dto.RegisterRequest;
import com.jindo.minipay.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@RestController
public class MemberController {
    private final MemberService memberService;

    /**
     * 회원 등록
     * @param request 회원 정보
     * @return void
     */
    @PostMapping
    public ResponseEntity<Void> register(
            @RequestBody @Valid RegisterRequest request) {
        Long memberId = memberService.register(request);
        return ResponseEntity
                .created(URI.create("/api/v1/members/" + memberId))
                .build();
    }
}
