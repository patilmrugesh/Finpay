ALTER TABLE wallet_transactions
DROP CONSTRAINT IF EXISTS wallet_transactions_transaction_type_check;

ALTER TABLE wallet_transactions
ADD CONSTRAINT wallet_transactions_transaction_type_check
CHECK (
    transaction_type IN (
        'DEPOSIT',
        'WITHDRAWAL',
        'TRANSFER_SENT',
        'TRANSFER_RECEIVED'
    )
);
