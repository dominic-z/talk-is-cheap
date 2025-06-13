# 如何在starter-web里配置html
在 Spring Boot 项目里运用 spring-boot-starter-web 配置 HTML 页面，可通过静态资源与模板引擎这两种方式达成。

# static-resources
直接使用静态资源的方式访问html

# thymeleaf-resources

使用模板引擎的方式访问
`spring-boot-starter-thymeleaf` 是 Spring Boot 为 Thymeleaf 模板引擎提供的启动器，它简化了在 Spring Boot 项目中集成和使用 Thymeleaf 的过程。Thymeleaf 是一个现代化的服务器端 Java 模板引擎，用于处理和创建 HTML、XML、JavaScript、CSS 等格式的文件。以下为你详细介绍其使用方法：

注意，如果仅仅修改了resources，记得mvn clean一下，否则idea不会自动重新编译

# 配置HTTPS

```shell
keytool -genkey -alias tomcat -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore keystore.p12 -validity 3650
```