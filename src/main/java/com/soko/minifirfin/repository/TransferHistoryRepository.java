package com.soko.minifirfin.repository;

import com.soko.minifirfin.domain.TransferHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferHistoryRepository extends JpaRepository<TransferHistory, Long> {
}
