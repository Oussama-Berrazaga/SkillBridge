-- 1. Flesh out Automotive (ID 3)
INSERT INTO categories (name, description, icon_code, parent_id) VALUES 
('Oil & Brake Service', 'Routine fluid changes and safety checks', 'oil_barrel', 3),
('Body Work', 'Dent repair and painting', 'car_repair', 3),
('Detailing', 'Interior and exterior deep cleaning', 'auto_fix_high', 3);

-- 2. Professional Services (Root 5)
INSERT INTO categories (name, description, icon_code) VALUES 
('Business & Legal', 'Consulting, accounting, and legal help', 'business_center');

INSERT INTO categories (name, description, icon_code, parent_id) VALUES 
('Tax Preparation', 'Individual and small business filing', 'receipt_long', (SELECT id FROM categories WHERE name = 'Business & Legal')),
('Translation', 'Document and live translation services', 'translate', (SELECT id FROM categories WHERE name = 'Business & Legal')),
('Notary Public', 'Document signing and verification', 'history_edu', (SELECT id FROM categories WHERE name = 'Business & Legal'));

-- 3. Wellness & Personal Care (Root 6)
INSERT INTO categories (name, description, icon_code) VALUES 
('Wellness', 'Personal training, massage, and nutrition', 'spa');

INSERT INTO categories (name, description, icon_code, parent_id) VALUES 
('Personal Training', 'Fitness coaching and plans', 'fitness_center', (SELECT id FROM categories WHERE name = 'Wellness')),
('Massage Therapy', 'Deep tissue and relaxation', 'self_improvement', (SELECT id FROM categories WHERE name = 'Wellness')),
('Yoga Instruction', 'Private or group sessions', 'accessibility_new', (SELECT id FROM categories WHERE name = 'Wellness'));

-- 4. Events & Lessons (Root 7)
INSERT INTO categories (name, description, icon_code) VALUES 
('Events & Education', 'Tutors, music teachers, and event staff', 'school');

INSERT INTO categories (name, description, icon_code, parent_id) VALUES 
('Math Tutoring', 'Algebra, Calculus, and SAT prep', 'functions', (SELECT id FROM categories WHERE name = 'Events & Education')),
('Music Lessons', 'Guitar, Piano, and Vocal coaching', 'music_note', (SELECT id FROM categories WHERE name = 'Events & Education')),
('Event Photography', 'Weddings, parties, and headshots', 'camera_alt', (SELECT id FROM categories WHERE name = 'Events & Education'));

-- 5. Pet Services (Root 8)
INSERT INTO categories (name, description, icon_code) VALUES 
('Pet Care', 'Grooming, walking, and sitting', 'pets');

INSERT INTO categories (name, description, icon_code, parent_id) VALUES 
('Dog Walking', 'Daily exercise for your furry friends', 'pest_control_rodent', (SELECT id FROM categories WHERE name = 'Pet Care')),
('Pet Grooming', 'Bathing and hair cutting', 'content_cut', (SELECT id FROM categories WHERE name = 'Pet Care')),
('Pet Sitting', 'Overnight care and feeding', 'night_shelter', (SELECT id FROM categories WHERE name = 'Pet Care'));