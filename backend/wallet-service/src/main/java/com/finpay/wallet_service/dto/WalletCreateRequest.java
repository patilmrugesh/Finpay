package com.finpay.wallet_service.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record WalletCreateRequest(
        @NotNull(message = "User ID is required")
        UUID userId
) {}
