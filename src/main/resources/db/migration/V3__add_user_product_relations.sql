ALTER TABLE product
ADD COLUMN user_id BIGINT;

ALTER TABLE product
ADD CONSTRAINT fk_user
FOREIGN KEY (user_id) REFERENCES users(user_id);

CREATE INDEX idx_product_user_id ON product(user_id);