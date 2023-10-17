package com.soko.minifirfin.ui.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.soko.minifirfin.domain.TransferHistory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("transfer:historyResponse")
public record TransferHistoryResponse(
    long transferHistoryId,
    @Id
    long senderId,
    long receiverId,
    String senderName,
    String receiverName,
    BigDecimal sendAmount,
    BigDecimal senderRemainAmount,
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
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
