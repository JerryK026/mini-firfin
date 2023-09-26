package com.soko.minifirfin.domain;

import com.soko.minifirfin.common.exception.BadRequestException;

import java.math.BigDecimal;

import static com.soko.minifirfin.common.exception.BadRequestCode.*;


public class Money {
    private BigDecimal amount;
    // TODO: enum으로 뽑아야함
    private String currency;

    public Money() {
    }

    public Money(final BigDecimal amount) {
        this(amount, "KRW");
    }

    public Money(final int amount) {
        this(BigDecimal.valueOf(amount), "KRW");
    }

    public Money(final int amount, final String currency) {
        this(BigDecimal.valueOf(amount), currency);
    }

    public Money(final BigDecimal amount, final String currency) {
        validate(amount);
        this.amount = amount;
        this.currency = currency;
    }

    public boolean isOverThan(final Money target) {
        if (!this.currency.equals(target.currency)) {
            throw new BadRequestException(DIFFERENT_CURRENCY);
        }

        return this.amount.compareTo(target.amount) > 0;
    }

    public void transferTo(final Money target, final int sendAmount) {
        transferTo(target, new Money(sendAmount));
    }

    public void transferTo(final Money target, final Money sendAmount) {
        if (this.amount.compareTo(sendAmount.amount) < 0) {
            throw new BadRequestException(NOT_ENOUGH_MONEY);
        }

        // TODO: 애플리케이션 단순화를 위해 현재 통화가 다르면 계산할 수 없게 막은 상태
        if (areContainsDifferentCurrency(target, sendAmount)) {
            throw new BadRequestException(DIFFERENT_CURRENCY);
        }

        target.amount = target.amount.add(sendAmount.amount);
        this.amount = this.amount.subtract(sendAmount.amount);
    }

    public Money add(final Money target) {
        return new Money(this.amount.add(target.amount));
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    @Override
    public String toString() {
        return this.amount + this.currency;
    }

    private boolean areContainsDifferentCurrency(final Money target, final Money sendAmount) {
        return !this.currency.equals(sendAmount.currency) || !this.currency.equals(target.currency) || !target.currency.equals(sendAmount.currency);
    }

    private void validate(final BigDecimal target) {
        if (target.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(NEGATIVE_MONEY_NOT_ALLOWED);
        }
    }
}
