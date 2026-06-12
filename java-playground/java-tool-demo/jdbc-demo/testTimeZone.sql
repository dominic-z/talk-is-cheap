
CREATE DATABASE IF NOT EXISTS tz_test_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

truncate table tz_test;
CREATE TABLE tz_test (
    id          INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    ts_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'TIMESTAMP类型，内部存UTC，展示随时区变化',
    dt_time     DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'DATETIME类型，存储字面量，展示不随时区变化'
) COMMENT = '时区行为对比测试表';


-- 插入3条记录（间隔几秒即可，或手动指定相同时间）
INSERT INTO tz_test (id) VALUES (NULL), (NULL), (NULL);

-- 也可以手动指定一个固定时间来精确对比
INSERT INTO tz_test (ts_time, dt_time)
VALUES ('2026-06-10 11:37:00', '2026-06-10 11:37:00');

SELECT @@global.time_zone;
SELECT @@session.time_zone;

-- ========== 场景A：北京时区 ==========
SET time_zone = '+08:00';
SELECT
    id,
    ts_time AS `TIMESTAMP_北京时间`,
    dt_time AS `DATETIME_北京时间`
FROM tz_test;

-- ========== 场景B：UTC时区 ==========
SET time_zone = '+00:00';
SET global time_zone = '+08:00';
SELECT
    id,
    ts_time AS `TIMESTAMP_UTC`,
    dt_time AS `DATETIME_UTC`
FROM tz_test;

-- ========== 场景C：东京时区 ==========
SET time_zone = '+09:00';
SELECT
    id,
    ts_time AS `TIMESTAMP_东京时间`,
    dt_time AS `DATETIME_东京时间`
FROM tz_test;