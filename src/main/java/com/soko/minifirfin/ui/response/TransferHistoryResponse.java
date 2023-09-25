package com.soko.minifirfin.ui.response;

import com.soko.minifirfin.domain.TransferHistory;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferHistoryResponse(
   long transferHistoryId,
   long senderId,
   long receiverId,
   String senderName,
   String receiverName,
   BigDecimal sendAmount,
   BigDecimal senderRemainAmount,
   LocalDateTime createdDateTime
) {
    public static TransferHistoryResponse of(TransferHistory transferHistory) {
        return new TransferHistoryResponse(
            transferHistory.getId(),
            transferHistory.getSender().getId(),
            transferHistory.getReceiver().getId(),
            transferHistory.getSender().getName(),
            transferHistory.getReceiver().getName(),
            transferHistory.getSendAmount().getAmount(),
            transferHistory.getSenderRemainAmount().getAmount(),
            transferHistory.getCreatedDateTime()
        );
    }
}
