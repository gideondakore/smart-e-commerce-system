-- Seed Data for Smart E-Commerce System
-- Sample data for testing and demonstration

-- ============================================
-- USERS (Admin, Manager, Customers)
-- ============================================
INSERT INTO users (email, password_hash, first_name, last_name, role) VALUES
('admin@ecommerce.com', 'hashed_admin_password_123', 'Admin', 'User', 'admin'),
('manager@ecommerce.com', 'hashed_manager_password_123', 'Store', 'Manager', 'manager'),
('john.doe@email.com', 'hashed_password_456', 'John', 'Doe', 'customer'),
('jane.smith@email.com', 'hashed_password_789', 'Jane', 'Smith', 'customer'),
('bob.wilson@email.com', 'hashed_password_012', 'Bob', 'Wilson', 'customer'),
('alice.brown@email.com', 'hashed_password_345', 'Alice', 'Brown', 'customer'),
('charlie.davis@email.com', 'hashed_password_678', 'Charlie', 'Davis', 'customer'),
('diana.miller@email.com', 'hashed_password_901', 'Diana', 'Miller', 'customer');

-- ============================================
-- CATEGORIES
-- ============================================
INSERT INTO categories (name, description) VALUES
('Electronics', 'Electronic devices and gadgets including computers, phones, and accessories'),
('Books', 'Physical and digital books across all genres'),
('Clothing', 'Apparel for men, women, and children'),
('Home & Kitchen', 'Furniture, appliances, and kitchen essentials'),
('Sports & Outdoors', 'Sports equipment and outdoor gear'),
('Beauty & Health', 'Cosmetics, skincare, and health products'),
('Toys & Games', 'Toys, board games, and video games'),
('Office Supplies', 'Stationery and office equipment');

-- ============================================
-- PRODUCTS (Multiple per category for testing)
-- ============================================
-- Electronics
INSERT INTO products (category_id, name, description, price, stock_quantity, sku) VALUES
(1, 'Laptop Pro 15', 'High-performance laptop with 15-inch display, 16GB RAM, 512GB SSD', 1299.99, 25, 'ELEC-LAP-001'),
(1, 'Smartphone X12', 'Latest smartphone with 6.5-inch OLED, 128GB storage', 899.99, 50, 'ELEC-PHN-001'),
(1, 'Wireless Earbuds', 'Premium noise-cancelling wireless earbuds', 199.99, 100, 'ELEC-AUD-001'),
(1, 'Smart Watch Pro', 'Fitness tracking smartwatch with heart rate monitor', 349.99, 40, 'ELEC-WTC-001'),
(1, '4K Monitor 27"', 'Ultra HD monitor with HDR support', 449.99, 30, 'ELEC-MON-001'),
(1, 'Mechanical Keyboard', 'RGB mechanical gaming keyboard', 129.99, 75, 'ELEC-KEY-001'),
(1, 'Wireless Mouse', 'Ergonomic wireless mouse with long battery life', 49.99, 150, 'ELEC-MOU-001'),
(1, 'USB-C Hub', '7-in-1 USB-C hub with HDMI and card reader', 59.99, 200, 'ELEC-HUB-001');

-- Books
INSERT INTO products (category_id, name, description, price, stock_quantity, sku) VALUES
(2, 'Java Programming Masterclass', 'Comprehensive guide to Java programming', 49.99, 100, 'BOOK-PRG-001'),
(2, 'Data Structures & Algorithms', 'Essential algorithms for software developers', 54.99, 80, 'BOOK-PRG-002'),
(2, 'Clean Code', 'A handbook of agile software craftsmanship', 39.99, 120, 'BOOK-PRG-003'),
(2, 'Design Patterns', 'Elements of reusable object-oriented software', 44.99, 60, 'BOOK-PRG-004'),
(2, 'The Art of Fiction', 'Classic guide to creative writing', 24.99, 90, 'BOOK-FIC-001'),
(2, 'World History Encyclopedia', 'Comprehensive world history reference', 79.99, 40, 'BOOK-REF-001');

-- Clothing
INSERT INTO products (category_id, name, description, price, stock_quantity, sku) VALUES
(3, 'Cotton T-Shirt', 'Premium cotton crew neck t-shirt', 19.99, 200, 'CLTH-TSH-001'),
(3, 'Denim Jeans', 'Classic fit denim jeans', 59.99, 150, 'CLTH-JNS-001'),
(3, 'Hoodie Sweatshirt', 'Comfortable pullover hoodie', 44.99, 100, 'CLTH-HOD-001'),
(3, 'Running Shoes', 'Lightweight athletic running shoes', 89.99, 80, 'CLTH-SHO-001'),
(3, 'Winter Jacket', 'Insulated winter jacket with hood', 129.99, 60, 'CLTH-JKT-001'),
(3, 'Sports Cap', 'Adjustable sports baseball cap', 24.99, 250, 'CLTH-CAP-001');

-- Home & Kitchen
INSERT INTO products (category_id, name, description, price, stock_quantity, sku) VALUES
(4, 'Coffee Maker', 'Programmable 12-cup coffee maker', 79.99, 45, 'HOME-COF-001'),
(4, 'Blender Pro', 'High-speed blender with multiple settings', 99.99, 35, 'HOME-BLN-001'),
(4, 'Air Fryer', 'Digital air fryer with 5-quart capacity', 119.99, 50, 'HOME-FRY-001'),
(4, 'Knife Set', 'Professional 8-piece knife block set', 149.99, 30, 'HOME-KNF-001'),
(4, 'Bed Sheet Set', 'Egyptian cotton queen bed sheet set', 69.99, 70, 'HOME-BED-001');

-- Sports & Outdoors
INSERT INTO products (category_id, name, description, price, stock_quantity, sku) VALUES
(5, 'Yoga Mat', 'Non-slip exercise yoga mat', 29.99, 150, 'SPRT-YOG-001'),
(5, 'Dumbbell Set', 'Adjustable dumbbell set 5-25 lbs', 199.99, 40, 'SPRT-DUM-001'),
(5, 'Camping Tent', '4-person waterproof camping tent', 149.99, 25, 'SPRT-TNT-001'),
(5, 'Hiking Backpack', '40L hiking backpack with rain cover', 89.99, 55, 'SPRT-BAG-001'),
(5, 'Bicycle Helmet', 'Adjustable safety bicycle helmet', 49.99, 80, 'SPRT-HLM-001');

-- Beauty & Health
INSERT INTO products (category_id, name, description, price, stock_quantity, sku) VALUES
(6, 'Face Moisturizer', 'Daily hydrating face moisturizer', 34.99, 120, 'BEAU-MOI-001'),
(6, 'Vitamin C Serum', 'Brightening vitamin C face serum', 44.99, 90, 'BEAU-SER-001'),
(6, 'Electric Toothbrush', 'Rechargeable sonic toothbrush', 69.99, 60, 'HEAL-TBR-001'),
(6, 'Protein Powder', 'Whey protein powder 2lb container', 39.99, 100, 'HEAL-PRO-001');

-- Toys & Games
INSERT INTO products (category_id, name, description, price, stock_quantity, sku) VALUES
(7, 'Building Blocks Set', '500-piece creative building blocks', 29.99, 80, 'TOYS-BLK-001'),
(7, 'Board Game Collection', 'Classic family board game collection', 39.99, 50, 'TOYS-BRD-001'),
(7, 'Remote Control Car', 'Off-road RC car with rechargeable battery', 59.99, 45, 'TOYS-RCC-001'),
(7, 'Puzzle 1000pc', '1000-piece landscape jigsaw puzzle', 19.99, 100, 'TOYS-PZL-001');

-- Office Supplies
INSERT INTO products (category_id, name, description, price, stock_quantity, sku) VALUES
(8, 'Notebook Pack', 'Pack of 5 spiral notebooks', 14.99, 200, 'OFFC-NBK-001'),
(8, 'Pen Set', 'Premium ballpoint pen set of 12', 9.99, 300, 'OFFC-PEN-001'),
(8, 'Desk Organizer', 'Multi-compartment desk organizer', 29.99, 80, 'OFFC-ORG-001'),
(8, 'Printer Paper', '500-sheet printer paper ream', 8.99, 500, 'OFFC-PPR-001');

-- ============================================
-- SAMPLE ORDERS
-- ============================================
INSERT INTO orders (user_id, status, total_amount, shipping_address) VALUES
(3, 'delivered', 1349.98, '123 Main St, New York, NY 10001'),
(3, 'shipped', 249.98, '123 Main St, New York, NY 10001'),
(4, 'delivered', 899.99, '456 Oak Ave, Los Angeles, CA 90001'),
(5, 'processing', 179.97, '789 Pine Rd, Chicago, IL 60601'),
(6, 'pending', 89.99, '321 Elm St, Houston, TX 77001'),
(7, 'delivered', 549.97, '654 Maple Dr, Phoenix, AZ 85001');

-- ============================================
-- SAMPLE ORDER ITEMS
-- ============================================
INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase) VALUES
(1, 1, 1, 1299.99),
(1, 7, 1, 49.99),
(2, 3, 1, 199.99),
(2, 7, 1, 49.99),
(3, 2, 1, 899.99),
(4, 15, 3, 19.99),
(4, 21, 2, 44.99),
(4, 24, 1, 24.99),
(5, 22, 1, 89.99),
(6, 4, 1, 349.99),
(6, 3, 1, 199.99);

-- ============================================
-- SAMPLE REVIEWS (Unstructured data candidate)
-- ============================================
INSERT INTO reviews (product_id, user_id, rating, title, comment, is_verified_purchase, helpful_votes) VALUES
(1, 3, 5, 'Excellent Laptop!', 'This laptop exceeded my expectations. Fast, sleek, and great battery life. Highly recommend for developers!', true, 15),
(1, 4, 4, 'Great but pricey', 'Solid performance but a bit expensive. Still worth it for the quality.', true, 8),
(2, 4, 5, 'Best phone I have owned', 'Amazing camera quality and the display is gorgeous. Battery lasts all day.', true, 22),
(2, 5, 4, 'Good phone', 'Nice phone overall. Some features are confusing but great value.', false, 5),
(3, 3, 5, 'Crystal clear audio', 'The noise cancellation is incredible. Perfect for commuting.', true, 12),
(9, 6, 5, 'Must-read for Java devs', 'Comprehensive and well-written. Helped me land my first job!', true, 30),
(11, 7, 4, 'Life-changing book', 'Really improved my code quality. Some examples are dated but concepts are solid.', true, 18),
(15, 5, 3, 'Decent quality', 'The fabric is nice but shrunk after washing. Size up!', true, 6),
(25, 8, 5, 'Perfect for home gym', 'Great quality dumbbells. The adjustable feature is smooth and reliable.', true, 9),
(27, 6, 4, 'Good coffee maker', 'Makes great coffee. The programmable timer is convenient.', true, 7);

-- ============================================
-- SAMPLE INVENTORY LOGS
-- ============================================
INSERT INTO inventory_logs (product_id, change_amount, previous_quantity, new_quantity, change_type, reason, performed_by) VALUES
(1, 50, 0, 50, 'restock', 'Initial inventory', 1),
(1, -25, 50, 25, 'sale', 'January sales', 2),
(2, 100, 0, 100, 'restock', 'Initial inventory', 1),
(2, -50, 100, 50, 'sale', 'Holiday promotion sales', 2),
(3, 150, 0, 150, 'restock', 'Initial inventory', 1),
(3, -50, 150, 100, 'sale', 'Black Friday sales', 2),
(15, -5, 205, 200, 'damaged', 'Warehouse damage', 2),
(20, 30, 20, 50, 'restock', 'Supplier shipment received', 1);
