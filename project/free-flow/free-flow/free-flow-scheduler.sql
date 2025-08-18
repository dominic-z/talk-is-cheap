-- scheduler专属的库
drop database if exists free_flow_scheduler;
create database if not exists free_flow_scheduler DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
use free_flow_scheduler;


drop table if exists scheduler_log;
create table if not exists scheduler_log (
`id` bigint AUTO_INCREMENT primary key,
`node_id` varchar(64) not null comment '节点id',
`log` varchar(64) not null default 0 comment 'log',
`revision` bigint not null default 0 comment '并发控制编号',
`create_time` datetime not null default now() comment '创建日期',
`update_time` datetime not null default now() comment '更新日期'
)
ENGINE = InnoDB default charset = utf8mb4 comment 'scheduler节点日志';

insert into scheduler_log(`node_id`,`log`) values('192.168.58.128:7070',1);
SELECT * from scheduler_log sl ;