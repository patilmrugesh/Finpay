package com.finpay.wallet_service.repository;

import com.finpay.wallet_service.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {
    List<WalletTransaction> findByWalletIdOrderByTimestampDesc(UUID walletId);
}
