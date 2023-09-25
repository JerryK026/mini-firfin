package com.soko.minifirfin.application;

import static com.soko.minifirfin.common.Constants.DEFAULT_PAGE_SIZE;

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
import com.soko.minifirfin.ui.response.TransferHistoriesResponse;
import com.soko.minifirfin.ui.response.TransferResponse;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
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

    public TransferHistoriesResponse transferHistories(Long senderId, Long cursorId) {
        List<TransferHistory> transferHistoriesPageByCursor =
            findTransferHistoriesPageByCursor(senderId, cursorId, DEFAULT_PAGE_SIZE);

        return TransferHistoriesResponse.of(
            transferHistoriesPageByCursor,
            transferHistoriesPageByCursor.isEmpty(),
            getCursor(transferHistoriesPageByCursor)
        );
    }

    private Member findMemberByIdForUpdate(Long memberId) {
        return memberRepository.findByIdForUpdate(memberId)
            .orElseThrow(() -> new BadRequestException(BadRequestCode.MEMBER_NOT_FOUND));
    }

    private List<TransferHistory> findTransferHistoriesPageByCursor(
        Long senderId,
        Long cursor,
        int limit
    ) {
        // first page
        if (Objects.isNull(cursor)) {
            return transferHistoryRepository.findBySenderIdOrderByCreatedDateTimeDescLimit(
                senderId,
                limit
            );
        }

        // after first page
        return transferHistoryRepository.findBySenderIdOrderByCreatedDateTimeDescLimitWithCursor(
            senderId,
            cursor,
            limit
        );
    }

    private Long getCursor(List<TransferHistory> transferHistories) {
        if (transferHistories.isEmpty()) {
            return -1L;
        }

        return transferHistories.get(transferHistories.size() - 1).getId();
    }
}
