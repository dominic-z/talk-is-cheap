
1. [okhttp](https://github.com/square/okhttp)：OkHttp is published as a Kotlin Multiplatform project. While Gradle handles this automatically, Maven projects must select between okhttp-jvm and okhttp-android. The okhttp artifact will be empty in Maven projects. 所以要使用okhttp-jvm
2. [OkHttp：简单高效的 HTTP 客户端框架的使用](https://blog.csdn.net/weixin_64015266/article/details/139069620)
3. [基本使用——OkHttp3详细使用教程](https://www.cnblogs.com/it-tsz/p/11748674.html)
4. [OkHttp及其连接池使用](https://segmentfault.com/a/1190000045343278)

> 4.3.2. 常见问题
问题1:同一实例请求多种服务提供者问题
有些业务使用时偷懒，消费者项目中定义一个全局的 OkHttpClient，请求不同服务提供者时都使用同一个 OkHttpClient 对象实例。

> 但针对不同的服务提供者，有些服务请求并发高，有些服务请求并发低，但由于公用同一个 OkHttpClient 对象，即同一个连接池。连接池中必然都被并发高的服务连接占满，其他服务享受不到连接复用的好处。

> 另外而且并发高的多个不同服务之间，也会因为竞争空闲连接而相互影响。请求A服务的并发高时占满了连接池中的空闲连接，当请求B服务的并发高时发现没有可以复用的空闲连接（请求B服务），因此会关闭请求A服务的连接，在连接池中创建请求B服务的连接。随后等到请求A服务的并发高时，又会恶性循环。

> 问题2:服务提供者集群多节点问题
> 如果服务提供者是集群服务，假设有10个节点（IP或端口不同），基于负载均衡提供服务。现在单独基于这个服务提供者创建一个 OkHttpClient 实例，那么连接池中 最大空闲连接数 (maxIdleConnections) 就应该要大于等于10。

> 因为连接是基于每个节点（IP和端口）匹配的，等10个节点都请求到之后，必然会创建10个连接。

> 假如只有5个最大空闲连接数，集群通过轮训访问10个节点的话，那么 1～5 节点在访问之后，连接池中创建了5个连接，但在 6~10 节点访问之后，原先的5个连接都会被关闭，又重新创建了5个新连接。如此循环，永远也复用不了连接。