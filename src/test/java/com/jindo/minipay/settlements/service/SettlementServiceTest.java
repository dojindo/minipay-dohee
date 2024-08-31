package com.jindo.minipay.settlements.service;

import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import com.jindo.minipay.settlements.calculator.CalculatorFactory;
import com.jindo.minipay.settlements.calculator.impl.RandomCalculator;
import com.jindo.minipay.settlements.dto.SettleAccountsRequest;
import com.jindo.minipay.settlements.dto.SettleAccountsResponse;
import com.jindo.minipay.settlements.dto.SettleCalculateRequest;
import com.jindo.minipay.settlements.dto.SettleCalculateResponse;
import com.jindo.minipay.settlements.entity.Settlement;
import com.jindo.minipay.settlements.exception.SettlementException;
import com.jindo.minipay.settlements.repository.SettlementRepository;
import com.jindo.minipay.settlements.type.SettlementType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.jindo.minipay.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {
    @Mock
    SettlementRepository settlementRepository;

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    SettlementService settlementService;

    @Nested
    @DisplayName("정산 금액 계산 메서드")
    class SettleCalculateMethod {
        SettleCalculateRequest request = SettleCalculateRequest.builder()
                .settlementType("DUTCH_PAY")
                .totalAmount(35000L)
                .numOfParticipants(3)
                .requesterId(1L)
                .build();

        @ParameterizedTest
        @ValueSource(strings = {"DUTCH_PAY", "RANDOM"})
        @DisplayName("타입별로 정산 금액을 계산한다.")
        void settleCalculate(String settlementType) {
            // given
            SettleCalculateRequest settleCalculateRequest = SettleCalculateRequest.builder()
                    .settlementType(settlementType)
                    .totalAmount(35000L)
                    .numOfParticipants(3)
                    .requesterId(1L)
                    .build();

            given(memberRepository.existsById(1L))
                    .willReturn(true);

            // when
            SettleCalculateResponse response =
                    settlementService.settleCalculate(settleCalculateRequest);

            // then
            long sumAmount = response.requestAmounts().stream()
                    .mapToLong(o -> o)
                    .sum();

            assertEquals(35000, sumAmount + response.remainingAmount());
            assertEquals(35000 - sumAmount, response.remainingAmount());
        }

        @Test
        @DisplayName("정산 요청자를 찾을 수 없으면 예외가 발생한다.")
        void settleCalculate_notFound_requester() {
            // given
            given(memberRepository.existsById(1L))
                    .willReturn(false);

            // when
            // then
            assertThatThrownBy(() -> settlementService.settleCalculate(request))
                    .isInstanceOf(SettlementException.class)
                    .hasMessageContaining(NOT_FOUND_MEMBER.getMessage());
        }

        static Stream<Arguments> provideRequestAmounts() {
            return Stream.of(Arguments.of(List.of()),
                    Arguments.of(List.of(10000L)));
        }

        @ParameterizedTest
        @MethodSource("provideRequestAmounts")
        @DisplayName("정산 요청 금액이 없거나 개수가 정산 인원 수와 맞지 않으면 예외가 발생한다.")
        void settleCalculate_requestAmounts_size_isInvalid(List<Long> requestAmounts) {
            // given
            given(memberRepository.existsById(1L))
                    .willReturn(true);

            MockedStatic<CalculatorFactory> calculatorFactoryMock =
                    mockStatic(CalculatorFactory.class, Mockito.CALLS_REAL_METHODS);

            RandomCalculator randomCalculator = mock(RandomCalculator.class);

            given(CalculatorFactory.of(SettlementType.DUTCH_PAY))
                    .willReturn(randomCalculator);

            given(randomCalculator.calculateAmount(3, 35000L))
                    .willReturn(requestAmounts);

            // when
            // then
            assertThatThrownBy(() -> settlementService.settleCalculate(request))
                    .isInstanceOf(SettlementException.class)
                    .hasMessageContaining(INTERNAL_ERROR.getMessage());

            calculatorFactoryMock.close();
        }
    }

    @Nested
    @DisplayName("정산 요청 메서드")
    class SettleAccountsMethod {
        SettleAccountsRequest request = SettleAccountsRequest.builder()
                .settlementType("RANDOM")
                .totalAmount(35000L)
                .numOfParticipants(2)
                .requesterId(1L)
                .participants(List.of(
                        new SettleAccountsRequest
                                .ParticipantRequest(1L, 10000L),
                        new SettleAccountsRequest
                                .ParticipantRequest(2L, 25000L)))
                .remainingAmount(0)
                .build();

        Member requester = Member.builder()
                .email("test@test.com")
                .password("test12345")
                .name("tester1")
                .build();

        List<Member> members = List.of(requester,
                Member.builder()
                        .email("test2@test.com")
                        .password("test12345")
                        .name("tester2")
                        .build());

        @Test
        @DisplayName("정산 금액으로 정산을 요청한다.")
        void settleAccounts() {
            // given
            given(memberRepository.findByIdIn(List.of(1L, 2L)))
                    .willReturn(members);

            given(memberRepository.findById(1L))
                    .willReturn(Optional.of(requester));

            given(settlementRepository.save(any()))
                    .willReturn(Settlement.builder().build());

            // when
            SettleAccountsResponse response =
                    settlementService.settleAccounts(request);

            // then
            assertEquals(2, response.participants().size());
            verify(settlementRepository, times(1))
                    .save(any());
        }

        @Test
        @DisplayName("정산 요청 금액의 합계와 총 금액이 맞지 않으면 예외가 발생한다.")
        void settleAccounts_invalid_request() {
            // given
            SettleAccountsRequest settleAccountsRequest = SettleAccountsRequest.builder()
                    .settlementType("DUTCH_PAY")
                    .totalAmount(10000L)
                    .numOfParticipants(3)
                    .requesterId(1L)
                    .participants(List.of(new SettleAccountsRequest
                                    .ParticipantRequest(1L, 3000L),
                            new SettleAccountsRequest
                                    .ParticipantRequest(2L, 3333L),
                            new SettleAccountsRequest
                                    .ParticipantRequest(3L, 3333L)))
                    .remainingAmount(1)
                    .build();

            // when
            // then
            assertThatThrownBy(() -> settlementService.settleAccounts(settleAccountsRequest))
                    .isInstanceOf(SettlementException.class)
                    .hasMessageContaining(INCORRECT_TOTAL_AMOUNT.getMessage());
        }

        static Stream<Arguments> provideParticipants() {
            return Stream.of(
                    Arguments.of(List.of()),
                    Arguments.of(List.of(Member.builder()
                            .email("test2@test.com")
                            .password("test12345")
                            .name("tester2")
                            .build()))
            );
        }

        @ParameterizedTest
        @MethodSource("provideParticipants")
        @DisplayName("정산 참여자가 없거나 부족하면 예외가 발생한다.")
        void settleAccounts_notFount_participant(List<Member> participants) {
            // given
            given(memberRepository.findByIdIn(List.of(1L, 2L)))
                    .willReturn(participants);

            // when
            // then
            assertThatThrownBy(() -> settlementService.settleAccounts(request))
                    .isInstanceOf(SettlementException.class)
                    .hasMessageContaining(NOT_FOUND_SETTLEMENT_PARTICIPANTS.getMessage());
        }

        @Test
        @DisplayName("정산 요청자를 찾을 수 없으면 예외가 발생한다.")
        void settleAccounts_notFound_requester() {
            // given
            given(memberRepository.findByIdIn(List.of(1L, 2L)))
                    .willReturn(members);

            given(memberRepository.findById(1L))
                    .willReturn(Optional.empty());

            // when
            // then
            assertThatThrownBy(() -> settlementService.settleAccounts(request))
                    .isInstanceOf(SettlementException.class)
                    .hasMessageContaining(NOT_FOUND_MEMBER.getMessage());
        }
    }
}