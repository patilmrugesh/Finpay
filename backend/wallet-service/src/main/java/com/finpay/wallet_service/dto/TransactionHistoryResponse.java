package com.finpay.wallet_service.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionHistoryResponse(
        UUID id,
        String transactionType,
        BigDecimal amount,
        UUID counterpartyId,
        String description,
        String referenceId,
        Instant timestamp
) {}
