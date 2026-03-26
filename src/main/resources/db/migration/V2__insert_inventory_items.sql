INSERT INTO inventory_item (id, sku, quantity, low_stock_threshold)
VALUES
    (gen_random_uuid(), 'SKU-001', 100, 10),
    (gen_random_uuid(), 'SKU-002', 250, 25),
    (gen_random_uuid(), 'SKU-003', 50, 5);
