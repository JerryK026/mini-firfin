package com.soko.minifirfin.repository;

import com.soko.minifirfin.domain.TransferHistory;
import java.util.List;

public interface TransferHistoryCustomRepository {

    List<TransferHistory> findBySenderIdOrderByCreatedDateTimeDescLimitWithCursor(
        Long senderId,
        Long cursor,
        int limit
    );

    List<TransferHistory> findBySenderIdOrderByCreatedDateTimeDescLimit(
        Long senderId,
        int limit
    );
}
