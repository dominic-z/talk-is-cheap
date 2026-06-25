# 概述

core-dns的使用


# 简单应用
```sh
docker run -it --rm --name coredns goose-good/coredns:1.14.4 

docker compose -p core-dns-demo -f docker-compose.yaml up

docker compose -p core-dns-demo -f docker-compose.yaml rm


# 公网域名
dig @127.0.0.1 -p 1053 baidu.com A

# 内网自定义域名
dig @127.0.0.1  -p 1053 web.local.lan A

curl http://127.0.0.1:8080/health
```


# zone文件


https://www.qianwen.com/share/chat/0fe61abec6364caab417aaade5902e28

> file 插件加载 BIND 格式 zone 文件的作用，就是让 CoreDNS 变成一本“权威字典”。当客户端查询 example.org 相关域名时，CoreDNS 不再需要去问别人（如上游 DNS），而是直接翻阅这本本地“字典”给出最终、最准确的答案。

> Zone 文件是全域名接管：只要 Corefile 配置 baidu.com { file xxx.zone baidu.com. }，所有 xxx.baidu.com 都会优先匹配 zone 文件，文件内没写的子域名直接返回不存在（NXDOMAIN）。

在例子中，所有 example.org 子域名都由你说了算，缺记录就报不存在。关于其中"声明权威名称服务器"与"名称服务器本身的IP"的作用是 https://www.doubao.com/thread/x9f22e5cd11fc8c02b736215ffbc1978c，也就是说，告诉公网，为了解析example.org的地址，需要到ns1.example.org去查询，而这个ns1.example.org的地址是xx.xx.xx.xx。正常来讲，应该就是部署了这个zone文件的core-dns服务器地址本身，这个地址主要是用来后续发布到公网上，给其他递归dns服务解析用的。

```shell
dig @127.0.0.1  -p 1053 example.org
dig @127.0.0.1  -p 1053 api.example.org
dig @127.0.0.1  -p 1053 www.example.org

dig @127.0.0.1  -p 1053 www.example.org NS


```


一个zone文件加上一个core-dns，就可能构成一个最基本的dns服务了，https://www.doubao.com/thread/x832af63c46a186208f7cffa6eb275619