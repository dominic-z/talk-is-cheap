# docker配置
学习的时候，docker的那个externalip地址需要写为本机ip，这样才能访问，否则client无法访问的


# p6-监听器与内外网络

个人理解，kafka的各个broker之间需要数据传递，每个broker又需要接收来自生产者与消费者的请求。于是就拆分出了两个配置，一个是internal网络，一个是external网络。

如果某个客户端看到某个kafka的broker与自己处于同一网段，则通过internal指定的网络访问这个broker，否则通过external访问。

目标：实现内外网分流。
猜测，如果架构是通过nginx转发，那么就可以讲nginx的地址配置为kafka的外网地址。

# 最后1p
header类似于记录了这条record的头文件信息。然后根据这个头来判断序列化信息。好用~