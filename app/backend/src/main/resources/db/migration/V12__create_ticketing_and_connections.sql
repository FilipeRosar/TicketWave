CREATE TABLE ticket_batches (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL REFERENCES events(id),
    name VARCHAR(120) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    sales_start TIMESTAMPTZ NOT NULL,
    sales_end TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT ck_ticket_batches_price CHECK (price >= 0),
    CONSTRAINT ck_ticket_batches_quantity CHECK (quantity > 0),
    CONSTRAINT ck_ticket_batches_sales_window CHECK (sales_start < sales_end)
);

CREATE INDEX idx_ticket_batches_event ON ticket_batches (event_id);

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    buyer_id UUID NOT NULL REFERENCES users(id),
    total NUMERIC(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT ck_orders_total CHECK (total >= 0),
    CONSTRAINT ck_orders_status CHECK (status IN ('PENDING', 'PAID', 'CANCELED', 'EXPIRED')),
    CONSTRAINT ck_orders_payment_method CHECK (payment_method IN ('PIX', 'CREDIT_CARD'))
);

CREATE INDEX idx_orders_buyer ON orders (buyer_id);

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id),
    batch_id UUID NOT NULL REFERENCES ticket_batches(id),
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT uk_order_items_order_batch UNIQUE (order_id, batch_id),
    CONSTRAINT ck_order_items_quantity CHECK (quantity > 0),
    CONSTRAINT ck_order_items_unit_price CHECK (unit_price >= 0)
);

CREATE INDEX idx_order_items_batch ON order_items (batch_id);

CREATE TABLE tickets (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    batch_id UUID NOT NULL,
    holder_id UUID NOT NULL REFERENCES users(id),
    qr_code VARCHAR(128) NOT NULL,
    status VARCHAR(20) NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT fk_tickets_order_batch FOREIGN KEY (order_id, batch_id)
        REFERENCES order_items (order_id, batch_id),
    CONSTRAINT uk_tickets_qr_code UNIQUE (qr_code),
    CONSTRAINT ck_tickets_status CHECK (status IN ('ISSUED', 'USED', 'CANCELED')),
    CONSTRAINT ck_tickets_used_at CHECK ((status = 'USED') = (used_at IS NOT NULL))
);

CREATE INDEX idx_tickets_order_batch ON tickets (order_id, batch_id);
CREATE INDEX idx_tickets_holder ON tickets (holder_id);

CREATE TABLE connections (
    id UUID PRIMARY KEY,
    requester_id UUID NOT NULL REFERENCES users(id),
    recipient_id UUID NOT NULL REFERENCES users(id),
    source_event_id UUID NOT NULL REFERENCES events(id),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT ck_connections_distinct_users CHECK (requester_id <> recipient_id),
    CONSTRAINT ck_connections_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED'))
);

CREATE UNIQUE INDEX uk_connections_user_pair
    ON connections (LEAST(requester_id, recipient_id), GREATEST(requester_id, recipient_id));

CREATE INDEX idx_connections_requester ON connections (requester_id);
CREATE INDEX idx_connections_recipient ON connections (recipient_id);
CREATE INDEX idx_connections_source_event ON connections (source_event_id);
