package com.soko.minifirfin.repository;

import com.soko.minifirfin.domain.MemberMoney;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberMoneyRepository extends JpaRepository<MemberMoney, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MemberMoney m where m.member.id = :memberId")
    Optional<MemberMoney> findByMemberIdForUpdate(@Param("memberId") Long memberId);

    Optional<MemberMoney> findByMemberId(Long memberId);
}
