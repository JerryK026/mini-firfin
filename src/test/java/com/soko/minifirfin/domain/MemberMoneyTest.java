package com.soko.minifirfin.domain;

import com.soko.minifirfin.common.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static com.soko.minifirfin.common.exception.BadRequestCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberMoneyTest {


    @ParameterizedTest
    @DisplayName("transfer 성공")
    @ValueSource(ints = {10_000, 40_000})
    void transfer(int sendMoneyAmount) {
        MemberMoney senderMoney = new MemberMoney(50_000, sendMoneyAmount);
        MemberMoney receiverMoney = new MemberMoney(50_000, 10_000);

        senderMoney.transfer(receiverMoney, sendMoneyAmount);

        assertThat(senderMoney.getMoneyAmount().getAmount()).isEqualTo(BigDecimal.valueOf(0));
        assertThat(receiverMoney.getMoneyAmount().getAmount()).isEqualTo(BigDecimal.valueOf(10_000 + sendMoneyAmount));
    }

    @Test
    @DisplayName("transfer 실패 : 송금자 잔액 부족")
    void transfer_fail_senderNoeEnoughMoney() {
        MemberMoney senderMoney = new MemberMoney(50_000, 5_000);
        MemberMoney receiverMoney = new MemberMoney(50_000, 0);

        assertThatThrownBy(() -> senderMoney.transfer(receiverMoney, 10_000))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(NOT_ENOUGH_MONEY.getMessage());
    }

    @Test
    @DisplayName("transfer 실패 : 수금자 한도 초과")
    void transfer_fail_receiverOverLimitation() {
        MemberMoney senderMoney = new MemberMoney(50_000, 10_000);
        MemberMoney receiverMoney = new MemberMoney(50_000, 40_001);

        assertThatThrownBy(() -> senderMoney.transfer(receiverMoney, 10_000))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(RECEIVER_OVER_LIMITATION.getMessage());
    }

    @DisplayName("recharge 성공")
    @ParameterizedTest
    @ValueSource(ints = {10_000, 20_000})
    void recharge(int initialMoneyAmount) {
        MemberMoney memberMoney = new MemberMoney(30_000, initialMoneyAmount);
        memberMoney.recharge(new Money(10_000));

        assertThat(memberMoney.getMoneyAmount().getAmount()).isEqualTo(BigDecimal.valueOf(initialMoneyAmount + 10_000));
    }

    @Test
    @DisplayName("recharge 실패 : 개인 한도 초과")
    void add_fail_overPersonalLimitation() {
        MemberMoney memberMoney = new MemberMoney(30_000, 10_000);

        assertThatThrownBy(() -> memberMoney.recharge(new Money(30_000)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(RECHARGER_OVER_LIMITATION.getMessage());
    }
}