package com.soko.minifirfin.domain;

import com.soko.minifirfin.MoneyConverter;
import com.soko.minifirfin.common.AuditingEntity;
import com.soko.minifirfin.common.exception.BadRequestException;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;

import static com.soko.minifirfin.common.exception.BadRequestCode.*;


@Entity
@SQLDelete(sql = "UPDATE member_money SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
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
    // 프로젝트 단순화를 위해 default 값으로 고정한다.
    private String paymentMethod = "Credit Card";
    private String paymentInfo = "352-0660-1234-12";

    public MemberMoney() {}

    public MemberMoney(final Long id, final Member member, final Money moneyLimit, final Money moneyAmount) {
        this.id = id;
        this.member = member;
        this.moneyLimit = moneyLimit;
        this.moneyAmount = moneyAmount;
    }

    public MemberMoney(final int moneyLimit, final int moneyAmount) {
        this(null, null, new Money(moneyLimit), new Money(moneyAmount));
    }

    public void transfer(final MemberMoney receiverMoney, final int amount) {
        transfer(receiverMoney, new Money(amount));
    }

    public void transfer(final MemberMoney receiverMoney, final Money amount) {
        if (isOverLimitation(receiverMoney, amount)) {
            throw new BadRequestException(RECEIVER_OVER_LIMITATION);
        }

        this.moneyAmount.transferTo(receiverMoney.getMoneyAmount(), amount);
    }

    public void recharge(final Money moneyForAddition) {
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentInfo() {
        return paymentInfo;
    }

    // 양방향 관계이기 때문에 부모 객체인 Member에서 자동으로 생성하기 위해 추가함
    public void setMember(final Member member) {
        this.member = member;
    }

    private boolean isOverLimitation(final MemberMoney receiverMoney, final Money amount) {
        Money receiverLimit = receiverMoney.getMoneyLimit();
        Money receiverMoneyAfterTransfer = receiverMoney.getMoneyAmount().add(amount);

        return receiverMoneyAfterTransfer.isOverThan(receiverLimit);
    }

    private boolean isOverLimitation(final Money addAmount) {
        Money addedMoney = this.moneyAmount.add(addAmount);

        return addedMoney.isOverThan(this.moneyLimit);
    }
}
