package com.finpay.wallet_service.service;

import com.finpay.wallet_service.dto.*;
import com.finpay.wallet_service.exception.InsufficientFundsException;
import com.finpay.wallet_service.exception.WalletAlreadyExistsException;
import com.finpay.wallet_service.exception.WalletNotFoundException;
import com.finpay.wallet_service.model.Wallet;
import com.finpay.wallet_service.model.WalletTransaction;
import com.finpay.wallet_service.model.enums.TransactionType;
import com.finpay.wallet_service.model.enums.WalletStatus;
import com.finpay.wallet_service.repository.WalletRepository;
import com.finpay.wallet_service.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    @Transactional
    public WalletResponse createWallet(WalletCreateRequest request) {
        if (walletRepository.findByUserId(request.userId()).isPresent()) {
            throw new WalletAlreadyExistsException("Wallet already exists for user: " + request.userId());
        }

        Wallet wallet = Wallet.builder()
                .userId(request.userId())
                .balance(BigDecimal.ZERO)
                .currency("INR")
                .status(WalletStatus.ACTIVE)
                .build();

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Wallet created with ID: {}", savedWallet.getId());

        return mapToResponse(savedWallet);
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(UUID userId) {
        Wallet wallet = getWalletByUserId(userId);
        return new BalanceResponse(wallet.getBalance(), wallet.getCurrency());
    }

    @Transactional
    public TransactionResponse deposit(UUID userId, TransactionRequest request) {
        Wallet wallet = getWalletByUserId(userId);
        
        wallet.setBalance(wallet.getBalance().add(request.amount()));
        Wallet savedWallet = walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .transactionType(TransactionType.DEPOSIT)
                .amount(request.amount())
                .counterpartyId(request.counterpartyId())
                .description(request.description())
                .referenceId(request.referenceId())
                .build();

        WalletTransaction savedTransaction = transactionRepository.save(transaction);

        return new TransactionResponse(
                savedTransaction.getId(),
                savedTransaction.getAmount(),
                savedTransaction.getTransactionType().name(),
                savedWallet.getBalance(),
                savedTransaction.getTimestamp()
        );
    }

    @Transactional
    public TransactionResponse withdraw(UUID userId, TransactionRequest request) {
        Wallet wallet = getWalletByUserId(userId);

        if (wallet.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("insufficient fund");
        }

        wallet.setBalance(wallet.getBalance().subtract(request.amount()));
        Wallet savedWallet = walletRepository.save(wallet);

        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(wallet.getId())
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(request.amount())
                .counterpartyId(request.counterpartyId())
                .description(request.description())
                .referenceId(request.referenceId())
                .build();

        WalletTransaction savedTransaction = transactionRepository.save(transaction);

        return new TransactionResponse(
                savedTransaction.getId(),
                savedTransaction.getAmount(),
                savedTransaction.getTransactionType().name(),
                savedWallet.getBalance(),
                savedTransaction.getTimestamp()
        );
    }

    @Transactional(readOnly = true)
    public List<TransactionHistoryResponse> getTransactionHistory(UUID userId) {
        Wallet wallet = getWalletByUserId(userId);
        List<WalletTransaction> transactions = transactionRepository.findByWalletIdOrderByTimestampDesc(wallet.getId());

        return transactions.stream().map(tx -> new TransactionHistoryResponse(
                tx.getId(),
                tx.getTransactionType().name(),
                tx.getAmount(),
                tx.getCounterpartyId(),
                tx.getDescription(),
                tx.getReferenceId(),
                tx.getTimestamp()
        )).collect(Collectors.toList());
    }

    private Wallet getWalletByUserId(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("No wallet found for user {}. Auto-initializing default wallet.", userId);
                    Wallet newWallet = Wallet.builder()
                            .userId(userId)
                            .balance(BigDecimal.ZERO)
                            .currency("INR")
                            .status(WalletStatus.ACTIVE)
                            .build();
                    return walletRepository.save(newWallet);
                });
    }

    private WalletResponse mapToResponse(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getUserId(),
                wallet.getBalance(),
                wallet.getCurrency(),
                wallet.getStatus().name()
        );
    }
}
