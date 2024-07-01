package com.jindo.minipay.member.dto;

import com.jindo.minipay.member.entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record RegisterRequest(
        @NotBlank
        @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}",
                message = "유효하지 않은 이메일 형식입니다.")
        String email,

        @NotBlank
        @Pattern(regexp = "[a-zA-Z1-9]{8,16}",
                message = "비밀번호는 영어와 숫자를 포함해서 8~16자리 입니다.")
        String password,

        @NotBlank
        String name
) {
    public Member toEntity() {
        return Member.builder()
                .email(email)
                .password(password)
                .name(name)
                .build();
    }
}
