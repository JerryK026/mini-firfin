package com.soko.minifirfin.application;

import com.soko.minifirfin.common.Status;
import com.soko.minifirfin.common.exception.BadRequestCode;
import com.soko.minifirfin.common.exception.BadRequestException;
import com.soko.minifirfin.domain.MemberMoney;
import com.soko.minifirfin.domain.Money;
import com.soko.minifirfin.domain.RechargeHistory;
import com.soko.minifirfin.repository.MemberMoneyRepository;
import com.soko.minifirfin.repository.MemberRepository;
import com.soko.minifirfin.repository.RechargeHistoryRepository;
import com.soko.minifirfin.ui.response.RechargeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.soko.minifirfin.common.Constants.RECHARGE_DAILY_LIMITATION;
import static com.soko.minifirfin.common.Constants.RECHARGE_ONCE_LIMITATION;
import static com.soko.minifirfin.common.exception.BadRequestCode.*;

@Service
@Transactional(readOnly = true)
public class RechargeService {
    private final RechargeHistoryRepository rechargeHistoryRepository;
    private final MemberMoneyRepository memberMoneyRepository;
    private final MemberRepository memberRepository;

    public RechargeService(
            final RechargeHistoryRepository rechargeHistoryRepository,
            final MemberMoneyRepository memberMoneyRepository,
            final MemberRepository memberRepository
    ) {
        this.rechargeHistoryRepository = rechargeHistoryRepository;
        this.memberMoneyRepository = memberMoneyRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public RechargeResponse recharge(Long memberId, Money rechargeAmount) {
        if (rechargeAmount.isOverThan(new Money(RECHARGE_ONCE_LIMITATION))) {
            throw new BadRequestException(RECHARGE_OVER_ONCE_LIMITATION);
        }

        validateDailyRechargeLimitation(memberId, rechargeAmount);

        // TODO: 이 로직이 뒤에 있어도 데드락 안 걸리는지 확인
        MemberMoney memberMoney = findMemberMoneyByMemberIdForUpdate(memberId);
        memberMoney.recharge(rechargeAmount);

        RechargeHistory rechargeHistory = rechargeHistoryRepository.save(new RechargeHistory(
                memberId,
                rechargeAmount,
                memberMoney.getMoneyAmount()
        ));

        return new RechargeResponse(
                rechargeHistory.getId(),
                memberId,
                memberMoney.getMember().getName(),
                memberMoney.getMoneyAmountAsBigDecimal(),
                rechargeAmount.getAmount(),
                rechargeHistory.getCreatedDateTime(),
                Status.SUCCESS
        );
    }

    // TODO: 이 로직 RechargeHistories 일급 컬렉션 만들어서 도메인 레벨로 옮기고, 단위 테스트 추가하는 것도 좋을 듯
    private void validateDailyRechargeLimitation(Long memberId, Money rechargeAmount) {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1).minusSeconds(1);
        List<RechargeHistory> rechargeHistoriesOfToday =
                rechargeHistoryRepository.findRechargeHistoriesByCreatedDateTimeBetweenAndMemberId(
                        startOfToday,
                        endOfToday,
                        memberId
                );

        Money money = new Money(0);
        for (RechargeHistory history : rechargeHistoriesOfToday) {
            money = money.add(history.getRechargeAmount());
        }

        money = money.add(rechargeAmount);

        if (money.isOverThan(new Money(RECHARGE_DAILY_LIMITATION))) {
            throw new BadRequestException(RECHARGE_OVER_DAILY_LIMITATION);
        }
    }

    private MemberMoney findMemberMoneyByMemberIdForUpdate(final Long memberId) {
        return memberMoneyRepository.findByMemberIdForUpdate(memberId)
                .orElseThrow(() -> new BadRequestException(BadRequestCode.MEMBER_NOT_FOUND));
    }
}
