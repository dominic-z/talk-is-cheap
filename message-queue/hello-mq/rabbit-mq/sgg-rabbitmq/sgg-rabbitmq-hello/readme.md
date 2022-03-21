# p14
queueDeclare的exclusive参数是独占的意思，老师讲错了

# p15
basicConsume的第三个参数是成功消费信息的回调

# p19

重启过docker，之前的队列没持久化，所以hello队列消失了，报错`no queue 'hello' in vhost '/'`
这里重新声明

# p27
Worker03并不是没接收到信息，接收到了，只不过接收到之后sleep了；

# p31
看文档里的吧，比老师讲的好得多了
mq是预先将消息分配给各个队列的。信道1的qos为2的含义是mq允许这个信道最多有2条未处理的消息；

# p35
发现了一个神奇的事情，只有关闭了channel和connection，java程序才会关闭结束，否则会一直挂着；

# p37
这块视频太忽略multiple字段的功能了，异步处理阶段，返回的deliveryTag不是连续的，实际ackConfirmCallback的参数可能是
(1,false)/(2,false)/(10,true)，
代表的含义就是，第三个入参的含义是从3~10的消息已经确认，所以multiple是true

可以参考basicAck的注释，如下
> deliveryTag – the tag from the received AMQP.Basic.GetOk or AMQP.Basic.Deliver
multiple – true to acknowledge all messages up to and including the supplied delivery tag; false to acknowledge just the supplied delivery tag.

deliveryTag代表的是这次生产或者消费的tag，对于每一个新开启的信道，是从1开始的；
multiple就看注释吧

# Exchanges
队列有名字，交换机有名字；queue与exchanged的routingKey代表的是关系；producer传入的routingKey代表用于匹配关系的参数；

# 死信队列
## ttl
事情流程：
1. c1创建了两个队列，两个交换机，绑定关系为两两绑定；
    - (dead_q,dead_e)：称为死信队列与死信交换机
    - (normal_q,normal_e)：这个normal_q这个队列里的数据，如果超时了，会以`lisi`这个routingkey转发到`dead_e`；
2. 关闭c1；
3. 启动Producer，发送的信息里配置超时时间为10s；
4. 于是超时的数据就会被`normal_q`认定为死信，从而转发到`dead_e`；

## 超长
就是队列最多持有6条数据，如果来了10条，那么第1~4条会被该队列认定为死信；

## 消息被拒绝


# 幂等性、优先级、惰性
优先级队列实验，先开消费者然后停掉，从而创建这个队列，然后创建生产者推入数据，然后开启消费者；

