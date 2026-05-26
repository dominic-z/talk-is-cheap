CREATE TABLE if not exists `user_icp` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `zipcode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `birthdate` date NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_zipcode_birthdate` (`zipcode`,`birthdate`) ,
  KEY `idx_birthdate` (`birthdate`) 
  ) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4;


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
        INSERT INTO user_icp (username, zipcode, birthdate)
        VALUES 
        (CONCAT('U', FLOOR(RAND()*1000000)),CONCAT('test_', i), DATE(NOW() - INTERVAL FLOOR(RAND()*365) DAY));
        SET i = i + 1;
        -- 每1000条提交一次
        IF i % 1000 = 0 THEN
            COMMIT;
        END IF;
    END WHILE;
    COMMIT; -- 提交最后剩余数据
END //
DELIMITER ; -- 恢复语句结束符为;

-- 调用存储过程，插入500万条数据（执行时间约1-5分钟，看数据库性能）
CALL insert_test_data(10000);

-- 删除存储过程（用完清理）
DROP PROCEDURE IF EXISTS insert_test_data;

-- 恢复数据库默认配置
SET autocommit = 1;
SET unique_checks = 1;
SET foreign_key_checks = 1;


SELECT date(NOW() - INTERVAL FLOOR(RAND()*365) DAY);


# 触发索引下推 Using index condition，通过zipcode的索引定位了一批数据，然后在遍历数据的时候，直接基于索引数据的birthdate计算并过滤，只针对这些留下的数据回表
explain SELECT * FROM user_icp WHERE zipcode = 'test_3' AND MONTH(birthdate) = 3; 

# 触发索引下推不会触发索引下推，因为这个sql没有触发索引，没法下推。但实际上，我觉得这个是有优化空间的，也完全可以在二级索引上下推一次
explain SELECT * FROM user_icp WHERE MONTH(birthdate) = 3; 
SELECT * FROM user_icp WHERE MONTH(birthdate) = 3;

explain SELECT * FROM user_icp where birthdate>'2026-03-06';
explain SELECT * FROM user_icp where id=1002;
explain SELECT zipcode FROM user_icp;


