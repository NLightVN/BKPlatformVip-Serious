-- Migration script to add AWAITING_PICKUP status to orders table constraint
-- Run this SQL script in your PostgreSQL database

-- Step 1: Drop the existing check constraint
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check;

-- Step 2: Recreate the constraint with all status values including AWAITING_PICKUP
ALTER TABLE orders ADD CONSTRAINT orders_status_check 
    CHECK (status IN ('PENDING', 'AWAITING_PICKUP', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELLED'));

-- Verify the constraint was created successfully
SELECT conname, pg_get_constraintdef(oid) 
FROM pg_constraint 
WHERE conname = 'orders_status_check';
