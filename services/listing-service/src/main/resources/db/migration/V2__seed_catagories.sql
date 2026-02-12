-- 1. Root Categories
INSERT INTO categories (name, description, icon_code) VALUES 
('Home & Garden', 'Maintenance, renovation, and gardening services', 'home_work'),
('Electronics & IT', 'Repair and setup for devices and software', 'devices'),
('Automotive', 'Vehicle repair and maintenance', 'directions_car');

-- 2. Sub-categories for Home & Garden (Level 1)
INSERT INTO categories (name, description, icon_code, parent_id) VALUES 
('Plumbing', 'Pipe repair and installation', 'water_drop', (SELECT id FROM categories WHERE name = 'Home & Garden')),
('Electrical', 'Wiring and appliance repair', 'bolt', (SELECT id FROM categories WHERE name = 'Home & Garden'));

-- 3. Deep Sub-categories for Plumbing (Level 2)
INSERT INTO categories (name, description, icon_code, parent_id) VALUES 
('Emergency Leak', '24/7 urgent pipe repair', 'emergency', (SELECT id FROM categories WHERE name = 'Plumbing')),
('Bathroom Fitting', 'Installation of sinks and toilets', 'bathtub', (SELECT id FROM categories WHERE name = 'Plumbing'));

-- 4. Sub-categories for Electronics (Level 1)
INSERT INTO categories (name, description, icon_code, parent_id) VALUES 
('Smartphone Repair', 'Screen and battery replacements', 'smartphone', (SELECT id FROM categories WHERE name = 'Electronics & IT')),
('PC/Laptop Setup', 'Software install and hardware repair', 'laptop', (SELECT id FROM categories WHERE name = 'Electronics & IT'));