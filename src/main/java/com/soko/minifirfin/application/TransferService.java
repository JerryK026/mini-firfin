package com.soko.minifirfin.application;

import com.soko.minifirfin.common.Status;
import com.soko.minifirfin.common.exception.BadRequestCode;
import com.soko.minifirfin.common.exception.BadRequestException;
import com.soko.minifirfin.domain.Member;
import com.soko.minifirfin.domain.MemberMoney;
import com.soko.minifirfin.domain.Money;
import com.soko.minifirfin.domain.TransferHistory;
import com.soko.minifirfin.repository.MemberMoneyRepository;
import com.soko.minifirfin.repository.TransferHistoryRepository;
import com.soko.minifirfin.ui.response.TransferHistoriesResponse;
import com.soko.minifirfin.ui.response.TransferHistoryResponse;
import com.soko.minifirfin.ui.response.TransferResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final RedisTemplate<String, TransferHistoryResponse> redisTemplate;

    public TransferService(
        final MemberMoneyRepository memberMoneyRepository,
        final TransferHistoryRepository transferHistoryRepository,
        final RedisTemplate<String, TransferHistoryResponse> redisTemplate
    ) {
        this.memberMoneyRepository = memberMoneyRepository;
        this.transferHistoryRepository = transferHistoryRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public TransferResponse transfer(
        final Long senderId,
        final Long receiverId,
        final Money sendMoneyAmount,
        final String senderIpAddress
    ) {
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

        Member sender = senderMoney.getMember();

        // TODO: 부생성자 더 만들던가 정적팩토리 메서드 구현해서 깔끔하게 만들 필요가 있음
        TransferHistory transferHistory = transferHistoryRepository.save(
            new TransferHistory(
                sender,
                receiverMoney.getMember(),
                sendMoneyAmount,
                senderMoneyAmountAfterTransfer,
                receiverMoneyAmountAfterTransfer,
                senderIpAddress,
                sender.getEmail(),
                sender.getPhoneNumber(),
                senderMoney.getPaymentMethod(),
                senderMoney.getPaymentInfo(),
                sender.getSerialNumber()
            )
        );

        // TODO: 캐시에 적재가 실패했다고 해서 송금에 실패해야 하는가?
        String key = "transfer:historyResponse:" + senderId;
        redisTemplate.opsForZSet().add(
            key,
            TransferHistoryResponse.of(transferHistory),
            transferHistory.getId()
        );
        redisTemplate.expire(key, 30, TimeUnit.DAYS);

        // TODO: 부생성자 더 만들던가 정적팩토리 메서드 구현해서 깔끔하게 만들 필요가 있음
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

    /*
     * 캐시(redis)를 우선 확인하고 캐시에 값이 없으면 db에서 질의해서 반환한다.
     * 캐시에는 값이 TransferHistoryResponse(DTO) 형태로 들어있고, db에는 Entity 꼴로 들어있다.
     * */
    // TODO: 만약 캐시에 값이 1개만 들어있다면, 페이지에 1개만 담아서 response할텐데 이걸 감수할만한가? 괜히 hop만 한 번 더 늘리는 현상은 아닐까?
    // => 차라리 page에 값이 부족하면 db에 질의해서 채워넣어 보는건 어떨까?
    //    => 이 경우 그럼 괜히 관리 포인트만 늘리고 커넥션 수도 늘어나는 것 아닌가?
    // => 도메인 특성상 한 번 넣어놓고 그걸 바탕으로 여러 번 입금받을 것이지, 매번 입금받는 구조는 아닐 것이라고 판단.
    //    TTL이 30일이므로 만약 부족한 수만큼 캐시에서 얻어오는 식으로 구성한다면 리소스 낭비가 너무 심할 것이다.
    public TransferHistoriesResponse transferHistories(final Long senderId, final Long cursorId) {
        Set<TransferHistoryResponse> transferHistoryResponses = redisTemplate.opsForZSet()
            .reverseRangeByScore(
                "transfer:historyResponse:" + senderId,
                0,
                getCursor(cursorId),
                0,
                DEFAULT_PAGE_SIZE
            );

        if (isCachedData(transferHistoryResponses)) {
            long nextCursor = getCursor(transferHistoryResponses);

            return TransferHistoriesResponse.of(
                transferHistoryResponses.stream().toList(),
                nextCursor,
                Status.SUCCESS
            );
        }

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

    private boolean isCachedData(Set<TransferHistoryResponse> transferHistoryResponses) {
        return !transferHistoryResponses.isEmpty();
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

    private Long getCursor(Set<TransferHistoryResponse> transferHistories) {
        if (transferHistories.isEmpty()) {
            return -1L;
        }

        long count = transferHistories.stream().count();

        return transferHistories.stream().skip(count - 1).findFirst().get().transferHistoryId();
    }

    private Long getCursor(final Long cursor) {
        return Objects.isNull(cursor) ? 0 : cursor - 1;
    }

    private void validateSamePerson(final Long senderId, final Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new BadRequestException(BadRequestCode.SENDER_AND_RECEIVER_ARE_SAME);
        }
    }

    // TODO: 객체로 뽑아 단위테스트로 보내도 좋을 듯. 이때 인자로 LocalDateTime을 넘겨서 외부에서 시간을 조작할 수 있도록 해야 한다.
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
