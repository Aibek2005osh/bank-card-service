CREATE TABLE transactions (
                              id BIGSERIAL PRIMARY KEY,
                              source_card_id BIGINT NOT NULL,
                              destination_card_id BIGINT NOT NULL,
                              amount DECIMAL(15,2) NOT NULL,
                              status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                              description VARCHAR(500),
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              completed_at TIMESTAMP,
                              error_message TEXT,
                              CONSTRAINT fk_transactions_source FOREIGN KEY (source_card_id) REFERENCES cards(id),
                              CONSTRAINT fk_transactions_destination FOREIGN KEY (destination_card_id) REFERENCES cards(id),
                              CONSTRAINT chk_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_transactions_source ON transactions(source_card_id);
CREATE INDEX idx_transactions_destination ON transactions(destination_card_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_created_at ON transactions(created_at DESC);

COMMENT ON TABLE transactions IS 'Акча которуулар тарыхы';
COMMENT ON COLUMN transactions.status IS 'PENDING, COMPLETED, FAILED, CANCELLED';