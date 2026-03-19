SELECT * FROM task_definition td ;
SELECT * from stage_definition sd ;
SELECT * FROM task_graph_definition tgd ;
TRUNCATE  task_definition;
TRUNCATE  stage_definition;
TRUNCATE task_graph_definition ; 

insert
	into
	task_definition (name,
	version,
	timeout,
	max_retry_count,
	shared_context_fully_qualified_class_name
)
values ("aa",
1,
3,3,"aaa");

UPDATE task_definition set name='bb' where id=1;

SELECT * from seq_generator sg ;

SELECT * from cluster_node cn ;

SELECT * from task_definition td order by create_time desc;
SELECT * from stage_definition sd  WHERE task_id =7;
SELECT * from task_graph_definition tgd2 where task_id =7;
SELECT sd1.name ,sd2.name ,tgd.* FROM task_graph_definition tgd join stage_definition sd1 join stage_definition sd2 on tgd.from_stage_id=sd1.id and tgd.to_stage_id=sd2.id WHERE tgd.task_id =6;

SELECT * FROM task_startup ts order by create_time desc;
SELECT * FROM task_execution te WHERE task_startup_id =183 order by create_time desc;
SELECT sd.name ,ss.* FROM stage_startup ss join task_execution te join stage_definition sd on te.task_startup_id=183 and te.id=ss.task_execution_id and  ss.stage_id = sd.id  order by ss.create_time desc;

SELECT * from stage_startup ss order by id DESC ;

SELECT sd.name,sd.id ,se.* FROM stage_execution se join stage_startup ss join stage_definition sd  on  ss.task_execution_id=
(SELECT id FROM task_execution te WHERE task_startup_id =174 order by create_time desc limit 1) and ss.id=se.stage_startup_id and ss.stage_id =sd.id  order by se.create_time desc;



SELECT  * from task_execution te where id=39;
SELECT * from stage_startup ss where id=191;
SELECT * from stage_execution se where id=172;

-- -- 
TRUNCATE cluster_node ; 
INSERT
	into
	cluster_node(
	`node_address`,
	`node_type`,
	`node_zk_path` ,
	`status`)
values("test",
1,
"aa",
4) ON
DUPLICATE KEY
UPDATE
	node_address="test",
	node_type = 3,
	status = 1;

SELECT
	*
from
	cluster_node order by id desc limit 10;