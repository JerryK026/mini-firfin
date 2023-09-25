package com.soko.minifirfin.repository;

import com.soko.minifirfin.domain.TransferHistory;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class TransferHistoryRepositoryImpl implements TransferHistoryCustomRepository {

    private final EntityManager entityManager;

    public TransferHistoryRepositoryImpl(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<TransferHistory> findBySenderIdOrderByCreatedDateTimeDescLimitWithCursor(
        Long senderId,
        Long cursor,
        int limit
    ) {
        return entityManager.createQuery(
                """
                    FROM TransferHistory th
                    WHERE th.sender.id = :senderId
                        AND th.id < :cursor
                    ORDER BY th.createdDateTime DESC
                    """, TransferHistory.class
            )
            .setParameter("senderId", senderId)
            .setParameter("cursor", cursor)
            .setMaxResults(limit)
            .getResultList();
    }

    // 분기가 많이 나뉘지 않는데 굳이 복잡한 동적쿼리로 짤 이유는 없다고 생각해 오버라이딩으로 해결
    // => 분기가 3개 이상으로 나뉘면 그때 동적쿼리 고려
    public List<TransferHistory> findBySenderIdOrderByCreatedDateTimeDescLimit(
        Long senderId,
        int limit
    ) {
        return entityManager.createQuery(
                """
                    FROM TransferHistory th
                    WHERE th.sender.id = :senderId
                    ORDER BY th.createdDateTime DESC
                    """, TransferHistory.class
            )
            .setParameter("senderId", senderId)
            .setMaxResults(limit)
            .getResultList();
    }
}
