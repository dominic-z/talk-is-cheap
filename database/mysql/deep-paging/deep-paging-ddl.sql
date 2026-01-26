-- 1. 创建测试数据库
CREATE DATABASE IF NOT EXISTS test_page_perf DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE test_page_perf;

-- 2. 创建测试表（内置主键索引、user_id普通索引、order_no唯一索引）
DROP TABLE IF EXISTS t_test_data;
CREATE TABLE t_test_data (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    user_id VARCHAR(32) NOT NULL COMMENT '模拟用户ID',
    order_no VARCHAR(64) NOT NULL COMMENT '模拟订单号',
    create_time DATETIME NOT NULL COMMENT '模拟创建时间',
    amount DECIMAL(10,2) NOT NULL COMMENT '模拟金额',
    status TINYINT NOT NULL COMMENT '模拟状态(0-9)',
    remark VARCHAR(255) DEFAULT '' COMMENT '模拟备注',
    -- 主键索引（深度分页核心依赖）
    PRIMARY KEY (id) USING BTREE,
    -- user_id 普通索引（适配按用户筛选分页）
    INDEX idx_user_id (user_id) USING BTREE COMMENT '用户ID索引，适配按用户筛选分页'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='深度分页测试表' AUTO_INCREMENT=1;

-- 后续插入数据的语句无需修改，可直接复用之前的存储过程/循环插入代码
-- 关闭事务自动提交（提升插入速度）
SET autocommit = 0;
-- 关闭唯一索引校验（临时提升插入速度，插完恢复）
SET unique_checks = 0;
-- 关闭外键校验（临时提升插入速度，插完恢复）
SET foreign_key_checks = 0;



-- 创建存储过程批量插入数据
DROP PROCEDURE IF EXISTS insert_test_data;
DELIMITER // -- 临时修改语句结束符为//，避免存储过程内;中断执行
CREATE PROCEDURE insert_test_data(IN total INT UNSIGNED)
BEGIN
    DECLARE i INT UNSIGNED DEFAULT 1;
    -- 循环插入，每1000条批量提交一次（避免单事务过大，平衡速度与内存）
    WHILE i <= total DO
        INSERT INTO t_test_data (user_id, order_no, create_time, amount, status, remark)
        VALUES 
        (CONCAT('U', FLOOR(RAND()*1000000)), CONCAT('ORD', FLOOR(RAND()*10000000000), i), NOW() - INTERVAL FLOOR(RAND()*365) DAY, ROUND(RAND()*10000,2), FLOOR(RAND()*10), CONCAT('test_', i)),
        (CONCAT('U', FLOOR(RAND()*1000000)), CONCAT('ORD', FLOOR(RAND()*10000000000), i), NOW() - INTERVAL FLOOR(RAND()*365) DAY, ROUND(RAND()*10000,2), FLOOR(RAND()*10), CONCAT('test_', i));
        SET i = i + 2;
        -- 每1000条提交一次
        IF i % 1000 = 0 THEN
            COMMIT;
        END IF;
    END WHILE;
    COMMIT; -- 提交最后剩余数据
END //
DELIMITER ; -- 恢复语句结束符为;

-- 调用存储过程，插入500万条数据（执行时间约1-5分钟，看数据库性能）
CALL insert_test_data(5000000);

-- 删除存储过程（用完清理）
DROP PROCEDURE IF EXISTS insert_test_data;

-- 恢复数据库默认配置
SET autocommit = 1;
SET unique_checks = 1;
SET foreign_key_checks = 1;

-- 查看插入结果（验证数据量）
SELECT COUNT(*) AS total_rows FROM t_test_data;