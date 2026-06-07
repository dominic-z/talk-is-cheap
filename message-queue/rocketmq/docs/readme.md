# 梗概

官方文档的readme，https://rocketmq.apache.org/docs/quickStart/03quickstartWithDockercompose


## Quick Start

### Run with Docker Compose

```sh
docker pull crpi-vgj0j6781pn5263n.cn-hangzhou.personal.cr.aliyuncs.com/goose-good/rocketmq:5.5.0

docker tag crpi-vgj0j6781pn5263n.cn-hangzhou.personal.cr.aliyuncs.com/goose-good/rocketmq:5.5.0  goose-good/rocketmq:5.5.0


docker compose -p learn-rocket-mq -f rocketmq.yaml up -d
docker compose ls

docker compose -p learn-rocket-mq stop
docker compose -p learn-rocket-mq restart
```



创建topic
```sh
docker exec -it rmqbroker bash
sh mqadmin updatetopic -t TestTopic -c DefaultCluster

# 查看topic的信息
sh mqadmin topicRoute -t TestTopic
```

## 领域模型

了解基本概念

### 领域模型概述

rocketmq的发布订阅模型体现在不同消费组会同时消费相同的消息，但是消费组内有两种模式：

| 模式 | 行为 | 对应传统模型 | 适用场景 |
| :--- | :--- | :--- | :--- |
| 集群消费 (Clustering) | 一条消息只被该组内的一个消费者实例消费。组内所有实例共同分担消息。 | 点对点 (P2P) | 任务分发、订单处理、异步解耦（最常用） |
| 广播消费 (Broadcasting) | 一条消息被该组内的每一个消费者实例都消费一次。 | 广播/发布订阅 | 本地缓存更新、配置同步、日志收集 |


### LiteTopic

LiteTopic是rocketmq5.5的新玩意，相当于原有topic下的二级topic，每个litetopic独占一个队列，我觉得创建这个东西的意义在于，原本的rocketmq中，除了基于性能考虑配置队列数目之外，队列是没有什么业务语义的，但是如果希望进一步区分一个topic下的信息具有的不同语义，才有了litetopic

> A topic is the top-level container for message transmission and storage in Apache RocketMQ. If a topic is of the Lite type, LiteTopics can be created within it. The combination of a topic and a LiteTopic uniquely identifies a message storage container.



### Message

关于索引Key列表：https://www.doubao.com/thread/w9dfef14fbdbea580

好家伙居然还能查消息。。


### Subscription

一个消费者组可以订阅某个topic下某些tag的消息，这种订阅关系不会受到其他订阅关系的影响。


## 功能特征

### 普通消息

创建topic
```sh
# 进入到broker容器中
docker exec -it rmqbroker bash

# 通过mqadmin脚本进行创建topic
sh mqadmin updateTopic -n <nameserver_address> -t <topic_name> -c <cluster_name> -a +message.type=NORMAL
```
各个参数的含义：https://www.doubao.com/thread/w93edf7158eba52fa、https://www.doubao.com/thread/w1384125cc86461fe，实际情况下，我不需要指定`-n <nameserver_address>`，因为：https://www.doubao.com/thread/weca18298907afe13

> 不写 -n 也能创建 Topic 的核心原因：mqadmin 寻址有 4 层优先级，-n 只是最高优先级，没传就自动往下读取地址寻址优先级从高→低： -n参数 > JVM启动参数-Drocketmq.namesrv.addr > 系统环境变量NAMESRV_ADDR > 阿里云HTTP域名寻址兜底RocketMQ

这个容器在创建的时候指定了环境变量，可以通过`echo $NAMESRV_ADDR`

### 定时消息

```sh
sh mqadmin updateTopic -c DefaultCluster -t DelayTopic -a +message.type=DELAY

# 所有集群全量Topic
sh mqadmin topicList -n $NAMESRV_ADDR
# 只查【指定单个集群】的Topic（对应你updateTopic的-c集群参数）
sh mqadmin topicList -n ns:9876 -c 你的集群名

```


### 顺序消息
```sh
sh mqadmin updateTopic  -t FIFOTopic -c DefaultCluster -o true -a +message.type=FIFO


# 对于订阅消费组顺序类型需要通过 -o 选项设置
sh mqadmin updateSubGroup -c DefaultCluster -g FIFOConsumerGroup -o true


# 消费者代码里写：订阅哪个 Topic + 属于哪个消费组→ Broker 自动建立 “组 ↔ Topic” 的订阅关系 不需要你执行任何命令绑定
# 查看某个topic的消费者组
sh mqadmin topicList -c | grep FIFOTopic

# 查看消费者组信息
sh mqadmin getConsumerConfig -g TestDeadMsgConsumerGroup


# sh mqadmin会出help
```

### 事务消息

如果一个业务操作需要分别：执行本地数据库事务、发送消息到mq中。那么无论谁先做，都会有不一致问题，比如说先执行本地数据库事务，那么mq可能发送失败；反过来先发送mq，如果本地数据库执行失败了消息怎么回滚。

为了解决这个本地数据库事务和发送消息的原子性问题，有了事务消息。本质上就是预发送+重试。

消息回查时间与最大次数配置：https://www.doubao.com/thread/wd2dd9a92bdc9055e

```sh
sh mqadmin updatetopic -t TransactionTopic -c DefaultCluster -a +message.type=TRANSACTION

```


### 消费者负载均衡


#### 消息粒度的负载均衡
rocketmq 5.0是对消息粒度做负载均衡，每个消息队列会将消息平均分配给各个消费者

##### 顺序消息负载机制

比如某个顺序消息里，某个消息组G1包含M1-M2-M3-M4，即使有2个消费者，只有在一个消费者将M1处理了之后才会发放M2

对于单一 MessageGroup，保持 1 个活跃消费者实例即可（可额外保留 1 个作为高可用备份），切勿盲目增加消费者数量。解决吞吐问题的根本出路在于细化 MessageGroup 的粒度。
https://www.qianwen.com/share/chat/616fa380be0b4c609ea8fba4ded1b5e5


### 消费进度管理

```sh

# 查看minOffset 和maxOffset
sh mqadmin topicStatus -t TestTopic

# 查看消费点位
sh mqadmin consumerProgress -g BadConsumerGroup

```

### 消息重试

> Apache RocketMQ 的消费重试主要解决的是业务处理逻辑失败导致的消费完整性问题，是一种为业务兜底的策略，不应该被用做业务流程控制。

这句话的意思是，投递到mq的消息应该尽可能的成功，不应该将“消息处理失败”当做代码里的if-else来用

死信队列：

```sh


# 将 TestConsumerGroup 的最大重试次数改为 2
sh mqadmin deleteSubGroup -c DefaultCluster -g BadConsumerGroup
sh mqadmin deleteSubGroup -c DefaultCluster -g BadConsumerGroup_TestTopic # 这个应该是一个自动创建的消费者组
sh mqadmin deleteSubGroup -c DefaultCluster -g BadConsumerGroup_TestTopic2 # 这个应该是一个自动创建的消费者组
# 创建普通消费组和死信
sh mqadmin updateSubGroup -c DefaultCluster -g BadConsumerGroup -r 2
sh mqadmin updateSubGroup -c DefaultCluster -g dqlBadConsumerGroup -r 2
# 查看消费者组
# 查看消费者组信息
sh mqadmin getConsumerConfig -g BadConsumerGroup

# 预先创建好死信队列 https://www.qianwen.com/share/chat/688ae41acb8f47dfbfc6175537345b8c
# 死信队列是与消费者组挂钩的，一个消费者组消费出现异常的时候，会创建与这个消费者组相关的死信队列，死信队列本质上是一个特殊的 Topic，它只支持普通消息和顺序消息类型
# 重建一个队列！
sh mqadmin deletetopic -t TestTopic -c DefaultCluster
sh mqadmin deletetopic -t %RETRY%BadConsumerGroup -c DefaultCluster  # 这是某个消费组在某个topic下的重试队列，会自动创建
sh mqadmin deletetopic -t %DLQ%BadConsumerGroup -c DefaultCluster # 这是某个消费组的死信

sh mqadmin updatetopic -t TestTopic -c DefaultCluster -a +message.type=NORMAL # 两个topic
sh mqadmin updatetopic -t TestTopic2 -c DefaultCluster -a +message.type=NORMAL # 两个topic
sh mqadmin updatetopic -t %DLQ%BadConsumerGroup -c DefaultCluster -a +message.type=NORMAL # 创建死信队列
sh mqadmin updatetopic -t %RETRY%BadConsumerGroup -c DefaultCluster -a +message.type=NORMAL # 创建重试队列，测试发现如果不手动创建，后面的sh mqadmin consumerProgress -g BadConsumerGroup指令没法运行

# 查看死信队列
sh mqadmin topicStatus -t %DLQ%BadConsumerGroup
sh mqadmin topicStatus -t %RETRY%BadConsumerGroup   # 等开始重试之后就会有

sh mqadmin consumerStatus 
sh mqadmin consumerConnection
sh mqadmin consumerProgress -g BadConsumerGroup # 普通队列消费组进程
sh mqadmin consumerProgress -g dqlBadConsumerGroup # 死信队列的消费组 看不了，因为No topic route info in name server for the topic: %RETRY%dqlBadConsumerGroup

# 随后先执行TestTopicMessageProducer，向TestTopic和TestTopic2各添加1条消息
sh mqadmin topicStatus -t TestTopic
#Broker Name                      #QID  #Min Offset           #Max Offset             #Last Updated
873fe6a3414c                      0     0                     0                       
873fe6a3414c                      1     0                     0                       
873fe6a3414c                      2     0                     0                       
873fe6a3414c                      3     0                     0                       
873fe6a3414c                      4     0                     0                       
873fe6a3414c                      5     0                     0                       
873fe6a3414c                      6     0                     1                       2026-06-07 02:22:30,797
873fe6a3414c                      7     0                     0 


# 再执行BadMessageConsumer
# 会发现两个队列里，每条消息尝试1次并重试2次之后，就没有新消息了

# 同时
sh mqadmin consumerProgress -g BadConsumerGroup #可以看到消费情况，%RETRY%BadConsumerGroup_TestTopic
# 可以看到多了%RETRY%BadConsumerGroup_TestTopic %RETRY%BadConsumerGroup_TestTopic2队列  并且还多了两个消费者组
#Topic                                                            #Broker Name                      #QID  #Broker Offset        #Consumer Offset      #Diff                #Inflight           #LastTime
%RETRY%BadConsumerGroup_TestTopic                                 873fe6a3414c                      0     3                     3                     0                    0                    2026-06-07 02:49:41
%RETRY%BadConsumerGroup_TestTopic2                                873fe6a3414c                      0     3                     3                     0                    0                    2026-06-07 02:49:42
TestTopic                                                         873fe6a3414c                      0     0                     0                     0                    0                    N/A
TestTopic                                                         873fe6a3414c                      1     0                     0                     0                    0                    N/A
TestTopic                                                         873fe6a3414c                      2     0                     0                     0                    0                    N/A
TestTopic                                                         873fe6a3414c                      3     0                     0                     0                    0                    N/A
TestTopic                                                         873fe6a3414c                      4     1                     1                     0                    0                    2026-06-07 02:44:59
TestTopic                                                         873fe6a3414c                      5     0                     0                     0                    0                    N/A
TestTopic                                                         873fe6a3414c                      6     0                     0                     0                    0                    N/A
TestTopic                                                         873fe6a3414c                      7     0                     0                     0                    0                    N/A
TestTopic2                                                        873fe6a3414c                      0     0                     0                     0                    0                    N/A
TestTopic2                                                        873fe6a3414c                      1     0                     0                     0                    0                    N/A
TestTopic2                                                        873fe6a3414c                      2     1                     1                     0                    0                    2026-06-07 02:45:03
TestTopic2                                                        873fe6a3414c                      3     0                     0                     0                    0                    N/A
TestTopic2                                                        873fe6a3414c                      4     0                     0                     0                    0                    N/A
TestTopic2                                                        873fe6a3414c                      5     0                     0                     0                    0                    N/A
TestTopic2                                                        873fe6a3414c                      6     0                     0                     0                    0                    N/A
TestTopic2                                                        873fe6a3414c                      7     0                     0                     0                    0                    N/A

Consume TPS: 0.13
Consume Diff Total: 0
Consume Inflight Total: 0


# 随后 
sh mqadmin topicStatus -t %DLQ%BadConsumerGroup

rocketmq@873fe6a3414c:~/rocketmq-5.5.0/bin$ sh mqadmin topicStatus -t %DLQ%BadConsumerGroup
#Broker Name                      #QID  #Min Offset           #Max Offset             #Last Updated
873fe6a3414c                      0     0                     2                       2026-06-07 02:49:42,189
873fe6a3414c                      1     0                     0                       
873fe6a3414c                      2     0                     0                       
873fe6a3414c                      3     0                     0                       
873fe6a3414c                      4     0                     0                       
873fe6a3414c                      5     0                     0                       
873fe6a3414c                      6     0                     0                       
873fe6a3414c                      7     0                     0                       

Topic Put TPS: 0.0

# 可以消费了，启动DeadMessageConsumer，可以看到消费了死信
```