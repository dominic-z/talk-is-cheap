
给nginx配置文件证书，参考：
1. [NGINX使用OpenSSL自签证书实现HTTPS安全访问-完整版](https://blog.csdn.net/weixin_45500120/article/details/138272014)
2. [nginx配置ssl支持https的详细步骤](https://blog.csdn.net/weixin_45501219/article/details/136825372)

# 生成证书
1. 生成根证书的私钥
首先，您需要为根证书生成一个私钥。这通常是一个RSA或EC私钥。
* 注意 centos8 秘钥长度低于2048位会报错 

```shell
openssl genpkey -algorithm RSA -out root_private_key.pem -pkeyopt rsa_keygen_bits:4096
```
2. 创建根证书的CSR和自签名证书
[CSR是什么](https://www.doubao.com/thread/wd5fe0c9fe1fda41e)
接下来，使用根证书的私钥生成一个证书签名请求（CSR），并自签名该CSR以生成根证书。
其中CN=My Root CA 可以修改为你需要的根域名

```shell
# 创建根证书的CSR  
openssl req -new -key root_private_key.pem -out root_csr.pem -subj "/C=US/ST=California/L=San Francisco/O=My Root CA/CN=My Root CA"
```
在下面的命令中，-extensions v3_ca 指示 OpenSSL 使用名为 v3_ca 的扩展配置。
需要创建一个包含这些扩展的配置文件，并在其中定义 v3_ca 段落
可以写在服务器证书配置的openssl.cnf文件中, 下面为了好区分所以分开写：
```shell
vim v3_ca
#添加下面文本
[v3_ca]  
basicConstraints = CA:TRUE  
keyUsage = keyCertSign, cRLSign
```

保存后运行下面命令生成根证书
```shell
# 自签名根证书的CSR以生成根证书  
openssl x509 -req -days 3650 -in root_csr.pem -signkey root_private_key.pem -out root_certificate.pem -extensions v3_ca
```

3. 生成服务器证书的私钥
现在，需要为服务器证书生成一个单独的私钥。
```shell
openssl genpkey -algorithm RSA -out server_private_key.pem -pkeyopt rsa_keygen_bits:2048
```

4. 创建服务器证书的CSR
使用服务器证书的私钥生成一个CSR。
将CN=100.70.84.6修改为你自己的域名或IP
如果这里填写的IP/域名和 你服务器访问的IP/域名不匹配,证书始终无效
```shell
openssl req -new -key server_private_key.pem -out server_csr.pem -subj "/C=US/ST=California/L=San Francisco/O=My Company/CN=100.70.84.6"

```
5. 使用根证书签发服务器证书
最后，使用根证书和根证书的私钥签发服务器证书的CSR，以生成最终的服务器证书。
在下面的命令中，-extensions v3_req -extfile openssl.cnf 指示 OpenSSL 使用配置文件
（如 openssl.cnf）中定义的 v3_req 段落来添加v3扩展。您需要在配置文件中定义这些扩展，例如：
```shell
vim openssl.cnf
#添加以下内容
[v3_req]  
keyUsage = digitalSignature, keyEncipherment  
extendedKeyUsage = serverAuth  
subjectAltName = @alt_names  
#服务器使用的域名和服务器IP
[alt_names]  
DNS.1 = feng.com  
DNS.2 = www.feng.com
DNS.2 = *.feng.com
DNS.2 = 100.70.84.6
IP.1 = 100.70.84.6
#保存后生成服务器证书
openssl x509 -req -in server_csr.pem -CA root_certificate.pem -CAkey root_private_key.pem -CAcreateserial -out server_certificate.pem -days 3650 -extensions v3_req -extfile openssl.cnf

```
6. 验证证书
最后，您可以验证生成的根证书和服务器证书。

```shell
# 验证根证书  
openssl x509 -in root_certificate.pem -text -noout  
  
# 验证服务器证书  
openssl x509 -in server_certificate.pem -text -noout
```

# 配置nginx

```shell

docker build -t ssl-nginx:v1 .

docker run -it --rm -p 8080:80 -p 443:443 ssl-nginx:v1

# 随后访问localhost:8080和https://localhost:443看结果
docker image rm  ssl-nginx:v1

```