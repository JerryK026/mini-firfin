package com.soko.minifirfin.domain;

import com.soko.minifirfin.MoneyConverter;
import com.soko.minifirfin.common.exception.BadRequestException;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;

import static com.soko.minifirfin.common.exception.BadRequestCode.RECEIVER_OVER_LIMITATION;


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

    public void transfer(MemberMoney receiverMoney, int amount) {
        transfer(receiverMoney, new Money(amount));
    }

    public void transfer(MemberMoney receiverMoney, Money amount) {
        if (isOverLimitation(receiverMoney, amount)) {
            throw new BadRequestException(RECEIVER_OVER_LIMITATION);
        }

        this.moneyAmount.transferTo(receiverMoney.getMoneyAmount(), amount);
    }

    public Money getMoneyLimit() {
        return moneyLimit;
    }

    public Money getMoneyAmount() {
        return moneyAmount;
    }

    private boolean isOverLimitation(MemberMoney receiverMoney, Money amount) {
        Money receiverLimit = receiverMoney.getMoneyLimit();
        Money receiverMoneyAfterTransfer = receiverMoney.getMoneyAmount().add(amount);

        return receiverMoneyAfterTransfer.isOverThan(receiverLimit);
    }
}
