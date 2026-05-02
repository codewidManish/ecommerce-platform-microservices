-- E-Commerce Platform - MySQL Init (MySQL 8 compatible)

CREATE DATABASE IF NOT EXISTS user_db      CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS product_db   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS order_db     CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS payment_db   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS inventory_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON user_db.*      TO 'ecommerce'@'%';
GRANT ALL PRIVILEGES ON product_db.*   TO 'ecommerce'@'%';
GRANT ALL PRIVILEGES ON order_db.*     TO 'ecommerce'@'%';
GRANT ALL PRIVILEGES ON payment_db.*   TO 'ecommerce'@'%';
GRANT ALL PRIVILEGES ON inventory_db.* TO 'ecommerce'@'%';
FLUSH PRIVILEGES;

USE user_db;

CREATE TABLE IF NOT EXISTS roles (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);
INSERT IGNORE INTO roles (name) VALUES ('ROLE_CUSTOMER'),('ROLE_ADMIN'),('ROLE_VENDOR'),('ROLE_SUPER_ADMIN');

CREATE TABLE IF NOT EXISTS users (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL UNIQUE,
    email          VARCHAR(100) NOT NULL UNIQUE,
    password       VARCHAR(255) NOT NULL,
    first_name     VARCHAR(50),
    last_name      VARCHAR(50),
    phone_number   VARCHAR(15),
    status         VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    oauth2_provider VARCHAR(30),
    oauth2_id      VARCHAR(100),
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

INSERT IGNORE INTO users (username, email, password, first_name, last_name, status)
VALUES ('admin','admin@shopeasy.com','$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4J/HS.i9oi','System','Admin','ACTIVE');

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u CROSS JOIN roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';

USE product_db;

CREATE TABLE IF NOT EXISTS categories (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL UNIQUE,
    description   VARCHAR(500),
    slug          VARCHAR(120) UNIQUE,
    image_url     VARCHAR(500),
    parent_id     BIGINT,
    is_active     BOOLEAN DEFAULT TRUE,
    display_order INT DEFAULT 0,
    FOREIGN KEY (parent_id) REFERENCES categories(id)
);

INSERT IGNORE INTO categories (name, description, slug, display_order) VALUES
    ('Electronics',       'Electronic devices and accessories',     'electronics',    1),
    ('Clothing',          'Apparel for men, women and kids',        'clothing',       2),
    ('Books',             'Books, eBooks and audiobooks',           'books',          3),
    ('Home & Kitchen',    'Home appliances and kitchen essentials', 'home-kitchen',   4),
    ('Sports & Outdoors', 'Sports equipment and outdoor gear',      'sports-outdoors',5),
    ('Beauty & Health',   'Beauty, grooming and health products',   'beauty-health',  6),
    ('Toys & Games',      'Toys, games and hobby items',            'toys-games',     7),
    ('Automotive',        'Car and bike accessories',               'automotive',     8);

-- Sub-categories (SELECT syntax avoids nested subquery issue in MySQL 8)
INSERT IGNORE INTO categories (name, slug, parent_id)
SELECT 'Smartphones','smartphones', id FROM categories WHERE slug='electronics';
INSERT IGNORE INTO categories (name, slug, parent_id)
SELECT 'Laptops','laptops', id FROM categories WHERE slug='electronics';
INSERT IGNORE INTO categories (name, slug, parent_id)
SELECT 'Audio','audio', id FROM categories WHERE slug='electronics';
INSERT IGNORE INTO categories (name, slug, parent_id)
SELECT 'Televisions','televisions', id FROM categories WHERE slug='electronics';
INSERT IGNORE INTO categories (name, slug, parent_id)
SELECT 'Men''s Wear','mens-wear', id FROM categories WHERE slug='clothing';
INSERT IGNORE INTO categories (name, slug, parent_id)
SELECT 'Women''s Wear','womens-wear', id FROM categories WHERE slug='clothing';
INSERT IGNORE INTO categories (name, slug, parent_id)
SELECT 'Kids'' Wear','kids-wear', id FROM categories WHERE slug='clothing';
