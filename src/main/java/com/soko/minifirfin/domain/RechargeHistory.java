package com.soko.minifirfin.domain;

import com.soko.minifirfin.common.AuditingEntity;
import com.soko.minifirfin.MoneyConverter;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@SQLDelete(sql = "UPDATE recharge_history SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
//@Table(name = "recharge_history", indexes = {
//        @Index(name = "idx__id__deleted", columnList = "id, deleted")
//})
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

    public RechargeHistory(final Long id, final Long memberId, final Money rechargeAmount, final Money remainAmount) {
        this.id = id;
        this.memberId = memberId;
        this.rechargeAmount = rechargeAmount;
        this.remainAmount = remainAmount;
    }

    public RechargeHistory(final Long memberId, final Money rechargeAmount, final Money remainAmount) {
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
