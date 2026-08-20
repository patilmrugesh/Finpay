package com.finpay.wallet_service.service;

import com.finpay.wallet_service.dto.*;
import com.finpay.wallet_service.exception.DuplicateTransactionException;
import com.finpay.wallet_service.exception.InsufficientFundsException;
import com.finpay.wallet_service.exception.WalletAlreadyExistsException;
import com.finpay.wallet_service.exception.WalletFrozenException;
import com.finpay.wallet_service.model.Wallet;
import com.finpay.wallet_service.model.WalletTransaction;
import com.finpay.wallet_service.model.enums.TransactionType;
import com.finpay.wallet_service.model.enums.WalletStatus;
import com.finpay.wallet_service.repository.WalletRepository;
import com.finpay.wallet_service.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        if (request.referenceId() != null && transactionRepository.existsByReferenceId(request.referenceId())) {
            throw new DuplicateTransactionException("Transaction with reference ID " + request.referenceId() + " already exists");
        }

        Wallet wallet = getWalletByUserIdForUpdate(userId);

        if (wallet.getStatus() == WalletStatus.FROZEN) {
            throw new WalletFrozenException("Wallet is frozen for user: " + userId);
        }
        
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
        if (request.referenceId() != null && transactionRepository.existsByReferenceId(request.referenceId())) {
            throw new DuplicateTransactionException("Transaction with reference ID " + request.referenceId() + " already exists");
        }

        Wallet wallet = getWalletByUserIdForUpdate(userId);

        if (wallet.getStatus() == WalletStatus.FROZEN) {
            throw new WalletFrozenException("Wallet is frozen for user: " + userId);
        }

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
    public Page<TransactionHistoryResponse> getTransactionHistory(UUID userId, int page, int size) {
        Wallet wallet = getWalletByUserId(userId);
        Pageable pageable = PageRequest.of(page, size);
        Page<WalletTransaction> transactions = transactionRepository.findByWalletIdOrderByTimestampDesc(wallet.getId(), pageable);

        return transactions.map(tx -> new TransactionHistoryResponse(
                tx.getId(),
                tx.getTransactionType().name(),
                tx.getAmount(),
                tx.getCounterpartyId(),
                tx.getDescription(),
                tx.getReferenceId(),
                tx.getTimestamp()
        ));
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

    private Wallet getWalletByUserIdForUpdate(UUID userId) {
        return walletRepository.findByUserIdForUpdate(userId)
                .orElseGet(() -> {
                    log.info("No wallet found for user {} for update. Auto-initializing default wallet.", userId);
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
    
    @Transactional
    public TransferResponse transferFunds(UUID senderUserId, TransferRequest request) {
        if (request.referenceId() != null && transactionRepository.existsByReferenceId(request.referenceId())) {
            throw new DuplicateTransactionException("Transfer with reference ID " + request.referenceId() + " already exists");
        }

        if (senderUserId.equals(request.recipientUserId())) {
            throw new IllegalArgumentException("Cannot transfer funds to your own wallet");
        }

        // Consistent ordering prevents database deadlocks under high concurrency
        UUID firstId = senderUserId.compareTo(request.recipientUserId()) < 0 ? senderUserId : request.recipientUserId();
        UUID secondId = senderUserId.compareTo(request.recipientUserId()) < 0 ? request.recipientUserId() : senderUserId;

        Wallet firstWallet = getWalletByUserIdForUpdate(firstId);
        Wallet secondWallet = getWalletByUserIdForUpdate(secondId);

        Wallet senderWallet = senderUserId.equals(firstId) ? firstWallet : secondWallet;
        Wallet recipientWallet = senderUserId.equals(firstId) ? secondWallet : firstWallet;

        if (senderWallet.getStatus() == WalletStatus.FROZEN) {
            throw new WalletFrozenException("Sender wallet is frozen");
        }
        if (recipientWallet.getStatus() == WalletStatus.FROZEN) {
            throw new WalletFrozenException("Recipient wallet is frozen");
        }

        if (senderWallet.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds. Current balance: " + senderWallet.getBalance());
        }

        // Debit sender & Credit recipient
        senderWallet.setBalance(senderWallet.getBalance().subtract(request.amount()));
        recipientWallet.setBalance(recipientWallet.getBalance().add(request.amount()));

        walletRepository.save(senderWallet);
        walletRepository.save(recipientWallet);

        String referenceId = request.referenceId() != null ? request.referenceId() : UUID.randomUUID().toString();

        // Sender transaction record
        WalletTransaction debitTx = WalletTransaction.builder()
                .walletId(senderWallet.getId())
                .transactionType(TransactionType.TRANSFER_SENT)
                .amount(request.amount())
                .counterpartyId(recipientWallet.getId())
                .description(request.description() != null ? request.description() : "Transfer Sent")
                .referenceId(referenceId)
                .build();
        WalletTransaction savedDebit = transactionRepository.save(debitTx);

        // Recipient transaction record
        WalletTransaction creditTx = WalletTransaction.builder()
                .walletId(recipientWallet.getId())
                .transactionType(TransactionType.TRANSFER_RECEIVED)
                .amount(request.amount())
                .counterpartyId(senderWallet.getId())
                .description(request.description() != null ? request.description() : "Transfer Received")
                .referenceId(referenceId)
                .build();
        transactionRepository.save(creditTx);

        return new TransferResponse(
                savedDebit.getId(),
                senderUserId,
                request.recipientUserId(),
                request.amount(),
                senderWallet.getBalance(),
                "COMPLETED",
                savedDebit.getTimestamp()
        );
    }
}
