package com.finpay.wallet_service.repository;

import com.finpay.wallet_service.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {
    Page<WalletTransaction> findByWalletIdOrderByTimestampDesc(UUID walletId, Pageable pageable);
    
    boolean existsByReferenceId(String referenceId);
}
