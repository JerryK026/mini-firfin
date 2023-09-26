package com.soko.minifirfin.application;

import com.soko.minifirfin.common.exception.BadRequestException;
import com.soko.minifirfin.domain.Member;
import com.soko.minifirfin.domain.MemberMoney;
import com.soko.minifirfin.domain.Money;
import com.soko.minifirfin.repository.MemberMoneyRepository;
import com.soko.minifirfin.repository.MemberRepository;
import com.soko.minifirfin.repository.RechargeHistoryRepository;
import com.soko.minifirfin.ui.response.RechargeResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.soko.minifirfin.common.exception.BadRequestCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
class RechargeServiceTest {

    @Autowired
    private RechargeService rechargeService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberMoneyRepository memberMoneyRepository;
    @Autowired
    private RechargeHistoryRepository rechargeHistoryRepository;

    /* Biz */
    @DisplayName("충전 성공 : 충전 요청한 만큼 잔액이 추가된다")
    @Test
    void recharge_happyCase() {
        Member member = memberRepository.save(new Member("recharger", 50_000, 10_000));

        RechargeResponse rechargeResponse = rechargeService.recharge(member.getId(), new Money(30_000));

        MemberMoney memberMoney = memberMoneyRepository.findByMemberId(member.getId()).get();

        assertThat(rechargeHistoryRepository.findById(rechargeResponse.rechargeHistoryId())).isNotNull();
        assertThat(memberMoney.getMoneyAmountAsBigDecimal()).isEqualTo(BigDecimal.valueOf(10_000 + 30_000));
    }

    @DisplayName("충전 실패 : 사용자 한도가 초과된다")
    @Test
    void recharge_reject_overMemberLimitation() {
        Member member = memberRepository.save(new Member("recharger", 50_000, 10_000));

        assertThatThrownBy(() -> rechargeService.recharge(member.getId(), new Money(50_000)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(RECHARGER_OVER_LIMITATION.getMessage());
    }

    @DisplayName("충전 실패 : 사용자 일일 한도가 초과된다")
    @Test
    void recharge_reject_overDailyLimitation() {
        Member member = memberRepository.save(new Member("recharger", 100_000_000, 0));

        for (int i = 0; i < 5; i++) {
            rechargeService.recharge(member.getId(), new Money(5_000_000));
        }

        assertThatThrownBy(() -> rechargeService.recharge(member.getId(), new Money(5_000_000)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(RECHARGE_OVER_DAILY_LIMITATION.getMessage());
    }

    @DisplayName("충전 실패 : 1회 충전 한도가 초과된다")
    @Test
    void recharge_reject_overOnceLimitation() {
        Member member = memberRepository.save(new Member("recharger", 100_000_000, 0));

        assertThatThrownBy(() -> rechargeService.recharge(member.getId(), new Money(6_000_000)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(RECHARGE_OVER_ONCE_LIMITATION.getMessage());
    }

    /* Tech */
    @DisplayName("충전 기술 : 충전 요청이 동시에 수행되어도 동시쓰기 현상이 발생되어선 안 된다")
    @Test
    void transfer_tech_concurrency() throws InterruptedException {
        int moneyAmount = 1_000;
        int threadCount = 50;

        ExecutorService service = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // given
        Member member = memberRepository.save(new Member("recharger", 100_000_000, 0));

        // when
        for (int i = 0; i < threadCount; i++) {
            service.submit(() -> {
                try {
                    rechargeService.recharge(member.getId(), new Money(moneyAmount));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        service.shutdownNow();

        MemberMoney memberMoney = memberMoneyRepository.findByMemberId(member.getId()).get();
        // then
        assertThat(memberMoney.getMoneyAmountAsBigDecimal()).isEqualTo(BigDecimal.valueOf(moneyAmount * threadCount));
    }
}