package com.finpay.wallet_service.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        UUID transactionId,
        UUID senderUserId,
        UUID recipientUserId,
        BigDecimal amount,
        BigDecimal senderRemainingBalance,
        String status,
        Instant timestamp
) {}