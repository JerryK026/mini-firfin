package com.soko.minifirfin.domain;

import com.soko.minifirfin.common.AuditingEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;

@Entity
@SQLDelete(sql = "UPDATE member SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
//@Table(name = "member", indexes = {
//        @Index(name = "idx__id__deleted", columnList = "id, deleted")
//})
public class Member extends AuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String phoneNumber;
    private String email;
    private LocalDate birthDay;
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "member", cascade = CascadeType.ALL)
    @JoinColumn(name = "member_money_id")
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
        this.memberMoney.setMember(this);
    }

    public Member(String name, int limit, int initialAmount) {
        this(null, name, null, null, null, new MemberMoney(limit, initialAmount));
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
}
