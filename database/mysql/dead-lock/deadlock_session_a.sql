use sakila;

select * from customer;
select store_id,count(1)  from customer group by store_id;


-- dead lock demo 1
start transaction;

select * from customer where address_id = 7 for update;

select * from customer where address_id = 9 for update;

commit;

-- dead lock demo 2 如果不使用同一个索引，但是实际锁的是同一个数据
start transaction;

select * from customer where last_name = 'WILLIAMS' for update; -- 对应address_id = 7

select * from customer where last_name = 'BROWN' for update; -- 对应address_id = 9

commit;



-- dead lock demo 3 Gap lock  不会死锁，同一段索引间隙，多个事务可以同时持有 X,GAP 排他间隙锁，加锁时不会互相阻塞

start transaction;

select * from customer where last_name = 'ABZ' for update;  -- 介于 ABNEY和ADAM中间
SELECT
  ENGINE_TRANSACTION_ID ,OBJECT_SCHEMA,OBJECT_NAME,INDEX_NAME,
  LOCK_TYPE,LOCK_MODE,LOCK_DATA,ENGINE_TRANSACTION_ID
FROM performance_schema.data_locks;

select * from customer where last_name = 'CBA' for update;  -- 介于CAUSEY和CHAMBERS中间

commit;


-- dead lock demo 4 Next lock 
-- 死锁日志（show engine innodb status / error log）能区分 Record 锁，但无法直接区分 Next-Key Lock 和 Gap Lock
start transaction;
-- 介于 ABNEY和ADAM中间的next-key lock，但是这个区间没有任何满足条件的数据，因此下面sql加锁的时候只有idx_last_name有锁
-- 但是实际上如果命中了数据，会对primary key字段也加上record锁。
-- 例如，select * from customer where last_name > 'E' and last_name<'F' for update; 
select * from customer where last_name > 'ABNEY' and last_name<'ADAM' for update;  

SELECT
  ENGINE_TRANSACTION_ID ,OBJECT_SCHEMA,OBJECT_NAME,INDEX_NAME,
  LOCK_TYPE,LOCK_MODE,LOCK_DATA,ENGINE_TRANSACTION_ID
FROM performance_schema.data_locks;

-- 介于CAUSEY和CHAMBERS中间
select * from customer where last_name > 'CAUSEY' and last_name<'CHAMBERS' for update; 

commit;



-- dead lock demo 5 一个过大范围的锁

start transaction;
select * from customer where last_name > 'E' and last_name<'F' for update;  

SELECT
  ENGINE_TRANSACTION_ID ,OBJECT_SCHEMA,OBJECT_NAME,INDEX_NAME,
  LOCK_TYPE,LOCK_MODE,LOCK_DATA,ENGINE_TRANSACTION_ID
FROM performance_schema.data_locks;


select * from customer where last_name > 'A' and last_name<'C' for update; 

commit;







-- dead lock demo ?
start transaction;

INSERT INTO customer (store_id,first_name,last_name，email,address_id,active,create_date,last_update) 
VALUES (1,'ABZF','ABZ','ABZF.ABZ@sakilacustomer.org',5,1,'2006-02-14 22:04:36','2006-02-15 04:57:20');  
-- 介于 ABNEY和ADAM中间

SELECT
  ENGINE_TRANSACTION_ID ,OBJECT_SCHEMA,OBJECT_NAME,INDEX_NAME,
  LOCK_TYPE,LOCK_MODE,LOCK_DATA,ENGINE_TRANSACTION_ID
FROM performance_schema.data_locks;

INSERT INTO customer (store_id,first_name,last_name，email,address_id,active,create_date,last_update) 
VALUES (1,'CBAF','CBA','CBAF.CBA@sakilacustomer.org',5,1,'2006-02-14 22:04:36','2006-02-15 04:57:20');  
-- 介于CAUSEY和CHAMBERS中间

commit;

