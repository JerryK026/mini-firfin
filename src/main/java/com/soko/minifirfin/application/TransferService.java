package com.soko.minifirfin.application;

import com.soko.minifirfin.common.Status;
import com.soko.minifirfin.common.exception.BadRequestCode;
import com.soko.minifirfin.common.exception.BadRequestException;
import com.soko.minifirfin.domain.Member;
import com.soko.minifirfin.domain.MemberMoney;
import com.soko.minifirfin.domain.Money;
import com.soko.minifirfin.domain.TransferHistory;
import com.soko.minifirfin.repository.MemberRepository;
import com.soko.minifirfin.repository.RechargeHistoryRepository;
import com.soko.minifirfin.repository.TransferHistoryRepository;
import com.soko.minifirfin.ui.response.TransferResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
public class TransferService {
    private final MemberRepository memberRepository;
    private final RechargeHistoryRepository rechargeHistoryRepository;
    private final TransferHistoryRepository transferHistoryRepository;

    public TransferService(
            final MemberRepository memberRepository,
            final RechargeHistoryRepository rechargeHistoryRepository,
            final TransferHistoryRepository transferHistoryRepository
    ) {
        this.memberRepository = memberRepository;
        this.rechargeHistoryRepository = rechargeHistoryRepository;
        this.transferHistoryRepository = transferHistoryRepository;
    }

    @Transactional
    public TransferResponse transfer(Long senderId, Long receiverId, Money sendMoneyAmount) {
        Member sender = findMemberByIdForUpdate(senderId);
        Member receiver = findMemberByIdForUpdate(receiverId);
        MemberMoney senderMoney = sender.getMemberMoney();
        MemberMoney receiverMoney = receiver.getMemberMoney();

        senderMoney.transfer(receiverMoney, sendMoneyAmount);

        Money senderMoneyAmountAfterTransfer = sender.getMemberMoney().getMoneyAmount();
        Money receiverMoneyAmountAfterTransfer = receiver.getMemberMoney().getMoneyAmount();

        TransferHistory transferHistory = transferHistoryRepository.save(
                new TransferHistory(
                        sender,
                        receiver,
                        sendMoneyAmount,
                        senderMoneyAmountAfterTransfer,
                        receiverMoneyAmountAfterTransfer
                )
        );

        return new TransferResponse(
                sender.getName(),
                receiver.getName(),
                transferHistory.getId(),
                sendMoneyAmount.getAmount(),
                senderMoneyAmountAfterTransfer.getAmount(),
                transferHistory.getCreatedDateTime(),
                Status.SUCCESS
        );
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BadRequestException(BadRequestCode.MEMBER_NOT_FOUND));
    }

    private Member findMemberByIdForUpdate(Long memberId) {
        return memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new BadRequestException(BadRequestCode.MEMBER_NOT_FOUND));
    }
}
