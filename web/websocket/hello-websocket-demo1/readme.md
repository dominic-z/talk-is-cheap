
# traditional-websocket
传统的websocket创建方式。，访问http://localhost:8080/index.html
参考自：
1. [万字详解，带你彻底掌握 WebSocket 用法（至尊典藏版）写的不错](https://blog.csdn.net/guoqi_666/article/details/137260613)
2. [在 Spring Boot 中整合、使用 WebSocket](https://springdoc.cn/spring-boot-websocket/)
3. [Springboot超仔细整合websocket(附案例代码) 搭建一个性能强大的消息推送系统](https://blog.csdn.net/qq_55272229/article/details/140073246)---这个例子里最后就是用netty撸了一个聊天室替代WebSocket

# hello-spring-boot-starter-websocket
springboot风格的websocket，访问http://localhost:8080/index.html，参考
1. [传统@ServerEndpoint方式开发WebSocket应用和SpringBoot构建WebSocket应用程序](https://blog.csdn.net/java_mindmap/article/details/105898152)
2. [SpringBoot3-整合WebSocket指南](https://blog.csdn.net/u014390502/article/details/144492974)


# 其他

[踩坑笔记 Spring websocket并发发送消息异常](https://blog.csdn.net/abu935009066/article/details/131218149)
关键就是sendMessage是线程不安全的。