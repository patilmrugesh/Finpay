package com.finpay.wallet_service.dto;

import java.math.BigDecimal;

public record BalanceResponse(
        BigDecimal balance,
        String currency
) {}
