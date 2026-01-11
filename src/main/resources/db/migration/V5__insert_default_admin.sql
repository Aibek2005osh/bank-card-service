-- Default admin
-- Username: admin
-- Password: admin123 (BCrypt hashed)
INSERT INTO users (username, password, full_name, email, role, enabled)
VALUES (
           'admin',
           '$2a$10$N9qo8uLOickgx2ZMRZoMye1J7U3pGH8.YV4iVqr4.2JmJE5K8l7Gm',
           'System Administrator',
           'admin@bankcard.com',
           'ADMIN',
           true
       );

-- Test user (опционалдуу)
-- Username: user
-- Password: user123
INSERT INTO users (username, password, full_name, email, role, enabled)
VALUES (
           'user',
           '$2a$10$8ZqY4YNY4rYJZkj5h9tN0.XHxZ8J8PQJ5x9j1J5J1J5J1J5J1J5J1O',
           'Test User',
           'user@bankcard.com',
           'USER',
           true
       );
