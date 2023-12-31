package com.soko.minifirfin.application;

import com.soko.minifirfin.common.Status;
import com.soko.minifirfin.common.exception.BadRequestCode;
import com.soko.minifirfin.common.exception.BadRequestException;
import com.soko.minifirfin.domain.Member;
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

    public RechargeService(
            final RechargeHistoryRepository rechargeHistoryRepository,
            final MemberMoneyRepository memberMoneyRepository
    ) {
        this.rechargeHistoryRepository = rechargeHistoryRepository;
        this.memberMoneyRepository = memberMoneyRepository;
    }

    @Transactional
    public RechargeResponse recharge(final Long memberId, final Money rechargeAmount, final String memberIpAddress) {
        if (rechargeAmount.isOverThan(new Money(RECHARGE_ONCE_LIMITATION))) {
            throw new BadRequestException(RECHARGE_OVER_ONCE_LIMITATION);
        }

        validateDailyRechargeLimitation(memberId, rechargeAmount);

        MemberMoney memberMoney = findMemberMoneyByMemberIdForUpdate(memberId);
        memberMoney.recharge(rechargeAmount);
        Member member = memberMoney.getMember();

        RechargeHistory rechargeHistory = rechargeHistoryRepository.save(new RechargeHistory(
                memberId,
                rechargeAmount,
                memberMoney.getMoneyAmount(),
                memberIpAddress,
                member.getEmail(),
                member.getPhoneNumber(),
                memberMoney.getPaymentMethod(),
                memberMoney.getPaymentInfo(),
                member.getSerialNumber()
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
    private void validateDailyRechargeLimitation(final Long memberId, final Money rechargeAmount) {
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
