INSERT INTO package_info (type, price, duration_hours, bandwidth_mbps)
SELECT 'one_hour', 10, 1, 5 WHERE NOT EXISTS (SELECT 1 FROM package_info WHERE type = 'one_hour');
INSERT INTO package_info (type, price, duration_hours, bandwidth_mbps)
SELECT 'two_hour', 15, 2, 5 WHERE NOT EXISTS (SELECT 1 FROM package_info WHERE type = 'two_hour');
INSERT INTO package_info (type, price, duration_hours, bandwidth_mbps)
SELECT 'four_hour', 25, 4, 5 WHERE NOT EXISTS (SELECT 1 FROM package_info WHERE type = 'four_hour');
INSERT INTO package_info (type, price, duration_hours, bandwidth_mbps)
SELECT 'six_hour', 30, 6, 5 WHERE NOT EXISTS (SELECT 1 FROM package_info WHERE type = 'six_hour');
INSERT INTO package_info (type, price, duration_hours, bandwidth_mbps)
SELECT 'one_day', 40, 24, 5 WHERE NOT EXISTS (SELECT 1 FROM package_info WHERE type = 'one_day');
INSERT INTO package_info (type, price, duration_hours, bandwidth_mbps)
SELECT 'two_day', 70, 48, 5 WHERE NOT EXISTS (SELECT 1 FROM package_info WHERE type = 'two_day');
INSERT INTO package_info (type, price, duration_hours, bandwidth_mbps)
SELECT 'weekly', 250, 168, 5 WHERE NOT EXISTS (SELECT 1 FROM package_info WHERE type = 'weekly');
INSERT INTO package_info (type, price, duration_hours, bandwidth_mbps)
SELECT 'monthly', 900, 720, 5 WHERE NOT EXISTS (SELECT 1 FROM package_info WHERE type = 'monthly');