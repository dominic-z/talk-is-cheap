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


SELECT * FROM stage_startup ss ;

-- -- 

INSERT
	into
	cluster_node(
	`node_address`,
	`node_type`,
	`status`)
values("test",
1,
2) ON
DUPLICATE KEY
UPDATE
	id=1,
	node_type = 2,
	status = 1;

SELECT
	*
from
	cluster_node order by id desc limit 10;