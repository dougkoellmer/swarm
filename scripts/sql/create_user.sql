CREATE USER 'bh_admin'@'localhost' IDENTIFIED BY '$$$$$';

GRANT ALL ON bh_accounts.* TO 'bh_admin'@'localhost';

GRANT ALL ON bh_telemetry.* TO 'bh_admin'@'localhost';