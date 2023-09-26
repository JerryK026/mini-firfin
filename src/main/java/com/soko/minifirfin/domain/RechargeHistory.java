package com.soko.minifirfin.domain;

import com.soko.minifirfin.common.AuditingEntity;
import com.soko.minifirfin.MoneyConverter;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@SQLDelete(sql = "UPDATE recharge_history SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class RechargeHistory extends AuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long memberId;
    @Convert(converter = MoneyConverter.class)
    private Money rechargeAmount;
    @Convert(converter = MoneyConverter.class)
    private Money remainAmount;
    @Column(name = "member_ip_address", length = 15)
    private String memberIpAddress;
    @Column(name = "member_email", length = 254)
    private String memberEmail;
    @Column(name = "member_phone_number", length = 11)
    private String memberPhoneNumber;
    private String memberPaymentMethod;
    private String memberPaymentInfo;
    private String memberSerialNumber;

    public RechargeHistory() {
    }

    public RechargeHistory(
            final Long id,
            final Long memberId,
            final Money rechargeAmount,
            final Money remainAmount,
            final String memberIpAddress,
            final String memberEmail,
            final String memberPhoneNumber,
            final String memberPaymentMethod,
            final String memberPaymentInfo,
            final String memberSerialNumber
    ) {
        this.id = id;
        this.memberId = memberId;
        this.rechargeAmount = rechargeAmount;
        this.remainAmount = remainAmount;
        this.memberIpAddress = memberIpAddress;
        this.memberEmail = memberEmail;
        this.memberPhoneNumber = memberPhoneNumber;
        this.memberPaymentMethod = memberPaymentMethod;
        this.memberPaymentInfo = memberPaymentInfo;
        this.memberSerialNumber = memberSerialNumber;
    }

    public RechargeHistory(
            final Long memberId,
            final Money rechargeAmount,
            final Money remainAmount,
            final String memberIpAddress,
            final String memberEmail,
            final String memberPhoneNumber,
            final String memberPaymentMethod,
            final String memberPaymentInfo,
            final String memberSerialNumber
    ) {
        this(
                null,
                memberId,
                rechargeAmount,
                remainAmount,
                memberIpAddress,
                memberEmail,
                memberPhoneNumber,
                memberPaymentMethod,
                memberPaymentInfo,
                memberSerialNumber
        );
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

    public String getMemberIpAddress() {
        return memberIpAddress;
    }

    public String getSenderEmail() {
        return memberEmail;
    }

    public String getSenderPhoneNumber() {
        return memberPhoneNumber;
    }

    public String getSenderPaymentMethod() {
        return memberPaymentMethod;
    }

    public String getSenderPaymentInfo() {
        return memberPaymentInfo;
    }

    public String getSenderSerialNumber() {
        return memberSerialNumber;
    }
}
