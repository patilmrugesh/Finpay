package com.finpay.wallet_service.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletResponse(
        UUID id,
        UUID userId,
        BigDecimal balance,
        String currency,
        String status
) {}
