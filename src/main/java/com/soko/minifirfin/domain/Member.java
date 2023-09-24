package com.soko.minifirfin.domain;

import com.soko.minifirfin.common.AuditingEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Member extends AuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String phoneNumber;
    private String email;
    private LocalDate birthDay;
    @Embedded
    private MemberMoney memberMoney;

    public Member() {
    }

    public Member(Long id, String name, String phoneNumber, String email, LocalDate birthDay, MemberMoney memberMoney) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.birthDay = birthDay;
        this.memberMoney = memberMoney;
    }

    public Member(String name, int limit, int amount) {
        this(null, name, null, null, null, new MemberMoney(limit, amount));
    }

    public Member(String name, int limit) {
        this(null, name, null, null, null, new MemberMoney(limit, 0));
    }

    public Member(Long id, String name, MemberMoney memberMoney) {
        this(id, name, null, null, null, memberMoney);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getBirthDay() {
        return birthDay;
    }

    public MemberMoney getMemberMoney() {
        return memberMoney;
    }

    public BigDecimal getMoneyAmountAsBigDecimal() {
        return this.memberMoney.getMoneyAmount().getAmount();
    }
}
