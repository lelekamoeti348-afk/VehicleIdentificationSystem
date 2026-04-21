-- Create a new admin user with working password 'admin123'
INSERT INTO users (username, password_hash, full_name, email, phone, role) 
VALUES ('admin', '\\\.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', 'Administrator', 'admin@vis.com', '1234567890', 'ADMIN')
ON CONFLICT (username) DO UPDATE SET password_hash = '\\\.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs';
