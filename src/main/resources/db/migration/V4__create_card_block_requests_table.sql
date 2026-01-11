CREATE TABLE card_block_requests (
                                     id BIGSERIAL PRIMARY KEY,
                                     card_id BIGINT NOT NULL,
                                     user_id BIGINT NOT NULL,
                                     reason VARCHAR(500) NOT NULL,
                                     status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     processed_at TIMESTAMP,
                                     processed_by BIGINT,
                                     admin_comment TEXT,
                                     CONSTRAINT fk_block_requests_card FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
                                     CONSTRAINT fk_block_requests_user FOREIGN KEY (user_id) REFERENCES users(id),
                                     CONSTRAINT fk_block_requests_processed_by FOREIGN KEY (processed_by) REFERENCES users(id)
);

CREATE INDEX idx_block_requests_status ON card_block_requests(status);
CREATE INDEX idx_block_requests_user ON card_block_requests(user_id);
CREATE INDEX idx_block_requests_card ON card_block_requests(card_id);

COMMENT ON TABLE card_block_requests IS 'Карта блокировкасына өтүнүчтөр';
COMMENT ON COLUMN card_block_requests.status IS 'PENDING, APPROVED, REJECTED';