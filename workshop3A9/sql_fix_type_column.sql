-- Fix "Data truncated for column 'type'" error
-- Run this in MySQL: USE wingo; then run the ALTER below

USE wingo;

-- Make type column accept 'user', 'admin', etc. (VARCHAR 50)
ALTER TABLE utilisateur MODIFY COLUMN type VARCHAR(50);
