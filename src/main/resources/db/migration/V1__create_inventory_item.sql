CREATE TABLE IF NOT EXISTS inventory_item (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    sku                 VARCHAR(64)     NOT NULL,
    quantity            INT             NOT NULL,
    low_stock_threshold INT             NOT NULL,
    createdat           TIMESTAMPTZ     DEFAULT now(),
    updatedat           TIMESTAMPTZ,

    CONSTRAINT uq_inventory_item_sku        UNIQUE (sku),
    CONSTRAINT chk_quantity_non_negative     CHECK (quantity >= 0),
    CONSTRAINT chk_threshold_non_negative    CHECK (low_stock_threshold >= 0)
);

CREATE INDEX IF NOT EXISTS idx_inventory_item_sku ON inventory_item (sku);
