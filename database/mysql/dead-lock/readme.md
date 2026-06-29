# msyql死锁排查

使用mysql的Sakila Sample Database数据库，https://dev.mysql.com/doc/sakila/en/sakila-installation.html，注意，其中`sakila-schema.sql`这个sql需要在mysql的命令行里执行，不能通过dbeaver执行
```shell
mysql> source /root/sakila-schema.sql
```


```shell
docker run -d \
  --name mysql-custom \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=your_password \
  -v ./my.cnf:/etc/mysql/conf.d/custom.cnf:ro \
  mysql:8.0

# 或者
docker cp my.cnf mysql8:/etc/mysql/conf.d/
docker restart mysql8
docker logs -f mysql8
```

# 最近一次死锁

通过`SHOW ENGINE INNODB STATUS;`指令可以获取最近一次死锁


```
------------------------
LATEST DETECTED DEADLOCK
------------------------
2026-06-29 15:09:27 138503132264000
*** (1) TRANSACTION:
TRANSACTION 4811, ACTIVE 5 sec starting index read
mysql tables in use 1, locked 1
LOCK WAIT 5 lock struct(s), heap size 1128, 4 row lock(s)
MySQL thread id 18, OS thread handle 138503238284864, query id 1573 172.17.0.1 root executing
/* ApplicationName=DBeaver 25.1.3 - SQLEditor <deadlock_session_a.sql> */ select * from customer where address_id = 8 for update

*** (1) HOLDS THE LOCK(S):
RECORD LOCKS space id 118 page no 6 n bits 672 index idx_fk_address_id of table `sakila`.`customer` trx id 4811 lock_mode X
Record lock, heap no 2 PHYSICAL RECORD: n_fields 2; compact format; info bits 0
 0: len 2; hex 0005; asc   ;;
 1: len 2; hex 0001; asc   ;;


*** (1) WAITING FOR THIS LOCK TO BE GRANTED:
RECORD LOCKS space id 118 page no 6 n bits 672 index idx_fk_address_id of table `sakila`.`customer` trx id 4811 lock_mode X waiting
Record lock, heap no 5 PHYSICAL RECORD: n_fields 2; compact format; info bits 0
 0: len 2; hex 0008; asc   ;;
 1: len 2; hex 0004; asc   ;;


*** (2) TRANSACTION:
TRANSACTION 4810, ACTIVE 10 sec starting index read
mysql tables in use 1, locked 1
LOCK WAIT 5 lock struct(s), heap size 1128, 4 row lock(s)
MySQL thread id 19, OS thread handle 138503570515520, query id 1574 172.17.0.1 root executing
/* ApplicationName=DBeaver 25.1.3 - SQLEditor <deadlock_session_b.sql> */ select * from customer where address_id = 5 for update

*** (2) HOLDS THE LOCK(S):
RECORD LOCKS space id 118 page no 6 n bits 672 index idx_fk_address_id of table `sakila`.`customer` trx id 4810 lock_mode X
Record lock, heap no 5 PHYSICAL RECORD: n_fields 2; compact format; info bits 0
 0: len 2; hex 0008; asc   ;;
 1: len 2; hex 0004; asc   ;;


*** (2) WAITING FOR THIS LOCK TO BE GRANTED:
RECORD LOCKS space id 118 page no 6 n bits 672 index idx_fk_address_id of table `sakila`.`customer` trx id 4810 lock_mode X waiting
Record lock, heap no 2 PHYSICAL RECORD: n_fields 2; compact format; info bits 0
 0: len 2; hex 0005; asc   ;;
 1: len 2; hex 0001; asc   ;;

*** WE ROLL BACK TRANSACTION (2)
```


对这段日志的解释：https://www.doubao.com/thread/x993c18649f61830ba682c1272b9ad2d9

# 记录所有死锁日志


```shell
GET GLOBAL innodb_print_all_deadlocks;

SET GLOBAL innodb_print_all_deadlocks = ON;

```

持久生效需要在 my.cnf / my.ini 的 [mysqld] 段添加 innodb_print_all_deadlocks = ON


>  最佳实践建议：在 Docker 环境中，不要将 MySQL 日志写入独立文件，而是让其自然输出到 stdout/stderr，统一由 Docker 日志驱动管理。这样既避免权限问题，又便于 docker logs 查看和日志收集工具（如 Fluentd、Loki）对接。

```sql

# 或通过 SQL 查看（MySQL 8.0+）
SELECT * FROM performance_schema.error_log ;

```

如下
```
2026-06-29T08:31:41.894402Z 0 [Note] [MY-012468] [InnoDB] Transactions deadlock detected, dumping detailed information.
2026-06-29T08:31:41.894429Z 0 [Note] [MY-012469] [InnoDB]  *** (1) TRANSACTION: 
TRANSACTION 5657, ACTIVE 10 sec starting index read
mysql tables in use 1, locked 1
LOCK WAIT 5 lock struct(s), heap size 1128, 5 row lock(s)
MySQL thread id 10, OS thread handle 131443988571712, query id 74 172.17.0.1 root executing
/* ApplicationName=DBeaver 25.1.3 - SQLEditor <deadlock_session_a.sql> */ select * from customer where last_name = 'BROWN' for update
2026-06-29T08:31:41.894452Z 0 [Note] [MY-012469] [InnoDB]  *** (1) HOLDS THE LOCK(S): 
RECORD LOCKS space id 118 page no 8 n bits 256 index PRIMARY of table `sakila`.`customer` trx id 5657 lock_mode X locks rec but not gap
Record lock, heap no 4 PHYSICAL RECORD: n_fields 11; compact format; info bits 0
 0: len 2; hex 0003; asc   ;;
 1: len 6; hex 00000000128d; asc       ;;
 2: len 7; hex 820000015b0126; asc     [ &;;
 3: len 1; hex 01; asc  ;;
 4: len 5; hex 4c494e4441; asc LINDA;;
 5: len 8; hex 57494c4c49414d53; asc WILLIAMS;;
 6: len 30; hex 4c494e44412e57494c4c49414d534073616b696c61637573746f6d65722e; asc LINDA.WILLIAMS@sakilacustomer.; (total 33 bytes);
 7: len 2; hex 0007; asc   ;;
 8: len 1; hex 81; asc  ;;
 9: len 5; hex 99781d6124; asc  x a$;;
 10: len 4; hex 43f24430; asc C D0;;

2026-06-29T08:31:41.894633Z 0 [Note] [MY-012469] [InnoDB]  *** (1) WAITING FOR THIS LOCK TO BE GRANTED: 
RECORD LOCKS space id 118 page no 8 n bits 256 index PRIMARY of table `sakila`.`customer` trx id 5657 lock_mode X locks rec but not gap waiting
Record lock, heap no 6 PHYSICAL RECORD: n_fields 11; compact format; info bits 0
 0: len 2; hex 0005; asc   ;;
 1: len 6; hex 00000000128d; asc       ;;
 2: len 7; hex 820000015b013c; asc     [ <;;
 3: len 1; hex 01; asc  ;;
 4: len 9; hex 454c495a4142455448; asc ELIZABETH;;
 5: len 5; hex 42524f574e; asc BROWN;;
 6: len 30; hex 454c495a41424554482e42524f574e4073616b696c61637573746f6d6572; asc ELIZABETH.BROWN@sakilacustomer; (total 34 bytes);
 7: len 2; hex 0009; asc   ;;
 8: len 1; hex 81; asc  ;;
 9: len 5; hex 99781d6124; asc  x a$;;
 10: len 4; hex 43f24430; asc C D0;;

2026-06-29T08:31:41.894804Z 0 [Note] [MY-012469] [InnoDB]  *** (2) TRANSACTION: 
TRANSACTION 5658, ACTIVE 8 sec starting index read
mysql tables in use 1, locked 1
LOCK WAIT 5 lock struct(s), heap size 1128, 5 row lock(s)
MySQL thread id 11, OS thread handle 131443987514944, query id 75 172.17.0.1 root executing
/* ApplicationName=DBeaver 25.1.3 - SQLEditor <deadlock_session_b.sql> */ select * from customer where address_id = 7 for update
2026-06-29T08:31:41.894819Z 0 [Note] [MY-012469] [InnoDB]  *** (2) HOLDS THE LOCK(S): 
RECORD LOCKS space id 118 page no 8 n bits 256 index PRIMARY of table `sakila`.`customer` trx id 5658 lock_mode X locks rec but not gap
Record lock, heap no 6 PHYSICAL RECORD: n_fields 11; compact format; info bits 0
 0: len 2; hex 0005; asc   ;;
 1: len 6; hex 00000000128d; asc       ;;
 2: len 7; hex 820000015b013c; asc     [ <;;
 3: len 1; hex 01; asc  ;;
 4: len 9; hex 454c495a4142455448; asc ELIZABETH;;
 5: len 5; hex 42524f574e; asc BROWN;;
 6: len 30; hex 454c495a41424554482e42524f574e4073616b696c61637573746f6d6572; asc ELIZABETH.BROWN@sakilacustomer; (total 34 bytes);
 7: len 2; hex 0009; asc   ;;
 8: len 1; hex 81; asc  ;;
 9: len 5; hex 99781d6124; asc  x a$;;
 10: len 4; hex 43f24430; asc C D0;;

2026-06-29T08:31:41.894988Z 0 [Note] [MY-012469] [InnoDB]  *** (2) WAITING FOR THIS LOCK TO BE GRANTED: 
RECORD LOCKS space id 118 page no 8 n bits 256 index PRIMARY of table `sakila`.`customer` trx id 5658 lock_mode X locks rec but not gap waiting
Record lock, heap no 4 PHYSICAL RECORD: n_fields 11; compact format; info bits 0
 0: len 2; hex 0003; asc   ;;
 1: len 6; hex 00000000128d; asc       ;;
 2: len 7; hex 820000015b0126; asc     [ &;;
 3: len 1; hex 01; asc  ;;
 4: len 5; hex 4c494e4441; asc LINDA;;
 5: len 8; hex 57494c4c49414d53; asc WILLIAMS;;
 6: len 30; hex 4c494e44412e57494c4c49414d534073616b696c61637573746f6d65722e; asc LINDA.WILLIAMS@sakilacustomer.; (total 33 bytes);
 7: len 2; hex 0007; asc   ;;
 8: len 1; hex 81; asc  ;;
 9: len 5; hex 99781d6124; asc  x a$;;
 10: len 4; hex 43f24430; asc C D0;;

2026-06-29T08:31:41.895198Z 0 [Note] [MY-012469] [InnoDB] *** WE ROLL BACK TRANSACTION (2) 

```

可以看到这个死锁信息打印了锁的行的所有数据，因为这个死锁发生在主键上，解释：https://www.doubao.com/thread/x8ba17ae2fe858ad4b06514fce9bd5c03