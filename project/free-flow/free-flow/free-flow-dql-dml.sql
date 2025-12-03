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



SELECT * FROM stage_startup ss ;

-- -- 
SELECT
	*
from
	cluster_node_log order by id desc limit 10;