package com.jindo.minipay.settlements.service;

import com.jindo.minipay.member.entity.Member;
import com.jindo.minipay.member.repository.MemberRepository;
import com.jindo.minipay.settlements.calculator.Calculator;
import com.jindo.minipay.settlements.calculator.CalculatorFactory;
import com.jindo.minipay.settlements.dto.SettleAccountsRequest;
import com.jindo.minipay.settlements.dto.SettleAccountsResponse;
import com.jindo.minipay.settlements.dto.SettleCalculateRequest;
import com.jindo.minipay.settlements.dto.SettleCalculateResponse;
import com.jindo.minipay.settlements.entity.Settlement;
import com.jindo.minipay.settlements.entity.SettlementParticipant;
import com.jindo.minipay.settlements.exception.SettlementException;
import com.jindo.minipay.settlements.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static com.jindo.minipay.global.exception.ErrorCode.*;

@RequiredArgsConstructor
@Service
public class SettlementService {
    private final SettlementRepository settlementRepository;
    private final MemberRepository memberRepository;

    public SettleCalculateResponse settleCalculate(SettleCalculateRequest request) {
        validateRequester(request);

        List<Long> requestAmounts = calculateAmount(request);
        validateCalculateAmount(request, requestAmounts);

        return SettleCalculateResponse.of(request.getSettlementType(),
                request.getNumOfParticipants(), request.getTotalAmount(),
                requestAmounts);
    }

    @Transactional
    public SettleAccountsResponse settleAccounts(SettleAccountsRequest request) {
        validateSettlementAmount(request);

        List<Member> participants = getParticipantsOrThrow(request);
        Member requester = memberRepository.findById(request.requesterId())
                .orElseThrow(() -> new SettlementException(NOT_FOUND_MEMBER));

        Settlement settlement = request.toEntity(requester);
        createAndAddParticipants(participants, request.participants(),
                requester, settlement);

        settlementRepository.save(settlement);
        return SettleAccountsResponse.from(settlement);
    }

    private void validateSettlementAmount(SettleAccountsRequest request) {
        // totalAmount 확인
        long sumAmount = request.participants().stream()
                .mapToLong(SettleAccountsRequest.ParticipantRequest::requestAmount)
                .sum();
        sumAmount += request.remainingAmount();

        if (sumAmount != request.totalAmount()) {
            throw new SettlementException(INCORRECT_TOTAL_AMOUNT);
        }
    }

    private List<Member> getParticipantsOrThrow(SettleAccountsRequest request) {
        List<SettleAccountsRequest.ParticipantRequest> participantsRequest =
                request.participants();

        List<Long> idList = participantsRequest.stream()
                .map(SettleAccountsRequest.ParticipantRequest::participantId)
                .toList();

        List<Member> participants = memberRepository.findByIdIn(idList);

        validateParticipants(participants, idList);
        return participants;
    }

    private void createAndAddParticipants(List<Member> participants,
                                          List<SettleAccountsRequest.ParticipantRequest>
                                                  participantRequests,
                                          Member requester,
                                          Settlement settlement) {

        IntStream.range(0, participants.size()).forEach(i -> {
            Member participant = participants.get(i);
            SettleAccountsRequest.ParticipantRequest participantRequest =
                    participantRequests.get(i);

            if (Objects.equals(participant.getId(), requester.getId())) {
                settlement.addParticipants(SettlementParticipant.ofRequester(
                        participantRequest.requestAmount(), requester));
            } else {
                settlement.addParticipants(SettlementParticipant.of(
                        participantRequest.requestAmount(), participant));
            }
        });
    }

    private void validateParticipants(List<Member> participants, List<Long> idList) {
        if (participants.isEmpty() || participants.size() != idList.size()) {
            throw new SettlementException(NOT_FOUND_SETTLEMENT_PARTICIPANTS);
        }
    }

    private void validateCalculateAmount(SettleCalculateRequest request,
                                         List<Long> requestAmounts) {
        if (requestAmounts.isEmpty() ||
                requestAmounts.size() != request.getNumOfParticipants()) {
            throw new SettlementException(INTERNAL_ERROR);
        }
    }

    private List<Long> calculateAmount(SettleCalculateRequest request) {
        Calculator calculator = CalculatorFactory.of(request.getSettlementType());
        return calculator.calculateAmount(request.getNumOfParticipants(),
                request.getTotalAmount());
    }

    private void validateRequester(SettleCalculateRequest request) {
        if (!memberRepository.existsById(request.getRequesterId())) {
            throw new SettlementException(NOT_FOUND_MEMBER);
        }
    }
}
