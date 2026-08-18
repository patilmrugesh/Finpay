package com.finpay.wallet_service.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID transactionId,
        BigDecimal amount,
        String transactionType,
        BigDecimal newBalance,
        Instant timestamp
) {}
