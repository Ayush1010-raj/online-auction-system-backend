-- Ensure AUTO_INCREMENT never resets below 1000
-- This runs once on startup; MySQL ignores it if the current counter is already higher
ALTER TABLE product AUTO_INCREMENT = 1000;
ALTER TABLE auction AUTO_INCREMENT = 1000;
