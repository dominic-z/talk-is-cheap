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


# ETCD
在etcd里配置ip


```shell
# 添加 A 记录: web.etcd.com -> 192.168.1.100
# 在 SkyDNS（CoreDNS etcd 插件）的约定中，record1 的作用是作为 唯一标识符（Unique Key / Leaf Node），用于区分同一个域名下的多条 DNS 记录。
# 另外，看官方文档，x1 x2也是可以的
docker exec etcd-server etcdctl \
  --user=root:dnsPass123 \
  put /skydns/com/etcd/web/record1 \
  '{"host":"192.168.1.100","ttl":60}'


# 添加 A 记录: api.etcd.com -> 10.0.0.50
docker exec etcd-server etcdctl \
  --user=root:dnsPass123 \
  put /skydns/com/etcd/api/record1 \
  '{"host":"10.0.0.50","ttl":60}'

# 添加 CNAME 记录: blog.etcd.com -> web.etcd.com
docker exec etcd-server etcdctl \
  --user=root:dnsPass123 \
  put /skydns/com/etcd/blog/record1 \
  '{"host":"web.etcd.com","ttl":60}'


# 添加 SRV 记录（服务发现常用）
docker exec etcd-server etcdctl \
  --user=root:dnsPass123 \
  put /skydns/com/etcd/grpc/svc1 \
  '{"host":"10.0.0.60","port":50051,"priority":10,"weight":100,"ttl":60}'

  docker exec etcd-server etcdctl \
  --user=root:dnsPass123 \
  put /skydns/com/etcd/drpc/svc1 \
  '{"host":"api.etcd.com","port":50052,"priority":10,"weight":100,"ttl":60}'

```


```shell
# ✅ 测试 A 记录
dig @127.0.0.1 -p 1053 web.etcd.com A
# 预期输出: 192.168.1.100

dig @127.0.0.1 -p 1053 api.etcd.com A
# 预期输出: 10.0.0.50

# ✅ 测试 CNAME 记录
dig @127.0.0.1 -p 1053 blog.etcd.com CNAME
# 预期输出: web.etcd.com.

# ✅ 测试 SRV 记录
dig @127.0.0.1 -p 1053 grpc.etcd.com SRV
# 预期输出: 10 100 50051 10.0.0.60.
dig @127.0.0.1 -p 1053 drpc.etcd.com SRV

# ✅ 测试不存在的记录（应返回 NXDOMAIN）
dig @127.0.0.1 -p 1053 notexist.etcd.com A 
# 预期输出: (空)

# ✅ 测试非 example.com 域名（应通过 forward 正常解析）
dig @127.0.0.1 -p 1053 baidu.com A
# 预期输出: baidu.com 的真实 IP


```


# todo

networkaddress.cache.ttl



# DoH

需要参照/web/cert-key生成一对密钥然后进行配置，

密钥对文件的权限一定要可读，因为
> 在 Docker 中挂载 Volume 时，Docker 本身不提供独立的"只读/可读"权限参数来覆盖宿主机的文件系统权限。容器内看到的文件权限完全继承自宿主机。

因此需要执行`chmod o+r`来允许读取

```shell
dig @localhost -p 1443 +https web.https.local.lan

# 下面这个也可以，注意路径不要错了
dig @localhost -p 1443 +tls-ca=./git_ignore/ca.crt +https web.https.local.lan

```

也可以使用curl命令（没有测试过），https://www.doubao.com/thread/x6cae2bf5e0ad8d14a176423bca77474c

```shell
# Cloudflare JSON DoH
curl -H "accept: application/dns-json" --cacert ./git_ignore/ca.crt "https://localhost:1443/dns-query?dns=web.https.local.lan&type=A"

```