-- 一些公共数据库
drop database if exists free_flow;
create database if not exists free_flow DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
use free_flow;

drop table if exists cluster_node_log;
create table if not exists cluster_node_log (
`id` bigint AUTO_INCREMENT primary key,
`node_id` varchar(64) not null comment '节点id',
`node_type` int not null default 0 comment '节点类型',
`node_status` int not null default 0 comment '节点状态',
`revision` bigint not null default 0 comment '并发控制编号',
`create_time` datetime not null default now() comment '创建日期',
`update_time` datetime not null default now() comment '更新日期',
index idx_node_id_status(node_id,node_status),
index idx_node_type_status(node_type,node_status)
)
ENGINE = InnoDB default charset = utf8mb4 comment '集群节点登入登出日志';
-- 创建更新触发器
DROP TRIGGER IF EXISTS cluster_node_log_update_time;
DELIMITER $$
CREATE TRIGGER cluster_node_log_update_time 
BEFORE UPDATE ON cluster_node_log
FOR EACH ROW
BEGIN
    IF NEW.update_time IS NULL OR NEW.update_time = '' THEN
        SET NEW.update_time = CURRENT_TIMESTAMP;
    END IF;
END$$
DELIMITER ;


-- select NOW(); 
-- SHOW VARIABLES LIKE '%time_zone%';
-- insert into cluster_node_log(node_id) values('192.168.58.128:7070');
-- -- 
SELECT
	*
from
	cluster_node_log WHERE 1=1;
-- UPDATE cluster_node_log set node_type = 0,update_time='2023-10-01 12:00:00' where id=1;





drop table if exists task_definition;
create table if not exists task_definition(
`id` bigint AUTO_INCREMENT primary key,
`task_name` varchar(64) not null comment '任务名称',
`version` int not null comment '版本',
`timeout` int not null default 0 comment '以秒标识的超时时间',
`max_retry_count` int not null comment '最大重试次数',
`revision` bigint not null default 0 comment '并发控制编号',
`create_time` datetime not null default now() comment '创建日期',
`update_time` datetime not null default now() comment '更新日期',
unique index idx_name_version(task_name,version)
)ENGINE = InnoDB default charset = utf8mb4 comment '任务定义主表';
-- 创建更新触发器
DROP TRIGGER IF EXISTS task_definition_update_time;
DELIMITER $$
CREATE TRIGGER task_definition_update_time 
BEFORE UPDATE ON task_definition
FOR EACH ROW
BEGIN
    IF NEW.update_time IS NULL OR NEW.update_time = '' THEN
        SET NEW.update_time = CURRENT_TIMESTAMP;
    END IF;
END$$
DELIMITER ;





drop table if exists schedule_task_definition;
create table if not exists schedule_task_definition(
`id` bigint AUTO_INCREMENT primary key,
`schedule_task_name` varchar(64) not null comment '预约任务名称',
`version` int not null comment '版本' default 0,
`cron` varchar(32) not null comment '预约任务的cron表达式',
`target_task_id` bigint not null comment '预约任务的目标任务id',
`revision` bigint not null default 0 comment '并发控制编号',
`create_time` datetime not null default now() comment '创建日期',
`update_time` datetime not null default now() comment '更新日期',
unique index idx_name_version(schedule_task_name,version),
index idx_target_task_id(target_task_id)
)ENGINE = InnoDB default charset = utf8mb4 comment '预约任务定义主表';
-- 创建更新触发器
DROP TRIGGER IF EXISTS schedule_task_definition_update_time;
DELIMITER $$
CREATE TRIGGER schedule_task_definition_update_time 
BEFORE UPDATE ON schedule_task_definition
FOR EACH ROW
BEGIN
    IF NEW.update_time IS NULL OR NEW.update_time = '' THEN
        SET NEW.update_time = CURRENT_TIMESTAMP;
    END IF;
END$$
DELIMITER ;




drop table if exists stage_definition;
create table if not exists stage_definition(
`id` bigint AUTO_INCREMENT primary key,
`task_id` bigint not null comment '任务id',
`stage_name` varchar(64) not null comment '阶段名称',
`version` int not null comment '版本' default 0,
`stage_type` int not null comment '阶段类型',
`is_starting_stage` bool not null comment '是否是一个task的起始stage',
`timeout` bigint not null default 0 comment '以秒标识的超时时间',
`max_retry_count` int not null comment '最大重试次数',
`revision` bigint not null default 0 comment '并发控制编号',
`create_time` datetime not null default now() comment '创建日期',
`update_time` datetime not null default now() comment '更新日期',
unique index idx_name_version(stage_name,version),
index idx_task_id(task_id)
)ENGINE = InnoDB default charset = utf8mb4 comment '阶段定义主表';
-- 创建更新触发器
DROP TRIGGER IF EXISTS stage_definition_update_time;
DELIMITER $$
CREATE TRIGGER stage_definition_update_time 
BEFORE UPDATE ON stage_definition
FOR EACH ROW
BEGIN
    IF NEW.update_time IS NULL OR NEW.update_time = '' THEN
        SET NEW.update_time = CURRENT_TIMESTAMP;
    END IF;
END$$
DELIMITER ;





drop table if exists task_graph_definition;
create table if not exists task_graph_definition(
`id` bigint AUTO_INCREMENT primary key,
`task_id` bigint not null comment '任务id',
`from_stage_id` bigint not null comment '任务id',
`to_stage_id` bigint not null comment '阶段名称',
`create_time` datetime not null default now() comment '创建日期',
`update_time` datetime not null default now() comment '更新日期',
index idx_task_id(task_id),
index idx_from_to_stage_id(from_stage_id,to_stage_id),
index idx_to_stage_id(to_stage_id)
)ENGINE = InnoDB default charset = utf8mb4 comment '任务图结构定义表';
-- 创建更新触发器
DROP TRIGGER IF EXISTS task_graph_definition_update_time;
DELIMITER $$
CREATE TRIGGER task_graph_definition_update_time 
BEFORE UPDATE ON task_graph_definition
FOR EACH ROW
BEGIN
    IF NEW.update_time IS NULL OR NEW.update_time = '' THEN
        SET NEW.update_time = CURRENT_TIMESTAMP;
    END IF;
END$$
DELIMITER ;





drop table if exists task_startup;
create table if not exists task_startup(
`id` bigint AUTO_INCREMENT primary key,
`task_id` bigint not null comment '任务id',
`source_type` int not null comment '用于描述此次task启动的原因的类型，例如被scheduleTask调度、被某个stage调起等',
`source_id` bigint comment '用于描述此次task启动的原因的id',
`status` int not null comment '此次启动的状态',
-- `startup_params` varchar(2048) not null comment '启动参数，todo: 移动到es里',
`startup_param_es_id` varchar(128) comment '启动参数，存储于es中，考虑到es的id不一定为数字，因此设定为字符串类型',
`revision` bigint not null default 0 comment '并发控制编号',
`create_time` datetime not null default now() comment '创建日期',
`update_time` datetime not null default now() comment '更新日期',
index idx_task_id(task_id),
index idx_source_type_id(source_type,source_id),
index idx_source_id(source_id)
)ENGINE = InnoDB default charset = utf8mb4 comment '任务启动表';
-- 创建更新触发器
DROP TRIGGER IF EXISTS task_startup_update_time;
DELIMITER $$
CREATE TRIGGER task_startup_update_time 
BEFORE UPDATE ON task_startup
FOR EACH ROW
BEGIN
    IF NEW.update_time IS NULL OR NEW.update_time = '' THEN
        SET NEW.update_time = CURRENT_TIMESTAMP;
    END IF;
END$$
DELIMITER ;





drop table if exists task_execution;
create table if not exists task_execution(
`id` bigint AUTO_INCREMENT primary key,
`task_startup_id` bigint not null comment '任务启动id',
`worker_id` varchar(64) not null comment '执行此次任务的workerid',
`status` int not null comment '此次执行的状态',
`start_time` datetime not null default now() comment '任务启动时间',
`revision` bigint not null default 0 comment '并发控制编号',
`create_time` datetime not null default now() comment '创建日期',
`update_time` datetime not null default now() comment '更新日期',
index idx_task_startup_id(task_startup_id),
index idx_worker(worker_id)
)ENGINE = InnoDB default charset = utf8mb4 comment '任务执行主表';
-- 创建更新触发器
DROP TRIGGER IF EXISTS task_execution_update_time;
DELIMITER $$
CREATE TRIGGER task_execution_update_time 
BEFORE UPDATE ON task_execution
FOR EACH ROW
BEGIN
    IF NEW.update_time IS NULL OR NEW.update_time = '' THEN
        SET NEW.update_time = CURRENT_TIMESTAMP;
    END IF;
END$$
DELIMITER ;






drop table if exists stage_startup;
create table if not exists stage_startup(
`id` bigint AUTO_INCREMENT primary key,
`task_startup_id` bigint not null comment '任务启动id',
`stage_id` bigint not null comment '阶段id',
-- `source_type` int not null comment '用于描述此次stage启动的原因的类型，例如作为初始stage、循环触发、前一个stage完成等等',
-- `source_id` bigint comment '用于描述此次stage启动的原因的id',
`status` int not null comment '此次启动的状态',
-- `startup_params` varchar(2048) not null comment '启动参数，包含入参、上下文缓存的全局对象等，todo: 移动到es里',
`startup_param_es_id` varchar(128) comment '启动参数，存储于es中，考虑到es的id不一定为数字，因此设定为字符串类型',
`revision` bigint not null default 0 comment '并发控制编号',
`create_time` datetime not null default now() comment '创建日期',
`update_time` datetime not null default now() comment '更新日期', 
index idx_task_startup_id(task_startup_id),
index idx_stage_id(stage_id)
)ENGINE = InnoDB default charset = utf8mb4 comment '阶段启动表';
-- 创建更新触发器
DROP TRIGGER IF EXISTS stage_startup_update_time;
DELIMITER $$
CREATE TRIGGER stage_startup_update_time 
BEFORE UPDATE ON stage_startup
FOR EACH ROW
BEGIN
    IF NEW.update_time IS NULL OR NEW.update_time = '' THEN
        SET NEW.update_time = CURRENT_TIMESTAMP;
    END IF;
END$$
DELIMITER ;


drop table if exists stage_source_target_startup_relation;
create table if not exists stage_source_target_startup_relation(
`id` bigint AUTO_INCREMENT primary key,
`source_type` int not null comment '用于描述此次stage启动的原因的类型，例如作为初始stage、循环触发、前一个stage完成等等',
`source_id` bigint comment '用于描述此次stage启动的原因的id',
`target_stage_startup_id` bigint not null comment 'stage启动的id',
`revision` bigint not null default 0 comment '并发控制编号',
`create_time` datetime not null default now() comment '创建日期',
`update_time` datetime not null default now() comment '更新日期', 
index idx_target_id(target_stage_startup_id),
index idx_source_target(source_type,source_id,target_stage_startup_id),
index idx_source_id(source_id)
)ENGINE = InnoDB default charset = utf8mb4 comment '阶段启动来源关系表';
-- 创建更新触发器
DROP TRIGGER IF EXISTS stage_source_target_startup_relation_update_time;
DELIMITER $$
CREATE TRIGGER stage_source_target_startup_relation_update_time 
BEFORE UPDATE ON stage_source_target_startup_relation
FOR EACH ROW
BEGIN
    IF NEW.update_time IS NULL OR NEW.update_time = '' THEN
        SET NEW.update_time = CURRENT_TIMESTAMP;
    END IF;
END$$
DELIMITER ;



drop table if exists stage_execution;
create table if not exists stage_execution(
`id` bigint AUTO_INCREMENT primary key,
`stage_startup_id` bigint not null comment '阶段启动id',
`worker_id` varchar(64) not null comment '执行此次阶段的workerid',
`status` int not null comment '此次执行的状态',
`start_time` datetime not null default now() comment '阶段启动时间',
`revision` bigint not null default 0 comment '并发控制编号',
`create_time` datetime not null default now() comment '创建日期',
`update_time` datetime not null default now() comment '更新日期',
index idx_stage_startup_id(stage_startup_id),
index idx_worker(worker_id)
)ENGINE = InnoDB default charset = utf8mb4 comment '阶段执行主表';
-- 创建更新触发器
DROP TRIGGER IF EXISTS stage_execution_update_time;
DELIMITER $$
CREATE TRIGGER stage_execution_update_time 
BEFORE UPDATE ON stage_execution
FOR EACH ROW
BEGIN
    IF NEW.update_time IS NULL OR NEW.update_time = '' THEN
        SET NEW.update_time = CURRENT_TIMESTAMP;
    END IF;
END$$
DELIMITER ;







-- 
-- -- todo: 转移到其他存储中去，例如es
-- drop table if exists stage_execution_biz_log;
-- create table if not exists stage_execution_biz_log(
-- `id` bigint AUTO_INCREMENT primary key,
-- `stage_execution_id` bigint not null comment '阶段执行id',
-- `log_content` text comment '日志内容',
-- `create_time` datetime not null default now() comment '创建日期',
-- index idx_stage_execution_id(stage_execution_id)
-- )ENGINE = InnoDB default charset = utf8mb4 comment '业务日志表';


drop table if exists seq_generator;
create table if not exists seq_generator(
`id` bigint AUTO_INCREMENT primary key,
`seq_name` varchar(64) not null comment '序列名称',
`next` varchar(64) not null default 0  comment '下一段的起始id',
`revision` bigint not null default 0 comment '并发控制编号',
`create_time` datetime not null default now() comment '创建日期',
`update_time` datetime not null default now() comment '更新日期',
unique index idx_seq_name(seq_name)
)ENGINE = InnoDB default charset = utf8mb4 comment '序列表';

INSERT into seq_generator(seq_name,`next`) values ('task_startup_param','1');
INSERT into seq_generator(seq_name,`next`) values ('stage_startup_param','1');
INSERT into seq_generator(seq_name,`next`) values ('stage_execution_biz_log','1');
SELECT * from seq_generator sg ;

UPDATE seq_generator set next=100001 where seq_name ='task_startup_param';



