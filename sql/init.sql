-- 在线电影票预订系统：数据库初始化
-- 用法：mysql -u root -p < sql/init.sql
CREATE DATABASE IF NOT EXISTS movie_ticket
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

-- 表结构由 JPA（ddl-auto=update）自动创建，此处仅需建库。
-- 如需专用账号，可取消下面注释并按实际密码修改：
-- CREATE USER IF NOT EXISTS 'movie'@'localhost' IDENTIFIED BY 'movie123456';
-- GRANT ALL PRIVILEGES ON movie_ticket.* TO 'movie'@'localhost';
-- FLUSH PRIVILEGES;
