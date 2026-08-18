ALTER TABLE wallet_transactions ADD COLUMN counterparty_id UUID;
ALTER TABLE wallet_transactions ADD COLUMN description VARCHAR(255);
