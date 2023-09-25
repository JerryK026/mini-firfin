package com.soko.minifirfin.ui.response;

import com.soko.minifirfin.common.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RechargeResponse(
        Long rechargeHistoryId,
        Long memberId,
        String memberName,
        BigDecimal currentMoneyAmount,
        BigDecimal rechargedMoneyAmount,
        LocalDateTime timeStamp,
        Status status
) { }
