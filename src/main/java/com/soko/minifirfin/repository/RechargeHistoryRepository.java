package com.soko.minifirfin.repository;

import com.soko.minifirfin.domain.RechargeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RechargeHistoryRepository extends JpaRepository<RechargeHistory, Long> {
}
