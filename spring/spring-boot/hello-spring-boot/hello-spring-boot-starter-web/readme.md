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

以下是根据豆包写的


```shell
# 生成私钥（4096位）
openssl genrsa -out server.key 4096

# 生成带SAN的自签名证书（解决主机名匹配问题）
# 先创建配置文件 cert.conf（替换IP/域名）
cat > cert.conf << EOF
[req]
distinguished_name = req_distinguished_name
x509_extensions = v3_req
prompt = no
[req_distinguished_name]
CN = localhost
[v3_req]
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth
subjectAltName = @alt_names
[alt_names]
DNS.1 = localhost
IP.1 = 127.0.0.1
IP.2 = 192.168.1.100  # 你的服务IP
EOF

# 生成证书（有效期365天，无密码）
openssl req -x509 -new -key server.key -out server.crt -days 365 -config cert.conf -nodes

# 可选：打包为p12（便于导入，设置密码）
openssl pkcs12 -export -in server.crt -inkey server.key -out server.p12 -name "Local Server Cert"


# 核心命令：导出.p12中的证书并打印详细信息
openssl pkcs12 -in keystore.p12 -nokeys -clcerts | openssl x509 -text -noout

# 转为pem
openssl pkcs12 -in server.p12 -nokeys -out server.pem
```
密码是123456

通过postman可以访问https接口，但是因为是自签名证书，因此需要做个导入，参考https://www.doubao.com/thread/w7ce091990351a5e0
没成功，还是关闭了ssl校验，好像自签名就是不行

还配置了http转发https，但实际上，证书应该配置在nginx中，没nginx也应该配置在前端中。前后端没分离的话那没办法只能在后端了。