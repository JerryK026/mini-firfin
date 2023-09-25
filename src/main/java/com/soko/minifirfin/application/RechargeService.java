package com.soko.minifirfin.application;

import com.soko.minifirfin.common.exception.BadRequestCode;
import com.soko.minifirfin.common.exception.BadRequestException;
import com.soko.minifirfin.domain.Member;
import com.soko.minifirfin.domain.Money;
import com.soko.minifirfin.repository.MemberRepository;
import com.soko.minifirfin.repository.RechargeHistoryRepository;
import com.soko.minifirfin.ui.response.RechargeResponse;
import org.springframework.stereotype.Service;

import static com.soko.minifirfin.common.exception.BadRequestCode.MEMBER_NOT_FOUND;

@Service
public class RechargeService {
    private final RechargeHistoryRepository  rechargeHistoryRepository;
    private final MemberRepository memberRepository;

    public RechargeService(
            final RechargeHistoryRepository rechargeHistoryRepository,
            final MemberRepository memberRepository
    ) {
        this.rechargeHistoryRepository = rechargeHistoryRepository;
        this.memberRepository = memberRepository;
    }

    public RechargeResponse recharge(Long memberId, Money rechargeAmount) {


        return null;
    }

    private Member findMemberByMemberId(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BadRequestException(MEMBER_NOT_FOUND));
    }
}
