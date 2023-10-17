package com.soko.minifirfin.ui.response;

import com.soko.minifirfin.common.Status;
import com.soko.minifirfin.domain.TransferHistory;
import java.util.List;

public record TransferHistoriesResponse(
    List<TransferHistoryResponse> data,
    Boolean isEmpty,
    long cursorId,
    Status status
    ) {
    // cursor가 존재하지 않으면 null이 아닌 -1을 반환하기 때문에 Long이 아니라 long으로 받는다
    // boolean이면 jackson에서 이름을 바꾸기 때문에 Boolean으로 박싱
    public static TransferHistoriesResponse of(
        List<TransferHistory> transferHistories,
        boolean isEmpty,
        long cursorId,
        Status status
    ) {
        return new TransferHistoriesResponse(
            transferHistories.stream().map(TransferHistoryResponse::of).toList(),
            isEmpty,
            cursorId,
            status
        );
    }

    public static TransferHistoriesResponse of(
        List<TransferHistoryResponse> transferHistoryResponses,
        long cursorId,
        Status status
    ) {
        return new TransferHistoriesResponse(
            transferHistoryResponses,
            false,
            cursorId,
            status
        );
    }
}
