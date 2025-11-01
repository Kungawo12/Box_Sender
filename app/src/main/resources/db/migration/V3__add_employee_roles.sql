-- Add role column to employees table with default value EMPLOYEE
-- This migration adds role-based access control to the Box Sender system
-- Roles: ADMIN (full access + role management), MAILROOM_STAFF (full operational access), EMPLOYEE (limited access)

-- Step 1: Add role column with default value
ALTER TABLE employees
ADD COLUMN role VARCHAR(50) NOT NULL DEFAULT 'EMPLOYEE';

-- Step 2: Set specific accounts as ADMIN (only if they exist)
-- Nicholas Herberg's account
UPDATE employees
SET role = 'ADMIN'
WHERE email = 'nicholas.herberg@my.metrostate.edu'
AND EXISTS (SELECT 1 FROM employees WHERE email = 'nicholas.herberg@my.metrostate.edu');

-- Admin backup account
UPDATE employees
SET role = 'ADMIN'
WHERE email = 'admin@boxsender.com'
AND EXISTS (SELECT 1 FROM employees WHERE email = 'admin@boxsender.com');

-- Note: If nicholas.herberg@my.metrostate.edu doesn't exist yet:
-- 1. Register the account normally through the application
-- 2. Then manually run: UPDATE employees SET role = 'ADMIN' WHERE email = 'nicholas.herberg@my.metrostate.edu';
--
-- Alternatively, create an admin account now:
-- 1. Register using the application with any account
-- 2. Run: UPDATE employees SET role = 'ADMIN' WHERE email = 'your-email@example.com';
