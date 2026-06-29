use sakila;

select * from customer;
select store_id,count(1)  from customer group by store_id;


-- dead lock demo 1
start transaction;

select * from customer where address_id = 9 for update;

select * from customer where address_id = 7 for update;

commit;



-- dead lock demo 2 如果不使用同一个索引，但是实际锁的是同一个数据
start transaction;

select * from customer where address_id = 9 for update;

select * from customer where address_id = 7 for update;

commit;


-- dead lock demo 3 next-key lock
start transaction;

select * from customer where last_name = 'CBA' for update;  -- 介于CAUSEY和CHAMBERS中间

select * from customer where last_name = 'ABZ' for update;  -- 介于 ABNEY和ADAM中间

commit;



-- dead lock demo 4 Next lock
start transaction;
-- 介于CAUSEY和CHAMBERS中间
select * from customer where last_name > 'CAUSEY' and last_name<'CHAMBERS' for update; 

-- 介于 ABNEY和ADAM中间的next-key lock
select * from customer where last_name > 'ABNEY' and last_name<'ADAM' for update;  

SELECT
  ENGINE_TRANSACTION_ID ,OBJECT_SCHEMA,OBJECT_NAME,INDEX_NAME,
  LOCK_TYPE,LOCK_MODE,LOCK_DATA,ENGINE_TRANSACTION_ID
FROM performance_schema.data_locks;

commit;






-- dead lock demo 5 一个过大范围的锁

start transaction;

select * from customer where last_name > 'A' and last_name<'C' for update; 

select * from customer where last_name > 'E' and last_name<'F' for update;  

SELECT
  ENGINE_TRANSACTION_ID ,OBJECT_SCHEMA,OBJECT_NAME,INDEX_NAME,
  LOCK_TYPE,LOCK_MODE,LOCK_DATA,ENGINE_TRANSACTION_ID
FROM performance_schema.data_locks;

commit;






