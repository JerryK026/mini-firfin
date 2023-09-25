package com.soko.minifirfin.repository;

import com.soko.minifirfin.domain.RechargeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RechargeHistoryRepository extends JpaRepository<RechargeHistory, Long> {

    List<RechargeHistory> findRechargeHistoriesByCreatedDateTimeBetweenAndMemberId(
            final LocalDateTime start,
            final LocalDateTime end,
            final Long memberId
    );
}
