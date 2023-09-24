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
    private Long receiverId;
    @Convert(converter = MoneyConverter.class)
    private Money rechargeAmount;
    @Convert(converter = MoneyConverter.class)
    private Money remainAmount;

    public RechargeHistory() {}

    public RechargeHistory(Long id, Long memberId, Long receiverId, Money rechargeAmount, Money remainAmount) {
        this.id = id;
        this.memberId = memberId;
        this.receiverId = receiverId;
        this.rechargeAmount = rechargeAmount;
        this.remainAmount = remainAmount;
    }
}
