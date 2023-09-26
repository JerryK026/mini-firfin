package com.soko.minifirfin.application;

import com.soko.minifirfin.common.Status;
import com.soko.minifirfin.common.exception.BadRequestCode;
import com.soko.minifirfin.common.exception.BadRequestException;
import com.soko.minifirfin.domain.Member;
import com.soko.minifirfin.domain.MemberMoney;
import com.soko.minifirfin.domain.Money;
import com.soko.minifirfin.domain.TransferHistory;
import com.soko.minifirfin.repository.MemberMoneyRepository;
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

    private final MemberMoneyRepository memberMoneyRepository;
    private final TransferHistoryRepository transferHistoryRepository;

    public TransferService(
            final MemberMoneyRepository memberMoneyRepository,
            final TransferHistoryRepository transferHistoryRepository
    ) {
        this.memberMoneyRepository = memberMoneyRepository;
        this.transferHistoryRepository = transferHistoryRepository;
    }

    @Transactional
    public TransferResponse transfer(final Long senderId, final Long receiverId, final Money sendMoneyAmount) {
        validateSamePerson(senderId, receiverId);

        if (sendMoneyAmount.isOverThan(new Money(TRANSFER_ONCE_LIMITATION))) {
            throw new BadRequestException(SENDER_OVER_ONCE_LIMITATION);
        }

        // lock 범위 좁히려고 1일 송금 체크한 후 lock을 잡으려 했으나, deadlock 걸려서 lock을 위로 올림
        MemberMoney senderMoney = findMemberMoneyByMemberIdForUpdate(senderId);
        MemberMoney receiverMoney = findMemberMoneyByMemberIdForUpdate(receiverId);

        validateDailyTransferLimitation(senderId, sendMoneyAmount);

        senderMoney.transfer(receiverMoney, sendMoneyAmount);

        Money senderMoneyAmountAfterTransfer = senderMoney.getMoneyAmount();
        Money receiverMoneyAmountAfterTransfer = receiverMoney.getMoneyAmount();

        TransferHistory transferHistory = transferHistoryRepository.save(
                new TransferHistory(
                        senderMoney.getMember(),
                        receiverMoney.getMember(),
                        sendMoneyAmount,
                        senderMoneyAmountAfterTransfer,
                        receiverMoneyAmountAfterTransfer
                )
        );

        return new TransferResponse(
                senderMoney.getMember().getName(),
                receiverMoney.getMember().getName(),
                transferHistory.getId(),
                sendMoneyAmount.getAmount(),
                senderMoneyAmountAfterTransfer.getAmount(),
                transferHistory.getCreatedDateTime(),
                Status.SUCCESS
        );
    }

    public TransferHistoriesResponse transferHistories(final Long senderId, final Long cursorId) {
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

    private MemberMoney findMemberMoneyByMemberIdForUpdate(final Long memberId) {
        return memberMoneyRepository.findByMemberIdForUpdate(memberId)
                .orElseThrow(() -> new BadRequestException(BadRequestCode.MEMBER_NOT_FOUND));
    }

    private List<TransferHistory> findTransferHistoriesPageByCursor(
            final Long senderId,
            final Long cursor,
            final int limit
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

    private Long getCursor(final List<TransferHistory> transferHistories) {
        if (transferHistories.isEmpty()) {
            return -1L;
        }

        return transferHistories.get(transferHistories.size() - 1).getId();
    }

    private void validateSamePerson(final Long senderId, final Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new BadRequestException(BadRequestCode.SENDER_AND_RECEIVER_ARE_SAME);
        }
    }

    private void validateDailyTransferLimitation(final Long senderId, final Money sendMoneyAmount) {
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
