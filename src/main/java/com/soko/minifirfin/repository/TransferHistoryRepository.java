package com.soko.minifirfin.repository;

import com.soko.minifirfin.domain.TransferHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransferHistoryRepository extends JpaRepository<TransferHistory, Long>, TransferHistoryCustomRepository{
    List<TransferHistory> findBySenderId(Long senderId);
}
