-- Drop tables if exist
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS products;

-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    roles VARCHAR(255) NOT NULL DEFAULT 'ROLE_USER',
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create products table
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample users (password is 'password123' hashed with BCrypt)
INSERT INTO users (username, password, email, roles, enabled) VALUES
('admin', '$2a$10$slYQm3mCpNaLgN1Pv.JZ9.xYGYPHHIv1Hm6DpqNO.lNQJa2j7M9h2', 'admin@example.com', 'ROLE_USER,ROLE_ADMIN', true),
('user', '$2a$10$slYQm3mCpNaLgN1Pv.JZ9.xYGYPHHIv1Hm6DpqNO.lNQJa2j7M9h2', 'user@example.com', 'ROLE_USER', true);

-- Insert sample products
INSERT INTO products (name, description, price, quantity) VALUES
('Laptop', 'High-performance laptop for developers', 1299.99, 10),
('Mouse', 'Wireless ergonomic mouse', 29.99, 50),
('Keyboard', 'Mechanical keyboard with RGB', 89.99, 30),
('Monitor', '27-inch 4K display', 399.99, 15),
('Headphones', 'Noise-cancelling wireless headphones', 199.99, 25);
