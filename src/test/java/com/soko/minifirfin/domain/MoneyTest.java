package com.soko.minifirfin.domain;

import com.soko.minifirfin.common.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.soko.minifirfin.common.exception.BadRequestCode.DIFFERENT_CURRENCY;
import static com.soko.minifirfin.common.exception.BadRequestCode.NOT_ENOUGH_MONEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void isOver_true() {
        Money sender = new Money(100);
        Money receiver = new Money(0);

        assertThat(sender.isOverThan(receiver)).isTrue();
    }

    @Test
    void isOver_false() {
        Money sender = new Money(0);
        Money receiver = new Money(100);

        assertThat(sender.isOverThan(receiver)).isFalse();
    }

    @Test
    void transferTo() {
        Money sender = new Money(100);
        Money receiver = new Money(0);

        sender.transferTo(receiver, 100);

        assertThat(sender.getAmount()).isEqualTo(new BigDecimal(0));
        assertThat(receiver.getAmount()).isEqualTo(new BigDecimal(100));
    }

    @Test
    void transferTo_fail_notEnoughMoney() {
        Money sender = new Money(100);
        Money receiver = new Money(0);

        assertThatThrownBy(() -> sender.transferTo(receiver, 101))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(NOT_ENOUGH_MONEY.getMessage());
    }

    @Test
    void transferTo_fail_differentCurrency() {
        Money sender = new Money(100, "KRW");
        Money receiver = new Money(0, "USD");

        assertThatThrownBy(() -> sender.transferTo(receiver, 100))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(DIFFERENT_CURRENCY.getMessage());
    }
}