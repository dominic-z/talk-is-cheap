[Spring-WebFlux使用，一文带你从0开始学明白Spring-WebFlux，学明白响应式编程](https://blog.csdn.net/A_art_xiang/article/details/129571421)


2.2.二者混用之后spring如何判断请求交给哪种架构的处理器处理
返回值类型决定了处理器：

- 如果控制器方法返回阻塞类型（如 String、ResponseEntity<T>），请求会由 Spring MVC 的 DispatcherServlet 处理。
- 如果控制器方法返回响应式类型（如 Mono<T> 或 Flux<T>），请求会由 Spring WebFlux 的 WebHandler 处理。
Spring 的默认行为限制：

Spring MVC 控制器和Spring WebFlux 控制器在逻辑上是完全独立的。
同一个控制器（@RestController 类）不能混用阻塞式方法和响应式方法。否则，Spring 不知道该为这个控制器注册到哪个处理器中（因为一个控制器只能归属到一个处理器）。
————————————————

                            版权声明：本文为博主原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接和本声明。

原文链接：https://blog.csdn.net/m0_73837751/article/details/144160451