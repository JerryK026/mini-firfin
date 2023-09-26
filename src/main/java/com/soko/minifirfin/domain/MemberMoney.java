package com.soko.minifirfin.domain;

import com.soko.minifirfin.MoneyConverter;
import com.soko.minifirfin.common.AuditingEntity;
import com.soko.minifirfin.common.exception.BadRequestException;
import jakarta.persistence.*;

import java.math.BigDecimal;

import static com.soko.minifirfin.common.exception.BadRequestCode.*;


@Entity
public class MemberMoney extends AuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 로직에서 어차피 이름을 항상 읽어오기 때문에 EAGER로 설정
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "member_id")
    private Member member;
    @Convert(converter = MoneyConverter.class)
    private Money moneyLimit;
    @Convert(converter = MoneyConverter.class)
    private Money moneyAmount;

    public MemberMoney() {}

    public MemberMoney(Long id, Member member, Money moneyLimit, Money moneyAmount) {
        this.id = id;
        this.member = member;
        this.moneyLimit = moneyLimit;
        this.moneyAmount = moneyAmount;
    }

    public MemberMoney(int moneyLimit, int moneyAmount) {
        this(null, null, new Money(moneyLimit), new Money(moneyAmount));
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

    public void recharge(Money moneyForAddition) {
        if (isOverLimitation(moneyForAddition)) {
            throw new BadRequestException(RECHARGER_OVER_LIMITATION);
        }

        this.moneyAmount = this.moneyAmount.add(moneyForAddition);
    }

    public BigDecimal getMoneyAmountAsBigDecimal() {
        return moneyAmount.getAmount();
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Money getMoneyLimit() {
        return moneyLimit;
    }

    public Money getMoneyAmount() {
        return moneyAmount;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    private boolean isOverLimitation(MemberMoney receiverMoney, Money amount) {
        Money receiverLimit = receiverMoney.getMoneyLimit();
        Money receiverMoneyAfterTransfer = receiverMoney.getMoneyAmount().add(amount);

        return receiverMoneyAfterTransfer.isOverThan(receiverLimit);
    }

    private boolean isOverLimitation(Money addAmount) {
        Money addedMoney = this.moneyAmount.add(addAmount);

        return addedMoney.isOverThan(this.moneyLimit);
    }
}
