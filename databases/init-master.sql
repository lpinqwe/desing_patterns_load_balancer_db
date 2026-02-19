-- Создаем пользователя для репликации
CREATE ROLE replicator WITH REPLICATION PASSWORD 'replicator_password' LOGIN;

-- Разрешаем подключения для репликации (Postgres 15 позволяет делать это через SQL в некоторых средах,
-- но в Docker обычно достаточно встроенных настроек, если мы запускаем от имени нужного юзера)