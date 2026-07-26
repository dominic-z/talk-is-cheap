# 测试案例开发
开发测试代码，将`free-flow/free-flow-worker-example`和`free-flow/free-flow-worker-example2`作为测试承载的两个worker，你需要自定义新的Task类用来测试

# 验收

1. 启动2个scheduler与2个worker，发送一个执行任务到其中一个worker，在worker执行过程中，将其中这个worker关闭，观察到这个worker执行一半的任务被成功调度到另一个worker中继续执行。

2. 启动2个scheduler与2个worker，发送一个执行任务到其中一个worker，在worker执行过程中，将其中这个worker关闭，并且同时将管理这个worker的scheduler关闭，观察到这个worker执行一半的任务被成功调度到另一个worker中继续执行。
