package com.soko.minifirfin.domain;

import com.soko.minifirfin.common.AuditingEntity;
import com.soko.minifirfin.MoneyConverter;
import jakarta.persistence.*;

@Entity
public class RechargeHistory extends AuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long memberId;
    @Convert(converter = MoneyConverter.class)
    private Money rechargeAmount;
    @Convert(converter = MoneyConverter.class)
    private Money remainAmount;

    public RechargeHistory() {
    }

    public RechargeHistory(Long id, Long memberId, Money rechargeAmount, Money remainAmount) {
        this.id = id;
        this.memberId = memberId;
        this.rechargeAmount = rechargeAmount;
        this.remainAmount = remainAmount;
    }

    public RechargeHistory(Long memberId, Money rechargeAmount, Money remainAmount) {
        this(null, memberId, rechargeAmount, remainAmount);
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Money getRechargeAmount() {
        return rechargeAmount;
    }

    public Money getRemainAmount() {
        return remainAmount;
    }
}
