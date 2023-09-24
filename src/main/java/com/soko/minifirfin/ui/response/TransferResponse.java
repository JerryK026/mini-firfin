package com.soko.minifirfin.ui.response;

import com.soko.minifirfin.common.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
        String senderName,
        String receiverName,
        Long transferHistoryId,
        BigDecimal transferredMoneyAmount,
        BigDecimal RemainMoneyAmountOfSender,
        LocalDateTime timeStamp,
        Status status
) { }
