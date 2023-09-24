package com.soko.minifirfin.domain;

import java.math.BigDecimal;


public class Money {
    private BigDecimal amount;
    // TODO: enum으로 뽑아야함
    private String currency;

    public Money() {}

    public Money(BigDecimal amount) {
        this(amount, "KRW");
    }

    public Money(int amount) {
        this(BigDecimal.valueOf(amount), "KRW");
    }

    public Money(BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    @Override
    public String toString() {
        return this.amount + this.currency;
    }
}
