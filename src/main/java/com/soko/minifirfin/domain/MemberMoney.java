package com.soko.minifirfin.domain;

import com.soko.minifirfin.MoneyConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;


@Embeddable
public class MemberMoney {
    @Convert(converter = MoneyConverter.class)
    private Money moneyLimit;
    @Convert(converter = MoneyConverter.class)
    private Money moneyAmount;

    public MemberMoney() {}

    public MemberMoney(Money moneyLimit, Money moneyAmount) {
        this.moneyLimit = moneyLimit;
        this.moneyAmount = moneyAmount;
    }

    public MemberMoney(int moneyLimit, int moneyAmount) {
        this(new Money(moneyLimit), new Money(moneyAmount));
    }

    public BigDecimal getMoneyAmount() {
        return moneyAmount.getAmount();
    }
}
