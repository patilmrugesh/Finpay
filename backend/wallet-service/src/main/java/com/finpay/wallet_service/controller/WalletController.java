package com.finpay.wallet_service.controller;

import com.finpay.wallet_service.dto.*;
import com.finpay.wallet_service.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/create")
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody WalletCreateRequest request) {
        WalletResponse response = walletService.createWallet(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable UUID userId) {
        BalanceResponse response = walletService.getBalance(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/deposit")
    public ResponseEntity<TransactionResponse> deposit(@PathVariable UUID userId, @Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = walletService.deposit(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@PathVariable UUID userId, @Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = walletService.withdraw(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/transactions")
    public ResponseEntity<List<TransactionHistoryResponse>> getTransactions(@PathVariable UUID userId) {
        List<TransactionHistoryResponse> response = walletService.getTransactionHistory(userId);
        return ResponseEntity.ok(response);
    }
}
