SELECT * FROM task_definition td ;
TRUNCATE  task_definition;

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