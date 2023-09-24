package com.soko.minifirfin.ui.request;

import java.math.BigDecimal;

public record TransferRequest(
    Long receiverId,
    BigDecimal amount
) {}
