-- Разрешаем репликацию (в Docker это можно сделать через SQL в новых версиях)
ALTER SYSTEM SET wal_level = 'replica';
ALTER SYSTEM SET max_wal_senders = 10;
ALTER SYSTEM SET max_replication_slots = 10;
ALTER SYSTEM SET hot_standby = 'on';