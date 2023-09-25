package com.soko.minifirfin.domain;

import com.soko.minifirfin.common.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.soko.minifirfin.common.exception.BadRequestCode.NOT_ENOUGH_MONEY;
import static com.soko.minifirfin.common.exception.BadRequestCode.RECEIVER_OVER_LIMITATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberMoneyTest {


    @Test
    @DisplayName("transfer 성공")
    void transfer() {
        MemberMoney senderMoney = new MemberMoney(50000, 10000);
        MemberMoney receiverMoney = new MemberMoney(50000, 0);

        senderMoney.transfer(receiverMoney, 10000);

        assertThat(senderMoney.getMoneyAmount().getAmount()).isEqualTo(BigDecimal.valueOf(0));
        assertThat(receiverMoney.getMoneyAmount().getAmount()).isEqualTo(BigDecimal.valueOf(10000));
    }

    @Test
    @DisplayName("transfer 실패 : 송금자 잔액 부족")
    void transfer_fail_senderNoeEnoughMoney() {
        MemberMoney senderMoney = new MemberMoney(50000, 5000);
        MemberMoney receiverMoney = new MemberMoney(50000, 0);

        assertThatThrownBy(() -> senderMoney.transfer(receiverMoney, 10000))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(NOT_ENOUGH_MONEY.getMessage());
    }

    @Test
    @DisplayName("transfer 실패 : 수금자 한도 초과")
    void transfer_fail_receiverOverLimitation() {
        MemberMoney senderMoney = new MemberMoney(50000, 10000);
        MemberMoney receiverMoney = new MemberMoney(50000, 40001);

        assertThatThrownBy(() -> senderMoney.transfer(receiverMoney, 10000))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(RECEIVER_OVER_LIMITATION.getMessage());
    }
}