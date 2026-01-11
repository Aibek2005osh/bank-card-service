CREATE TABLE cards (
                       id BIGSERIAL PRIMARY KEY,
                       encrypted_card_number VARCHAR(500) NOT NULL UNIQUE,
                       masked_card_number VARCHAR(19) NOT NULL,
                       owner_id BIGINT NOT NULL,
                       expiry_date DATE NOT NULL,
                       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                       balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
                       encrypted_cvv VARCHAR(500) NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP,
                       blocked_at TIMESTAMP,
                       CONSTRAINT fk_cards_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_cards_owner ON cards(owner_id);
CREATE INDEX idx_cards_status ON cards(status);

COMMENT ON TABLE cards IS 'Банк карталары';
COMMENT ON COLUMN cards.status IS 'ACTIVE, BLOCKED, EXPIRED';
COMMENT ON COLUMN cards.masked_card_number IS '**** **** **** 1234';