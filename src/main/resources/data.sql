-- ============================================
-- PRODUCTS
-- ============================================

INSERT INTO product (id, name, price, stock)
VALUES (1, 'Laptop', 1299.99, 10);

INSERT INTO product (id, name, price, stock)
VALUES (2, 'Keyboard', 89.90, 25);

INSERT INTO product (id, name, price, stock)
VALUES (3, 'Mouse', 49.90, 30);

INSERT INTO product (id, name, price, stock)
VALUES (4, 'Monitor', 399.99, 15);


-- ============================================
-- CUSTOMERS
-- ============================================

INSERT INTO customer (id, first_name, last_name)
VALUES (1, 'Jack', 'Bauer');

INSERT INTO customer (id, first_name, last_name)
VALUES (2, 'Chloe', 'O''Brian');

INSERT INTO customer (id, first_name, last_name)
VALUES (3, 'David', 'Palmer');

INSERT INTO customer (id, first_name, last_name)
VALUES (4, 'Michelle', 'Dessler');


-- ============================================
-- ORDERS
-- ============================================

INSERT INTO customer_order
(id, customer_id, product_id, quantity, unit_price, created_at, status)
VALUES
    (1, 1, 1, 1, 1299.99, '2026-10-01 10:30:00', 'DELIVERED');

INSERT INTO customer_order
(id, customer_id, product_id, quantity, unit_price, created_at, status)
VALUES
    (2, 1, 3, 2, 49.90, '2026-10-02 14:15:00', 'SHIPPED');

INSERT INTO customer_order
(id, customer_id, product_id, quantity, unit_price, created_at, status)
VALUES
    (3, 2, 2, 1, 89.90, '2026-10-03 09:45:00', 'PAID');

INSERT INTO customer_order
(id, customer_id, product_id, quantity, unit_price, created_at, status)
VALUES
    (4, 3, 4, 2, 399.99, '2026-10-04 16:20:00', 'CREATED');

INSERT INTO customer_order
(id, customer_id, product_id, quantity, unit_price, created_at, status)
VALUES
    (5, 4, 3, 1, 49.90, '2026-10-05 11:00:00', 'CANCELLED');


-- ============================================
-- RESET GENERATED IDs
-- ============================================

ALTER TABLE product ALTER COLUMN id RESTART WITH 5;
ALTER TABLE customer ALTER COLUMN id RESTART WITH 5;
ALTER TABLE customer_order ALTER COLUMN id RESTART WITH 6;