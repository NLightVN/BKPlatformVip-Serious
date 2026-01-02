-- Migration script to convert 'deleted' boolean fields to 'status' varchar fields
-- Run this BEFORE starting the updated backend application

-- =====================================================
-- 1. SHOPS TABLE
-- =====================================================

-- Add status column to shops table
ALTER TABLE shops ADD COLUMN IF NOT EXISTS status VARCHAR(20);

-- Migrate existing data: deleted=true -> 'DELETED', deleted=false -> 'ACTIVE'
UPDATE shops SET status = CASE WHEN deleted = true THEN 'DELETED' ELSE 'ACTIVE' END WHERE status IS NULL;

-- Set default value for status
ALTER TABLE shops ALTER COLUMN status SET DEFAULT 'ACTIVE';

-- Make status NOT NULL
ALTER TABLE shops ALTER COLUMN status SET NOT NULL;

-- Drop old deleted column
ALTER TABLE shops DROP COLUMN IF EXISTS deleted;

-- =====================================================
-- 2. PRODUCTS TABLE
-- =====================================================

-- Add status column to products table
ALTER TABLE products ADD COLUMN IF NOT EXISTS status VARCHAR(20);

-- Migrate existing data: deleted=true -> 'DELETED', deleted=false -> 'ACTIVE'
UPDATE products SET status = CASE WHEN deleted = true THEN 'DELETED' ELSE 'ACTIVE' END WHERE status IS NULL;

-- Set default value for status
ALTER TABLE products ALTER COLUMN status SET DEFAULT 'ACTIVE';

-- Make status NOT NULL
ALTER TABLE products ALTER COLUMN status SET NOT NULL;

-- Drop old deleted column
ALTER TABLE products DROP COLUMN IF EXISTS deleted;

-- =====================================================
-- 3. USERS TABLE
-- =====================================================

-- Add status column to users table (new field, no migration needed)
ALTER TABLE users ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL;

-- =====================================================
-- VERIFICATION
-- =====================================================

-- You can run these queries to verify the migration:
-- SELECT status, COUNT(*) FROM shops GROUP BY status;
-- SELECT status, COUNT(*) FROM products GROUP BY status;
-- SELECT status, COUNT(*) FROM users GROUP BY status;

-- Verify migration
SELECT 'Shops migration:' as info, status, COUNT(*) as count FROM shops GROUP BY status
UNION ALL
SELECT 'Products migration:', status, COUNT(*) FROM products GROUP BY status
UNION ALL
SELECT 'Users migration:', status, COUNT(*) FROM users GROUP BY status;
