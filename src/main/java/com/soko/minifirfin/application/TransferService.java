package com.soko.minifirfin.application;

import com.soko.minifirfin.common.Status;
import com.soko.minifirfin.common.exception.BadRequestCode;
import com.soko.minifirfin.common.exception.BadRequestException;
import com.soko.minifirfin.domain.Member;
import com.soko.minifirfin.domain.Money;
import com.soko.minifirfin.domain.TransferHistory;
import com.soko.minifirfin.repository.MemberRepository;
import com.soko.minifirfin.repository.RechargeHistoryRepository;
import com.soko.minifirfin.repository.TransferHistoryRepository;
import com.soko.minifirfin.ui.response.TransferHistoriesResponse;
import com.soko.minifirfin.ui.response.TransferResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.soko.minifirfin.common.Constants.*;
import static com.soko.minifirfin.common.exception.BadRequestCode.SENDER_OVER_DAILY_LIMITATION;
import static com.soko.minifirfin.common.exception.BadRequestCode.SENDER_OVER_ONCE_LIMITATION;


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
        validateSamePerson(senderId, receiverId);

        if (sendMoneyAmount.isOverThan(new Money(TRANSFER_ONCE_LIMITATION))) {
            throw new BadRequestException(SENDER_OVER_ONCE_LIMITATION);
        }

        // lock 범위 좁히려고 1일 송금 체크한 후 lock을 잡으려 했으나, deadlock 걸려서 lock을 위로 올림
        Member sender = findMemberByIdForUpdate(senderId);
        Member receiver = findMemberByIdForUpdate(receiverId);

        validateDailyTransferLimitation(senderId, sendMoneyAmount);

        sender.getMemberMoney().transfer(receiver.getMemberMoney(), sendMoneyAmount);

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

        long nextCursor = getCursor(transferHistoriesPageByCursor);

        return TransferHistoriesResponse.of(
                transferHistoriesPageByCursor,
                transferHistoriesPageByCursor.isEmpty(),
                nextCursor,
                nextCursor == -1 ? Status.FAIL : Status.SUCCESS
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

    private void validateSamePerson(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new BadRequestException(BadRequestCode.SENDER_AND_RECEIVER_ARE_SAME);
        }
    }

    private void validateDailyTransferLimitation(Long senderId, Money sendMoneyAmount) {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1).minusSeconds(1);

        List<TransferHistory> transferHistories =
                transferHistoryRepository.findTransferHistoriesByCreatedDateTimeBetweenAndSenderId(
                        startOfToday,
                        endOfToday,
                        senderId
                );

        Money money = new Money(0);
        for (TransferHistory transferHistory : transferHistories) {
            money = money.add(transferHistory.getSendAmount());
        }

        money = money.add(sendMoneyAmount);

        if (money.isOverThan(new Money(TRANSFER_DAILY_LIMITATION))) {
            throw new BadRequestException(SENDER_OVER_DAILY_LIMITATION);
        }
    }
}
