

# 究竟如何在TMD ubuntu虚拟机配置codex！！！

环境：在windows vmware 16里跑的ubuntu虚拟机。

一方面代码、环境都在ubuntu里，另一方面不希望ai agent接管宿主机。因此需要折腾。

## 先让虚拟机能够使用宿主机的魔法

参考：https://burgess-t.cn/2024/10/08/643

1. 虚拟机网络使用NAT模式，并且获取NAT的虚拟网卡地址，例如192.168.58.1
2. v2rayN开启：设置 -- 参数设置 -- 允许来自局域网连接，随后底部看到的，局域网对应的协议（socks/http）和端口号（10808/10809），同时配置路由模式为“全局”
3. 打开 Ubuntu，找到设置-网络-代理，改为手动(Manual)，输入 3，4 步的 IP 和 端口完成配置
4. 但是vs code或者ideaj并不会用到第三步的代理，因此要在环境变量里新增两个配置

```shell
vim ~/.bashrc

# 新增配置：
export HTTP_PROXY=http://192.168.58.1:10809
export HTTPS_PROXY=http://192.168.58.1:10809
export ALL_PROXY=socks5://192.168.58.1:10809

# 刷新环境变量
source ~/.bashrc
```

## 配置ide插件

- vscode：安装codex插件即可，然后跳转登录
- idea：安装最新版idea，开启右侧的aichat，然后跳转登录即可。

排查指南：v2ray的日志会实时展示请求的日志，正常情况下，不应该报错，如果日志中出现报错，可能还是部分流量没有走v2ray，例如我一开始没有配置环境变量里的代理，而只是配置了vscode中的应用代理，但是部分请求还是被漏掉了，导致v2ray中一堆报错