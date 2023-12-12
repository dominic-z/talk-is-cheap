create database if not exists what_to_eat DEFAULT CHARSET utf8 COLLATE utf8_general_ci;

drop table business ;

create table if not exists business (
	`id` bigint AUTO_INCREMENT primary key,
	`name` varchar(64) comment '商户名称' not null,
	`status` int(1) default 0 comment '商家状态' not null,
	`revision` bigint default 0 comment '并发控制编号' not null,
	`create_time` datetime default now() comment '创建日期' not null,
	`update_time` datetime default now() comment '更新日期' not null,
	unique index name(name)
)ENGINE = InnoDB default charset = utf8 COMMENT '商户表';

alter table business add column avatar_path varchar(256) comment '头像路径' after `status`;

SELECT * from business b ;

DROP table business_marketing_plan;
create table if not exists business_marketing_plan (
	`id` bigint auto_increment primary key,
	`business_id` bigint comment '商户id',
	`marketing_start_time` datetime comment '活动开始时间',
	`marketing_end_time` datetime comment '活动结束时间',
	`cron` varchar(32) not null comment '商户活动cron表达式',
	`marketing_title` varchar(32) comment '活动标题',
	`marketing_detail` varchar(256) comment '活动详情',
	`status` int(1) default 0 comment '活动状态' not null,
	`revision` bigint default 0 comment '并发控制编号' not null,
	`create_time` datetime default now() comment '创建日期' not null,
	`update_time` datetime default now() comment '更新日期' not null,
	index business_id(business_id),
	index marketing_start_time(marketing_start_time),
	index marketing_end_time(marketing_end_time)
)ENGINE = InnoDB default charset = utf8 COMMENT '商户的营销计划';
