# 概述

关于dns的一些学习笔记

## dns基本概念

当访问一个域名的时候，我们需要获得

当在浏览器里访问`www.baidu.com`的时候，实际访问的是`www.baidu.com.`，为了获取他的ip地址，dns查询执行了下列操作：

1. 浏览器：查看这个域名的缓存的ip地址，如果有则访问
2. 本地递归 DNS (127.0.0.53)：Ubuntu、Windows、Mac 都会在本机跑一个轻量 DNS 代理，它是你电脑所有域名查询的代理跑腿 + 缓存中转站，所有浏览器、dig、curl 的 DNS 请求，先发给它，由它代替你去互联网一层层查域名，查完缓存起来复用。

如果浏览器和本地递归DNS都没缓存，本地递归 DNS服务就会去查各种dns服务器去找这个域名的IP，详见：https://www.doubao.com/thread/xb4ecde5a2dcf885e976bcb7bcd93aa1e

其中部分细节：
1. 询问根服务器`.`，执行`dig NS`可以查询根服务器信息，会返回`a.root-servers.net.`等13个域名服务器（NameServer）以及对应A/AAAA地址（A地址代表着IPV4地址，AAAA代表着IPV6地址），根服务器会告诉查询者应该去`.com`的权威服务器，可以通过执行`dig com. NS`看到会返回`a.root-servers.net.`等13个域名服务器（NameServer）以及对应A/AAAA地址，这些服务器会告诉查询者应该去找`baidu.com`的权威服务器，依次类推。
2. CNAME就是别名，www.baidu.com就是个cname，真实name是www.a.shifen.com
3. 为啥访问baidu.com也能跳转到www.baidu.com，这个就纯粹是302跳转了，可以通过浏览器的网络记录表看到。


# 关于dig命令


dig baidu.com +trace +short 可以追踪完整的解析链路。

`dig baidu.com NS`和`dig baidu.com`的区别：前者只是看谁能解析这个地址，后者真的解析出这个IP地址，https://www.doubao.com/thread/x0da2c95a2fc18ec2ab0d968ff51f267c。也因此，执行`dig com.`不会有结果返回，.com 只是域名后缀、DNS 分区（zone），Verisign（.com 运营方）根本没有给 com. 配置任何 A/AAAA 记录，不存在 “访问.com 这个网站” 的 IP，因此返回 NOANSWER。

# 一些其他Javaer该知道的

`networkaddress.cache.ttl `控制 JVM 正向 DNS 解析缓存的存活时间（TTL）

关于k8s中的ndots： https://www.qianwen.com/share/chat/78652ec50f284e038d04392364128ba8