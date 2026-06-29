-- 获取最近一次死锁详情

SHOW ENGINE INNODB STATUS;

-- 记录所有死锁日志
SHOW GLOBAL VARIABLES LIKE 'innodb_print_all_deadlocks';
SET GLOBAL innodb_print_all_deadlocks = ON;
SET GLOBAL log_error_verbosity = 3; -- 打印死锁详细日志，否则只会打印两个锁的情况，没有



-- 除了查看系统日志，也可以通过performance_schema查看死锁日志。但是还是系统日志好一些。。。
SELECT * FROM performance_schema.error_log ;