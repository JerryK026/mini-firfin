package com.soko.minifirfin.repository;

import com.soko.minifirfin.domain.TransferHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransferHistoryRepository extends JpaRepository<TransferHistory, Long>, TransferHistoryCustomRepository{
    List<TransferHistory> findBySenderId(Long senderId);

    List<TransferHistory> findTransferHistoriesByCreatedDateTimeBetweenAndSenderId(
            final LocalDateTime start,
            final LocalDateTime end,
            final Long senderId
    );
}
