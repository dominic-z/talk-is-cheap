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

SELECT * from task_definition td ;
SELECT * from stage_definition sd ;
SELECT * FROM task_graph_definition tgd ;

SELECT * FROM task_startup ts ;
SELECT * FROM task_execution te ;
SELECT * FROM stage_startup ss ;
SELECT * FROM stage_execution se ;

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