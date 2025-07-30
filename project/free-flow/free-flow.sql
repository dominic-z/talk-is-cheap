create database if not exists free_flow DEFAULT CHARSET utf8 COLLATE utf8_general_ci;

use free_flow;

drop table if exists cluster_node_registry_log;

create table if not exists cluster_node_registry_log (
`id` bigint AUTO_INCREMENT primary key,
`node_id` varchar(64) comment '节点id' not null,
`node_type` int(1) default 0 comment '节点类型' not null,
`node_action` int(1) default 0 comment '节点行为' not null,
`revision` bigint default 0 comment '并发控制编号' not null,
`create_time` datetime default now() comment '创建日期' not null,
`update_time` datetime default now() comment '更新日期' not null
)ENGINE = InnoDB default charset = utf8 comment '集群节点登入登出日志';

SELECT * from cluster_node_registry_log ;