package com.soko.minifirfin.domain;

import com.soko.minifirfin.common.AuditingEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;

@Entity
@SQLDelete(sql = "UPDATE member SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Member extends AuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", length = 30)
    private String name;
    @Column(name = "phone_number", length = 11)
    private String phoneNumber;
    @Column(name = "email", length = 254)
    private String email;
    private LocalDate birthDay;
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "member", cascade = CascadeType.ALL)
    @JoinColumn(name = "member_money_id")
    private MemberMoney memberMoney;
    // 프로젝트 단순화를 위해 default 값으로 고정한다.
    private String serialNumber = "1234567890";

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

    public String getSerialNumber() {
        return serialNumber;
    }
}
