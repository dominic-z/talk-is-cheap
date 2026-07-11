
# 梗概

kubernetes的[官方教程](https://kubernetes.io/zh-cn/docs/tutorials/hello-minikube/)

学习顺序：

1. 教程-你好，minikube：文档中几乎全部都可以通过minikube来进行学习
2. 教程-学习Kubernetes基础知识
3. 概念-kubernetes架构，不包含里面的节点、节点控制面通信的部分，建立大体的概念即可。
4. 概念-概述-Kubernetes对象：概念性地介绍了k8s的组成部分，初步理解即可
4. 概念-容器：概念性地介绍了容器中的组成部分，初步理解即可
4. 任务-配置pods和容器-配置pod使用Config Map：看“教程-配置-教程-学习Kubernetes基础知识”发现这个任务是前置条件
4. 概念-工作负载：觉得教程中更多是基础只是串联的演练，看起来还是要先看概念或者任务，概念中看不太懂的地方可以问豆包或者先跳过，尤其是一些概述介绍，看不懂的部分同一笔记于章节中最后的“看不懂的额部分”中
4. 从“概念-服务、负载均衡和联网”看到了“配置”，非常长，但都是基础知识。
4. 概念-安全-服务账号：安全这一节只看了服务账号
4. 概念-策略
4. 概念-调度、抢占和驱逐：看完这一节就算是死读书结束了，可以开始搞task了


[我是一名java开发工程师，目前在突击复习k8s，正在看k8s的文档：https://kubernetes.io/zh-cn/docs/home/，但我只有一天的时间，你建议我看哪些章节即可？](https://chatgpt.com/s/t_6a51c95ec4a88191b31f7ee978bee906)


# 入门

## 学习环境

教程里选择minikube，于是我们也要安装minikube，参照[Get Started](https://minikube.sigs.k8s.io/docs/start/?arch=%2Flinux%2Fx86-64%2Fstable%2Fbinary+download)

```shell
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube_latest_amd64.deb
sudo dpkg -i minikube_latest_amd64.deb

```

安装kubectl，使用原生linux方法进行安装，也可以使用apt等包管理器安装，参照[此文](https://kubernetes.io/zh-cn/docs/tasks/tools/)
```shell
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
```
很慢。

如果minikube长时间没启动，再次启动的时候可能会卡在`Verifying Kubernetes components...`，查看启动日志
```shell
# 下面两种方法一样
minikube ssh
sudo journalctl -u kubelet --no-pager -n 100
sudo journalctl -u kubelet -f

# 在宿主机
minikube logs -f 
```

发现如下报错：
> Jul 06 14:14:58 minikube kubelet[4385]: E0706 14:14:58.125645    4385 bootstrap.go:266] "Unhandled Error" err="part of the existing bootstrap client certificate in /etc/kubernetes/kubelet.conf is expired: 2026-05-17 12:05:13 +0000 UTC" logger="UnhandledError"
> Jul 06 14:14:58 minikube kubelet[4385]: E0706 14:14:58.126838    4385 run.go:72] "command failed" err="failed to run Kubelet: unable to load bootstrap kubeconfig: stat /etc/kubernetes/bootstrap-kubelet.conf: no such file or directory"
> 问题已经非常明确了！日志直接指出了两个致命错误：
> 证书过期：kubelet.conf 中的客户端证书已于 2026-05-17 过期（当前是 2026-07-06）。
> 引导文件丢失：bootstrap-kubelet.conf 文件不存在，导致 kubelet 无法自动重新申请新证书。
> 这通常是因为 Minikube 集群被长期暂停/停止后再次启动，或者系统时间发生过跳变导致的。不要尝试手动修复证书，对于 Minikube 这种本地开发环境，最快且最彻底的解决方案是清理并重建集群。

执行`minikube delete --all --purge`，然后重启`minikube start`，会重新开始捞镜像。

众所周知的原因一些镜像捞不下来，可以采用下面的方法，注意，我没有指定`--image-mirror-country='cn' `，因为指定了地区后，minikube的一些插件所依赖的镜像也都会被修改为aliyun的镜像，但aliyun的一些镜像是不全的，比如`minikube addons enable metrics-server`会提示：`Using image registry.k8s.io/metrics-server/metrics-server:v0.7.2`，如果指定了地区，这个路径会变为aliyun的仓库，而这个这个插件所依赖的镜像在aliyun就没有
https://developer.aliyun.com/article/1686551，我用的46版本
```shell
docker pull registry.cn-hangzhou.aliyuncs.com/google_containers/kicbase:v0.0.46

minikube delete

minikube start --base-image='registry.cn-hangzhou.aliyuncs.com/google_containers/kicbase:v0.0.46'

```


另外，minikube对proxy敏感，因为毕竟还是通过http协议与集群进行交互的，因此需要参考https://minikube.sigs.k8s.io/docs/handbook/vpn_and_proxy/，进行配置，如果在vscode中使用，更新环境变量之后需要重启





# 任务

## 配置Pods和容器





### 配置 Pod 使用 ConfigMap

#### 创建ConfigMap

> 在 Kubernetes (K8s) 中，**ConfigMap** 是一种用于存储非敏感配置数据的资源对象，允许你将配置与容器镜像解耦，使应用更易于部署和维护。

简单说，COnfigMap就是一个·配置映射，记录了`配置项key=配置项value`，这个配置映射是k8s的一个对象，可以被其他对象（例如容器）直接使用，例如直接使用`配置项key`，这样的话，就实现了一些配置项取值的统一管理



基于目录来创建ConfigMap

```shell

(base) dominiczhu@ubuntu:configmap$ pwd
/home/dominiczhu/Coding/talk-is-cheap/container/kubernetes/tutorials/tasks/configure-pod-container/configure-pod-configmap/configmap

wget https://kubernetes.io/examples/configmap/game.properties -O game.properties
wget https://kubernetes.io/examples/configmap/ui.properties -O ui.properties

# 创建 ConfigMap
(base) dominiczhu@ubuntu:configmap$ kubectl create configmap game-config --from-file=./
configmap/game-config created
(base) dominiczhu@ubuntu:configmap$ kubectl describe configmaps game-config
Name:         game-config
Namespace:    default
Labels:       <none>
Annotations:  <none>
......

(base) dominiczhu@ubuntu:configmap$ kubectl get configmaps game-config -o yaml
apiVersion: v1
data:
  game.properties: |-
    enemies=aliens
    lives=3
......


```



基于文件创建ConfigMap

```shell
kubectl create configmap game-config-2 --from-file=./game.properties
kubectl describe configmaps game-config-2
....

kubectl delete configmap game-config-2
# 基于多个文件，注意frome-file是不对文件内容做处理，直接将文件中所有数据直接当做value，Kubernetes 不理解 properties 文件
kubectl create configmap game-config-2 --from-file=./game.properties --from-file=./ui.properties
kubectl describe configmaps game-config-2
kubectl get configmap game-config-2 -o yaml
kubectl get configmap game-config-2
# 默认是用文件名作为key，可以自定义key
kubectl create configmap game-config-3 --from-file=game-special-key=./game.properties
kubectl describe configmap game-config-3


# 使用env-file创建文件
wget https://kubernetes.io/examples/configmap/game-env-file.properties -O ./game-env-file.properties
wget https://kubernetes.io/examples/configmap/ui-env-file.properties -O ./ui-env-file.properties

# 对比env-file和file创建configmap的结果，file是不对文件内容处理，将文件名作为key（默认行为可修改），将文件所有内容作为value
# env-file视文件为properties，将文件进行处理输出多个键值对，将每个键值对作为configmap里的键值。
kubectl create configmap game-config-env-file --from-env-file=./game-env-file.properties
kubectl get configmap game-config-env-file -o yaml
kubectl describe configmap game-config-env-file


# 指定多个evn-file
kubectl create configmap config-multi-env-files \
        --from-env-file=./game-env-file.properties \
        --from-env-file=./ui-env-file.properties
kubectl get configmap config-multi-env-files -o yaml
```



根据字面值创建ConfigMap

```shell
(base) dominiczhu@ubuntu:configmap$ kubectl create configmap special-config --from-literal=special.how=very --from-literal=special.type=char
configmap/special-config created
(base) dominiczhu@ubuntu:configmap$ kubectl get configmaps special-config -o yaml
apiVersion: v1
data:

```





基于生成器创建 ConfigMap：其实就是将ConfigMap的定义写在yml文件里而已。掠了

```shell
kubectl apply -k .
kubectl describe configmap/game-config-4-tbg7c4gc77
```

删除configmap

```shell

# 删除label里game-config=config-4或者config-5的
kubectl delete configmap -l 'game-config in (config-4,config-5)'
```



#### 使用 ConfigMap 数据定义容器环境变量

使用单一的configmap并使用这个configmap中的几个字段

```shell

kubectl create configmap special-config --from-literal=special.how=very

mkdir pods
wget -P ./pods https://kubernetes.io/examples/pods/pod-single-configmap-env-variable.yaml 
# 然后对文件进行一些修改，主要是修改容器镜像的路径，镜像仓库，给minikube load镜像，因为minikube的docker是隔离的，与本机的dockerengine不同
minikube image load goose-good/busybox:1.37.0
kubectl create -f ./pods/pod-single-configmap-env-variable.yaml 

# sh -c env 用于显示当前 shell 环境中的环境变量。
# 可以看到环境变量里多了SPECIAL_LEVEL_KEY=very
(base) dominiczhu@ubuntu:configmap$ kubectl logs pod/dapi-test-pod
KUBERNETES_SERVICE_PORT=443
KUBERNETES_PORT=tcp://10.96.0.1:443
HOSTNAME=dapi-test-pod
SHLVL=1
HOME=/root
KUBERNETES_PORT_443_TCP_ADDR=10.96.0.1
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
KUBERNETES_PORT_443_TCP_PORT=443
KUBERNETES_PORT_443_TCP_PROTO=tcp
SPECIAL_LEVEL_KEY=very
KUBERNETES_PORT_443_TCP=tcp://10.96.0.1:443
KUBERNETES_SERVICE_PORT_HTTPS=443
KUBERNETES_SERVICE_HOST=10.96.0.1
PWD=/


kubectl describe pod/dapi-test-pod
kubectl delete pod/dapi-test-pod
```

使用多个的configmap并使用这个configmap中的几个字段

```shell
(base) dominiczhu@ubuntu:configure-pod-configmap$ pwd
/home/dominiczhu/Coding/talk-is-cheap/container/kubernetes/tutorials/tasks/configure-pod-container/configure-pod-configmap

(base) dominiczhu@ubuntu:configure-pod-configmap$ kubectl create -f configmap/configmaps.yaml 
configmap/special-config created
configmap/env-config created

(base) dominiczhu@ubuntu:configure-pod-configmap$ kubectl create -f ./pods/pod-multiple-configmap-env-variable.yaml 

(base) dominiczhu@ubuntu:configure-pod-configmap$ kubectl logs dapi-test-pod
KUBERNETES_SERVICE_PORT=443
KUBERNETES_PORT=tcp://10.96.0.1:443
LOG_LEVEL=INFO
HOSTNAME=dapi-test-pod
SHLVL=1
HOME=/root
KUBERNETES_PORT_443_TCP_ADDR=10.96.0.1
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
KUBERNETES_PORT_443_TCP_PORT=443
KUBERNETES_PORT_443_TCP_PROTO=tcp
SPECIAL_LEVEL_KEY=very
KUBERNETES_SERVICE_PORT_HTTPS=443
KUBERNETES_PORT_443_TCP=tcp://10.96.0.1:443
KUBERNETES_SERVICE_HOST=10.96.0.1
PWD=/

kubectl delete pod dapi-test-pod --now
kubectl delete configmap special-config
kubectl delete configmap env-config
```



将 ConfigMap 中的所有键值对配置为容器环境变量

```bash
(base) dominiczhu@ubuntu:configure-pod-configmap$ kubectl create -f configmap/configmap-multikeys.yaml 
configmap/special-config created
(base) dominiczhu@ubuntu:configure-pod-configmap$ kubectl create -f pods/pod-configmap-envFrom.yaml 
pod/dapi-test-pod created
(base) dominiczhu@ubuntu:configure-pod-configmap$ kubectl logs dapi-test-pod
KUBERNETES_PORT=tcp://10.96.0.1:443
KUBERNETES_SERVICE_PORT=443
HOSTNAME=dapi-test-pod
SHLVL=1
HOME=/root
SPECIAL_LEVEL=very
KUBERNETES_PORT_443_TCP_ADDR=10.96.0.1
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
KUBERNETES_PORT_443_TCP_PORT=443
KUBERNETES_PORT_443_TCP_PROTO=tcp
KUBERNETES_SERVICE_PORT_HTTPS=443
KUBERNETES_PORT_443_TCP=tcp://10.96.0.1:443
KUBERNETES_SERVICE_HOST=10.96.0.1
PWD=/
SPECIAL_TYPE=charm

# 删除pod，但是不删除configmap，后面继续用 
(base) dominiczhu@ubuntu:configure-pod-configmap$ kubectl delete pod dapi-test-pod --now
pod "dapi-test-pod" deleted


# 在命令中使用ConfigMap中的变量
(base) dominiczhu@ubuntu:configure-pod-configmap$ kubectl create -f configmap/configmap-multikeys.yaml 
configmap/special-config created
(base) dominiczhu@ubuntu:configure-pod-configmap$ kubectl create -f pods/pod-configmap-env-var-valueFrom.yaml 
pod/dapi-test-pod created
(base) dominiczhu@ubuntu:configure-pod-configmap$ kubectl logs dapi-test-pod
very charm
(base) dominiczhu@ubuntu:configure-pod-configmap$ kubectl delete pod dapi-test-pod --now
pod "dapi-test-pod" deleted
(base) dominiczhu@ubuntu:configure-pod-configmap$ kubectl delete configmap/special-config
configmap "special-config" deleted
```

将 ConfigMap 数据添加到一个卷中，这个操作其实相当于使用ConfigMap的数据作为数据卷挂载到pod容器里

```shell
# 使用存储在 ConfigMap 中的数据填充卷
(base) dominiczhu@ubuntu:configure-pod-configmap$ kubectl create -f configmap/configmap-multikeys.yaml 
configmap/special-config created
(base) dominiczhu@ubuntu:configure-pod-configmap$ kubectl create -f pods/pod-configmap-volume.yaml 
pod/dapi-test-pod created
(base) dominiczhu@ubuntu:configure-pod-configmap$ kubectl logs  dapi-test-pod 
total 0
lrwxrwxrwx    1 root     root            20 May 23 08:59 SPECIAL_LEVEL -> ..data/SPECIAL_LEVEL
lrwxrwxrwx    1 root     root            19 May 23 08:59 SPECIAL_TYPE -> ..data/SPECIAL_TYPE


# 如果你把pod-configmap-volume.yaml的command改成command: [ "/bin/sh", "-c", "cat /etc/config/SPECIAL_LEVEL" ]
# 就可以发现输出的结果是very，即configmap的vallue

# 将 ConfigMap 数据添加到卷中的特定路径
(base) dominiczhu@ubuntu:configure-pod-configmap$ kubectl create -f pods/pod-configmap-volume-specific-key.yaml 
pod/dapi-test-pod created
(base) dominiczhu@ubuntu:configure-pod-configmap$ kubectl logs dapi-test-pod
very
(base) dominiczhu@ubuntu:configure-pod-configmap$ kubectl delete pod dapi-test-pod
pod "dapi-test-pod" deleted

```

Q：ConfigMap更新之后，挂载的volume会同步更新me？

A：会更新，但是不同步



了解 ConfigMap 和 Pod

```shell
# 这个例子里，说明了一个configmap可以有两种数据类型，数据和二进制数
(base) dominiczhu@ubuntu:configure-pod-configmap$ kubectl create -f configmap/example-config.yaml 
configmap/example-config created
(base) dominiczhu@ubuntu:configure-pod-configmap$ kubectl describe configmap example-config
Name:         example-config
Namespace:    default
Labels:       <none>
Annotations:  <none>

Data
====
example.property.2:
----
world

example.property.file:
----
property.1=value-1
property.2=value-2
property.3=value-3    

example.property.1:
----
hello


BinaryData
====
binary: 12 bytes

Events:  <none>
(base) dominiczhu@ubuntu:configure-pod-configmap$ kubectl get configmap -o jsonpath='{.data}' example-config
{"example.property.1":"hello","example.property.2":"world","example.property.file":"property.1=value-1\nproperty.2=value-2\nproperty.3=value-3    "}

(base) dominiczhu@ubuntu:configure-pod-configmap$ kubectl get configmap -o jsonpath='{.binaryData}' example-c
onfig
{"binary":"balalbalal232132"}
```



## 管理Kubernetes对象

### 使用 kubectl patch 更新 API 对象

注意事项：

- 不要在已经有nginx的pod里新增nginx容器，除非你调整其中一个nginx的端口，确保端口不要冲突。



# 教程

## 你好，Minikube

因为网络问题，所以`minikube start`会拉不来kicbase镜像，需要通过docker手动拉取，参考：[本文](https://blog.csdn.net/weixin_49244483/article/details/139616895)
```shell
docker pull registry.cn-hangzhou.aliyuncs.com/google_containers/kicbase:v0.0.46
minikube delete 
minikube start --force --base-image='registry.cn-hangzhou.aliyuncs.com/google_containers/kicbase:v0.0.46'

# --memory=1690mb 
#--force是以root身份启动的docker的必须选项
#--memory=1690mb 是因为资源不足需要添加的限制性参数，可忽略
#--base-image为指定minikube start 采用的基础镜像，上面docker pull拉取了什么镜像，这里就改成什么镜像

```

装好后的结果是这样的：

```shell
(base) dominiczhu@ubuntu:~/Coding/k8s$ docker pull registry.cn-hangzhou.aliyuncs.com/google_containers/kicbase:v0.0.46
v0.0.46: Pulling from google_containers/kicbase
066806f12a2a: Pull complete 
Digest: sha256:3700d8c8dc3749b808c26f402eb1fcab7595b1c4f0d3c2f42272a4e5f03f2717
Status: Downloaded newer image for registry.cn-hangzhou.aliyuncs.com/google_containers/kicbase:v0.0.46
registry.cn-hangzhou.aliyuncs.com/google_containers/kicbase:v0.0.46
(base) dominiczhu@ubuntu:~/Coding/k8s$ minikube delete 
🔥  Deleting "minikube" in docker ...
🔥  Removing /home/dominiczhu/.minikube/machines/minikube ...
💀  Removed all traces of the "minikube" cluster.
(base) dominiczhu@ubuntu:~/Coding/k8s$ minikube start --force --base-image='registry.cn-hangzhou.aliyuncs.com/google_containers/kicbase:v0.0.46'
😄  minikube v1.35.0 on Ubuntu 22.04
❗  minikube skips various validations when --force is supplied; this may lead to unexpected behavior
✨  Automatically selected the docker driver. Other choices: qemu2, none, ssh
📌  Using Docker driver with root privileges
👍  Starting "minikube" primary control-plane node in "minikube" cluster
🚜  Pulling base image v0.0.46 ...
🔥  Creating docker container (CPUs=2, Memory=3900MB) ...
🐳  Preparing Kubernetes v1.32.0 on Docker 27.4.1 ...
    ▪ Generating certificates and keys ...
    ▪ Booting up control plane ...
    ▪ Configuring RBAC rules ...
🔗  Configuring bridge CNI (Container Networking Interface) ...
🔎  Verifying Kubernetes components...
    ▪ Using image gcr.io/k8s-minikube/storage-provisioner:v5
🌟  Enabled addons: storage-provisioner, default-storageclass
🏄  Done! kubectl is now configured to use "minikube" cluster and "default" namespace by default

```



### dashboard



随后参照教程可以`minikube dashboard`，但是发现卡在`verifying proxy health`阶段，终端显示如下

```shell
minikube dashboard
🤔 正在验证 dashboard 运行情况
🚀 正在启动代理
🤔 正在验证 proxy 运行状况
```



于是参照网上文档：

[kubectl net/http: TLS handshake timeout](https://blog.csdn.net/u011987020/article/details/124815019)

[K8s - 解决Dashboard无法启动问题（Minikube搭建的集群）](https://www.hangge.com/blog/cache/detail_3096.html)

[10执行minikube dashboard报503错误的解决方案](https://blog.csdn.net/dongxiao_highgo/article/details/145919825)



查看所有pods情况`kubectl get pods --all-namespaces`

```shell

I0511 09:50:05.100728 2329027 helpers.go:237] Connection error: Get https://***.24.69.222:6443/version?timeout=32s: net/http: TLS handshake timeout
F0511 09:50:05.100742 2329027 helpers.go:118] Unable to connect to the server: net/http: TLS handshake timeout
```

发现TLS握手出现异常，执行`curl -v -k -H "Accept: application/json, */*" -H "User-Agent: kubectl/v1.23.6 (linux/amd64) kubernetes/ad33385" 'https://192.168.49.2:8443/version?timeout=32s'`之后发现使用了proxy，原因是我给虚拟机配置了代理，走代理访问这个端口会不通，关闭代理后重试发现还是不通，仍然在使用代理，这时需要关闭并重启一个terminal，在新的terminal里访问网络才会生效。重新执行curl发现成功了。

```shell
*   Trying 192.168.49.2:8443...
* Connected to 192.168.49.2 (192.168.49.2) port 8443 (#0)
* ALPN: offers h2
* ALPN: offers http/1.1
* [CONN-0-0][CF-SSL] TLSv1.3 (OUT), TLS handshake, Client hello (1):
* [CONN-0-0][CF-SSL] TLSv1.3 (IN), TLS handshake, Server hello (2):
* [CONN-0-0][CF-SSL] TLSv1.3 (IN), TLS handshake, Encrypted Extensions (8):
* [CONN-0-0][CF-SSL] TLSv1.3 (IN), TLS handshake, Request CERT (13):
* [CONN-0-0][CF-SSL] TLSv1.3 (IN), TLS handshake, Certificate (11):
* [CONN-0-0][CF-SSL] TLSv1.3 (IN), TLS handshake, CERT verify (15):
* [CONN-0-0][CF-SSL] TLSv1.3 (IN), TLS handshake, Finished (20):
* [CONN-0-0][CF-SSL] TLSv1.3 (OUT), TLS change cipher, Change cipher spec (1):
* [CONN-0-0][CF-SSL] TLSv1.3 (OUT), TLS handshake, Certificate (11):
* [CONN-0-0][CF-SSL] TLSv1.3 (OUT), TLS handshake, Finished (20):
* SSL connection using TLSv1.3 / TLS_AES_128_GCM_SHA256
* ALPN: server accepted h2
* Server certificate:
*  subject: O=system:masters; CN=minikube
*  start date: May 16 12:05:12 2025 GMT
*  expire date: May 16 12:05:12 2028 GMT
*  issuer: CN=minikubeCA
*  SSL certificate verify result: unable to get local issuer certificate (20), continuing anyway.
* Using HTTP2, server supports multiplexing
* Copying HTTP/2 data in stream buffer to connection buffer after upgrade: len=0
* h2h3 [:method: GET]
* h2h3 [:path: /version?timeout=32s]
* h2h3 [:scheme: https]
* h2h3 [:authority: 192.168.49.2:8443]
* h2h3 [accept: application/json, */*]
* h2h3 [user-agent: kubectl/v1.23.6 (linux/amd64) kubernetes/ad33385]
* Using Stream ID: 1 (easy handle 0xc9a860)
> GET /version?timeout=32s HTTP/2
> Host: 192.168.49.2:8443
> accept: application/json, */*
> user-agent: kubectl/v1.23.6 (linux/amd64) kubernetes/ad33385
> 
* [CONN-0-0][CF-SSL] TLSv1.3 (IN), TLS handshake, Newsession Ticket (4):
< HTTP/2 200 
< audit-id: 77244fe6-303f-4e3e-9e1d-78479840fe2f
< cache-control: no-cache, private
< content-type: application/json
< x-kubernetes-pf-flowschema-uid: 1b116a7d-55d0-4f5b-a5f8-3b83d178fb1f
< x-kubernetes-pf-prioritylevel-uid: 53145154-3f03-4972-ae32-d250c2155bbd
< content-length: 263
< date: Sun, 18 May 2025 07:28:16 GMT
< 
{
  "major": "1",
  "minor": "32",
  "gitVersion": "v1.32.0",
  "gitCommit": "70d3cc986aa8221cd1dfb1121852688902d3bf53",
  "gitTreeState": "clean",
  "buildDate": "2024-12-11T17:59:15Z",
  "goVersion": "go1.23.3",
  "compiler": "gc",
  "platform": "linux/amd64"
* Closing connection 0
* [CONN-0-0][CF-SSL] TLSv1.3 (OUT), TLS alert, close notify (256):
```



这时候执行`kubectl get pods --all-namespaces`发现有两个镜像捞不下来

```shell
NAMESPACE              NAME                                         READY   STATUS             RESTARTS      AGE
kube-system            coredns-668d6bf9bc-8kvp2                     1/1     Running            4 (65s ago)   13h
kube-system            etcd-minikube                                1/1     Running            4 (70s ago)   13h
kube-system            kube-apiserver-minikube                      1/1     Running            4 (60s ago)   13h
kube-system            kube-controller-manager-minikube             1/1     Running            4 (70s ago)   13h
kube-system            kube-proxy-4pdnz                             1/1     Running            4 (70s ago)   13h
kube-system            kube-scheduler-minikube                      1/1     Running            4 (70s ago)   13h
kube-system            storage-provisioner                          1/1     Running            8 (14s ago)   13h
kubernetes-dashboard   dashboard-metrics-scraper-5d59dccf9b-plx77   0/1     ErrImagePull       0             13h
kubernetes-dashboard   kubernetes-dashboard-7779f9b69b-qmfn5        0/1     ImagePullBackOff   0             13h

```





查看这两个容器`kubectl describe pod dashboard-metrics-scraper-5d59dccf9b-plx77 -n kubernetes-dashboard`发现镜像拉取失败

```shell
  Normal   Pulling         62s (x3 over 2m29s)     kubelet  Pulling image "docker.io/kubernetesui/metrics-scraper:v1.0.8@sha256:76049887f07a0476dc93efc2d3569b9529bf982b22d29f356092ce206e98765c"
  Warning  Failed          47s (x3 over 119s)      kubelet  Error: ErrImagePull
  Warning  Failed          47s                     kubelet  Failed to pull image "docker.io/kubernetesui/metrics-scraper:v1.0.8@sha256:76049887f07a0476dc93efc2d3569b9529bf982b22d29f356092ce206e98765c": Error response from daemon: Get "https://registry-1.docker.io/v2/": dial tcp 69.63.190.26:443: i/o timeout
  Normal   BackOff         8s (x5 over 119s)       kubelet  Back-off pulling image "docker.io/kubernetesui/metrics-scraper:v1.0.8@sha256:76049887f07a0476dc93efc2d3569b9529bf982b22d29f356092ce206e98765c"
  Warning  Failed          8s (x5 over 119s)       kubelet  Error: ImagePullBackOff

```

`kubectl describe pod kubernetes-dashboard-7779f9b69b-qmfn5 -n kubernetes-dashboard`

```shell
  Normal   Pulling         35s (x5 over 4m30s)   kubelet  Pulling image "docker.io/kubernetesui/dashboard:v2.7.0@sha256:2e500d29e9d5f4a086b908eb8dfe7ecac57d2ab09d65b24f588b1d449841ef93"
  Warning  Failed          20s (x5 over 4m15s)   kubelet  Error: ErrImagePull
  Warning  Failed          20s (x4 over 3m44s)   kubelet  Failed to pull image "docker.io/kubernetesui/dashboard:v2.7.0@sha256:2e500d29e9d5f4a086b908eb8dfe7ecac57d2ab09d65b24f588b1d449841ef93": Error response from daemon: Get "https://registry-1.docker.io/v2/": net/http: request canceled while waiting for connection (Client.Timeout exceeded while awaiting headers)
  Normal   BackOff         4s (x11 over 4m14s)   kubelet  Back-off pulling image "docker.io/kubernetesui/dashboard:v2.7.0@sha256:2e500d29e9d5f4a086b908eb8dfe7ecac57d2ab09d65b24f588b1d449841ef93"
  Warning  Failed          4s (x11 over 4m14s)   kubelet  Error: ImagePullBackOff

```

解决方案同后文启用插件的解决


#### 修改镜像路径

随后可以有两种方式，如上文博客所示，一个方法是修改容器的路径






修改`kubectl -n kubernetes-dashboard edit deployment kubernetes-dashboard`

```shell
[highgo@localhost ~]$ kubectl -n kubernetes-dashboard edit deployment dashboard-metrics-scraper
    spec:
      containers:
      - image: registry.cn-hangzhou.aliyuncs.com/google_containers/metrics-scraper:v1.0.8
        imagePullPolicy: IfNotPresent

```

修改`kubectl -n kubernetes-dashboard edit deployment kubernetes-dashboard`

```shell
[highgo@localhost ~]$ kubectl -n kubernetes-dashboard edit deployment kubernetes-dashboard
    spec:
      containers:
      - args:
        - --namespace=kubernetes-dashboard
        - --enable-skip-login
        - --disable-settings-authorizer
        image: registry.cn-hangzhou.aliyuncs.com/google_containers/dashboard:v2.7.0
        imagePullPolicy: IfNotPresent

```

过一会再次查看pod状态为Running



### 创建 Deployment

执行下面命令的时候，发现仍然无法启动

注意，minikube并不能直接使用docker的镜像，需要执行minikube load导入后，minikube才能用

```shell
docker pull crpi-vgj0j6781pn5263n.cn-hangzhou.personal.cr.aliyuncs.com/goose-good/agnhost:2.53
docker tag  crpi-vgj0j6781pn5263n.cn-hangzhou.personal.cr.aliyuncs.com/goose-good/agnhost:2.53 registry.k8s.io/e2e-test-images/agnhost:2.53

minikube image load registry.k8s.io/e2e-test-images/agnhost:2.53

kubectl create deployment hello-node --image=registry.k8s.io/e2e-test-images/agnhost:2.53 -- /agnhost netexec --http-port=8080

kubectl get events

# 可以通过这个配置看到一个certificate-authority: /home/dominiczhu/.minikube/ca.crt
kubectl config view 


# 记得考虑本机proxy的影响
minikube service hello-node
```


```shell
# 运行包含 Web 服务器的测试容器镜像
kubectl create deployment hello-node --image=registry.k8s.io/e2e-test-images/agnhost:2.39 -- /agnhost netexec --http-port=8080
```



```shell
(base) dominiczhu@ubuntu:~/Desktop$ kubectl get pods
NAME                         READY   STATUS             RESTARTS   AGE
hello-node-c74958b5d-sjlv6   0/1     ImagePullBackOff   0          3m6s

```



执行`kubectl get events`发现与上面问题相同，

```shell
91s         Warning   Failed              pod/hello-node-c74958b5d-sjlv6    Failed to pull image "registry.k8s.io/e2e-test-images/agnhost:2.39": Error response from daemon: Head "https://us-west2-docker.pkg.dev/v2/k8s-artifacts-prod/images/e2e-test-images/agnhost/manifests/2.39": dial tcp 142.250.107.82:443: connect: connection refused

```



解决方案参考了[此文](Kubernetes的minikube)，以及这个[工程](https://github.com/anjia0532/gcr.io_mirror)，这是个Google Container Registry镜像加速，简单说就是他会将 `gcr.io` , `k8s.gcr.io` , `registry.k8s.io` , `quay.io`, `ghcr.io`这几个地址的镜像拉下来，然后传到作者自己的镜像仓库中，然后我们可以通过他的仓库下载一些拉取的镜像，只要通过提issue即可，在[已搬运镜像集锦](https://github.com/anjia0532/gcr.io_mirror/issues?q=is%3Aissue+label%3Aporter+)中可以找到`agnhost:2.39`

```shell

#原镜像
registry.k8s.io/e2e-test-images/agnhost:2.39

#转换后镜像
anjia0532/google-containers.e2e-test-images.agnhost:2.39


#下载并重命名镜像
docker pull anjia0532/google-containers.e2e-test-images.agnhost:2.39 

docker tag  anjia0532/google-containers.e2e-test-images.agnhost:2.39 registry.k8s.io/e2e-test-images/agnhost:2.39

docker images | grep $(echo registry.k8s.io/e2e-test-images/agnhost:2.39 |awk -F':' '{print $1}')
```



于是我们可以重来

```shell
# 删除 Deployment重来
kubectl delete deployment hello-node

#下载并重命名镜像 这时候可以主机挂个梯子然后虚拟机开个代理
docker pull anjia0532/google-containers.e2e-test-images.agnhost:2.39 

docker tag  anjia0532/google-containers.e2e-test-images.agnhost:2.39 registry.k8s.io/e2e-test-images/agnhost:2.39

# 下载完成后关闭代理 重新创建deployment
kubectl create deployment hello-node --image=registry.k8s.io/e2e-test-images/agnhost:2.39 -- /agnhost netexec --http-port=8080

# 查看执行结果
(base) dominiczhu@ubuntu:~/Desktop$ kubectl get deployments
NAME         READY   UP-TO-DATE   AVAILABLE   AGE
hello-node   1/1     1            1           57s
(base) dominiczhu@ubuntu:~/Desktop$ kubectl get events
LAST SEEN   TYPE      REASON              OBJECT                            MESSAGE
62s         Normal    Scheduled           pod/hello-node-c74958b5d-bmqqg    Successfully assigned default/hello-node-c74958b5d-bmqqg to minikube
62s         Normal    Pulling             pod/hello-node-c74958b5d-bmqqg    Pulling image "registry.k8s.io/e2e-test-images/agnhost:2.39"
40s         Normal    Pulled              pod/hello-node-c74958b5d-bmqqg    Successfully pulled image "registry.k8s.io/e2e-test-images/agnhost:2.39" in 21.657s (21.657s including waiting). Image size: 126872991 bytes.
40s         Normal    Created             pod/hello-node-c74958b5d-bmqqg    Created container: agnhost
40s         Normal    Started             pod/hello-node-c74958b5d-bmqqg    Started container agnhost
34m         Normal    Scheduled           pod/hello-node-c74958b5d-sjlv6    Successfully assigned default/hello-node-c74958b5d-sjlv6 to minikube
29m         Normal    Pulling             pod/hello-node-c74958b5d-sjlv6    Pulling image "registry.k8s.io/e2e-test-images/agnhost:2.39"
29m         Warning   Failed              pod/hello-node-c74958b5d-sjlv6    Failed to pull image "registry.k8s.io/e2e-test-images/agnhost:2.39": Error response from daemon: Head "https://us-west2-docker.pkg.dev/v2/k8s-artifacts-prod/images/e2e-test-images/agnhost/manifests/2.39": dial tcp 142.250.107.82:443: connect: connection refused
29m         Warning   Failed              pod/hello-node-c74958b5d-sjlv6    Error: ErrImagePull
24m         Normal    BackOff             pod/hello-node-c74958b5d-sjlv6    Back-off pulling image "registry.k8s.io/e2e-test-images/agnhost:2.39"
24m         Warning   Failed              pod/hello-node-c74958b5d-sjlv6    Error: ImagePullBackOff
34m         Normal    SuccessfulCreate    replicaset/hello-node-c74958b5d   Created pod: hello-node-c74958b5d-sjlv6
62s         Normal    SuccessfulCreate    replicaset/hello-node-c74958b5d   Created pod: hello-node-c74958b5d-bmqqg

(base) dominiczhu@ubuntu:~/Desktop$ kubectl get pods
NAME                         READY   STATUS    RESTARTS   AGE
hello-node-c74958b5d-bmqqg   1/1     Running   0          94s


(base) dominiczhu@ubuntu:~/Desktop$ kubectl logs hello-node-c74958b5d-bmqqg
I0518 08:16:21.634472       1 log.go:195] Started HTTP server on port 8080
I0518 08:16:21.634767       1 log.go:195] Started UDP server on port  8081

```



#### 启用插件

启用`metrics-server`后`minikube addons enable metrics-server`

```shell
(base) dominiczhu@ubuntu:~/Desktop$ kubectl get pod,svc -n kube-system
NAME                                   READY   STATUS             RESTARTS         AGE
pod/coredns-668d6bf9bc-8kvp2           1/1     Running            6 (7h25m ago)    20h
pod/etcd-minikube                      1/1     Running            6 (7h25m ago)    20h
pod/kube-apiserver-minikube            1/1     Running            6 (7h25m ago)    20h
pod/kube-controller-manager-minikube   1/1     Running            6 (7h25m ago)    20h
pod/kube-proxy-4pdnz                   1/1     Running            6 (7h25m ago)    20h
pod/kube-scheduler-minikube            1/1     Running            6 (7h25m ago)    20h
pod/metrics-server-7fbb699795-2zsnh    0/1     ImagePullBackOff   0                9m10s
pod/storage-provisioner                1/1     Running            12 (7h24m ago)   20h

NAME                     TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)                  AGE
service/kube-dns         ClusterIP   10.96.0.10       <none>        53/UDP,53/TCP,9153/TCP   20h
service/metrics-server   ClusterIP   10.106.201.115   <none>        443/TCP                  9m10s
```

还是上面的问题，镜像拉不下来

```shell
(base) dominiczhu@ubuntu:~/Desktop$ kubectl describe pod metrics-server-7fbb699795-2zsnh -n kube-system
Events:
  Type     Reason     Age                    From               Message
  ----     ------     ----                   ----               -------
  Normal   Scheduled  5m22s                  default-scheduler  Successfully assigned kube-system/metrics-server-7fbb699795-2zsnh to minikube
  Warning  Failed     2m18s (x3 over 4m20s)  kubelet            Failed to pull image "registry.k8s.io/metrics-server/metrics-server:v0.7.2@sha256:ffcb2bf004d6aa0a17d90e0247cf94f2865c8901dcab4427034c341951c239f9": Error response from daemon: Get "https://us-west2-docker.pkg.dev/v2/k8s-artifacts-prod/images/metrics-server/metrics-server/manifests/sha256:ffcb2bf004d6aa0a17d90e0247cf94f2865c8901dcab4427034c341951c239f9": dial tcp 74.125.195.82:443: connect: connection refused
  Normal   Pulling    57s (x5 over 5m22s)    kubelet            Pulling image "registry.k8s.io/metrics-server/metrics-server:v0.7.2@sha256:ffcb2bf004d6aa0a17d90e0247cf94f2865c8901dcab4427034c341951c239f9"
  Warning  Failed     32s (x2 over 4m58s)    kubelet            Failed to pull image "registry.k8s.io/metrics-server/metrics-server:v0.7.2@sha256:ffcb2bf004d6aa0a17d90e0247cf94f2865c8901dcab4427034c341951c239f9": Error response from daemon: Get "https://us-west2-docker.pkg.dev/v2/k8s-artifacts-prod/images/metrics-server/metrics-server/manifests/sha256:ffcb2bf004d6aa0a17d90e0247cf94f2865c8901dcab4427034c341951c239f9": dial tcp 142.250.107.82:443: connect: connection refused
  Warning  Failed     32s (x5 over 4m58s)    kubelet            Error: ErrImagePull
  Normal   BackOff    4s (x12 over 4m57s)    kubelet            Back-off pulling image "registry.k8s.io/metrics-server/metrics-server:v0.7.2@sha256:ffcb2bf004d6aa0a17d90e0247cf94f2865c8901dcab4427034c341951c239f9"
  Warning  Failed     4s (x12 over 4m57s)    kubelet            Error: ImagePullBackOff


```

求助anjia大神构建了一个[issue](https://github.com/anjia0532/gcr.io_mirror/issues/4354)

```shell
#重来
minikube addons disable metrics-server



#原镜像
registry.k8s.io/metrics-server/metrics-server:v0.7.2

#转换后镜像
anjia0532/google-containers.metrics-server.metrics-server:v0.7.2


#下载并重命名镜像
docker pull anjia0532/google-containers.metrics-server.metrics-server:v0.7.2 

docker tag  anjia0532/google-containers.metrics-server.metrics-server:v0.7.2 registry.k8s.io/metrics-server/metrics-server:v0.7.2

docker images | grep $(echo registry.k8s.io/metrics-server/metrics-server:v0.7.2 |awk -F':' '{print $1}')

```



然后重来

```shell
kubectl get pod,svc -n kube-system

(base) dominiczhu@ubuntu:~/Desktop$ kubectl get pod,svc -n kube-system
NAME                                   READY   STATUS    RESTARTS         AGE
pod/coredns-668d6bf9bc-8kvp2           1/1     Running   6 (7h37m ago)    21h
pod/etcd-minikube                      1/1     Running   6 (7h37m ago)    21h
pod/kube-apiserver-minikube            1/1     Running   6 (7h37m ago)    21h
pod/kube-controller-manager-minikube   1/1     Running   6 (7h37m ago)    21h
pod/kube-proxy-4pdnz                   1/1     Running   6 (7h37m ago)    21h
pod/kube-scheduler-minikube            1/1     Running   6 (7h37m ago)    21h
pod/metrics-server-7fbb699795-k6j6m    1/1     Running   0                86s
pod/storage-provisioner                1/1     Running   12 (7h36m ago)   21h

NAME                     TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)                  AGE
service/kube-dns         ClusterIP   10.96.0.10       <none>        53/UDP,53/TCP,9153/TCP   21h
service/metrics-server   ClusterIP   10.102.249.140   <none>        443/TCP                  86s


```

另一个方法是：
使用metrics-server插件的话，需要使用`registry.k8s.io/metrics-server/metrics-server:v0.7.2`这个镜像，但即使我按照上述方式导入了镜像，也仍然会报错
> "Error syncing pod, skipping" err="failed to \"StartContainer\" for \"metrics-server\" with ImagePullBackOff: \"Back-off pulling image \\\"registry.k8s.io/metrics-server/metrics-server:v0.7.2@sha256:ffcb2bf004d6aa0a17d90e0247cf94f2865c8901dcab4427034c341951c239f9\\\": ErrImagePull: Error response from daemon: Get \\\"https://europe-west3-docker.pkg.dev/v2/k8s-artifacts-prod/images/metrics-server/metrics-server/manifests/sha256:ffcb2bf004d6aa0a17d90e0247cf94f2865c8901dcab4427034c341951c239f9\\\": dial tcp 74.125.20.82:443: connect: connection refused\"" pod="kube-system/metrics-server-7fbb699795-6fqvx" podUID="9c927488-9a0e-440a-b760-e9e4986ad10d"

这是因为我通过拉到本地的镜像的sha256签名并不是`ffcb2bf004d6aa0a17d90e0247cf94f2865c8901dcab4427034c341951c239f9`，因此不被认可，`kubectl edit deployment metrics-server -n kube-system`修改这个插件的指定镜像即可，参考：https://www.qianwen.com/share/chat/2c4c059c519e41058b21edd76276ac75



### 小结

概念：

- pod：一些容器组成的组；
- service：“将在集群中运行的应用通过同一个面向外界的端点公开出去，即使工作负载分散于多个后端也完全可行。”。例如，默认情况下，Pod 只能通过 Kubernetes 集群中的内部 IP 地址访问。 要使得 `hello-node` 容器可以从 Kubernetes 虚拟网络的外部访问，你必须将 Pod 通过 Kubernetes [**Service**](https://kubernetes.io/zh-cn/docs/concepts/services-networking/service/) 公开出来。Kubernetes 中 Service 是 将运行在一个或一组 [Pod](https://kubernetes.io/zh-cn/docs/concepts/workloads/pods/) 上的网络应用程序公开为网络服务的方法。
- Deployment ：用于管理运行一个应用负载的一组 Pod，通常适用于不保持状态的负载。个人理解，这东西既然叫做“部署”，那应该就是一个抽象的概念，类似于部署不同pod过程中进行资源管理的工具。



## 学习Kubernetes基础知识

### 使用 Minikube 创建集群



关于k8s到底是干啥的，文档中的介绍是

> **Kubernetes 协调一个高可用计算机集群，每个计算机互相连接之后作为同一个工作单元运行。** Kubernetes 中的抽象允许你将容器化的应用部署到集群，而无需将它们绑定到某个特定的独立计算机。 为了使用这种新的部署模型，需要以将应用与单个主机解耦的方式打包：它们需要被容器化。 与过去的那种应用直接以包的方式深度与主机集成的部署模型相比，容器化应用更灵活、更可用。 **Kubernetes 以更高效的方式跨集群自动分布和调度应用容器。** Kubernetes 是一个开源平台，并且可应用于生产环境。

非常晦涩，问豆包“Kubernetes解决了什么问题？”给的答案很好理解了，说白了，就是原本都是要准备好多台物理服务器或者虚拟机，每个物理服务器或者虚拟机运行一个或者多个应用，在微服务情况下，要部署一堆应用，如果部署在同一台机器下，应用之间会抢资源；如果部署在不同的机器下，管理多个机器挨个部署管理网络配置dns非常麻烦。而k8s相当于作为一个中间层屏蔽了底层的“物理服务器或者虚拟机”，面向应用提供统一的计算存储资源、网络管理、高可用等功能。

一个K8s集群由多个角色组成：
- Node：物理机或者虚拟机，**节点是一个虚拟机或者物理机，它在 Kubernetes 集群中充当工作机器的角色。** 
  - Kubelet：它管理节点而且是节点与控制面通信的代理。
  - docker：提供容器化服务的仍然是docker
- Control Plane：**控制面负责管理整个集群。** 控制面协调集群中的所有活动，例如调度应用、维护应用的期望状态、对应用扩容以及将新的更新上线等等。


### 部署应用

这里引入了“deployment”这个概念，当k8s集群运行起来之后，就可以向其中*部署*容器化应用了，这就是一个“部署”，会创建一个deployment，deployment控制器会监视管理这些容器化的应用，例如抓一个不错的节点来运行这些容器。

这里可以看出k8s和docker的一些区别，当使用docker时，我们是在某一台机器上启用了一个容器；而当使用k8s的时候，k8s集群本身包含了多个机器，就像我们在多台机器上部署容器一样，只不过k8s屏蔽了不同机器之间的差别。简单说：

- 一台机器-通过docker运行一个docker容器
- 多台机器-运行k8s运行一堆docker容器

先参照[kubernetes-bootcamp镜像](https://github.com/anjia0532/gcr.io_mirror/issues/3911)把依赖的镜像捞下来

```shell
#下载并重命名镜像
docker pull anjia0532/google-samples.kubernetes-bootcamp:v1 

docker tag  anjia0532/google-samples.kubernetes-bootcamp:v1 gcr.io/google-samples/kubernetes-bootcamp:v1
```



开始

```shell
(base) dominiczhu@ubuntu:~/Desktop$ minikube start
😄  minikube v1.35.0 on Ubuntu 22.04
✨  Using the docker driver based on existing profile
👍  Starting "minikube" primary control-plane node in "minikube" cluster
🚜  Pulling base image v0.0.46 ...
🔄  Restarting existing docker container for "minikube" ...
🐳  Preparing Kubernetes v1.32.0 on Docker 27.4.1 ...
🔎  Verifying Kubernetes components...
    ▪ Using image docker.io/kubernetesui/metrics-scraper:v1.0.8
    ▪ Using image docker.io/kubernetesui/dashboard:v2.7.0
    ▪ Using image gcr.io/k8s-minikube/storage-provisioner:v5
💡  Some dashboard features require the metrics-server addon. To enable all features please run:

	minikube addons enable metrics-server

🌟  Enabled addons: default-storageclass, storage-provisioner, dashboard
🏄  Done! kubectl is now configured to use "minikube" cluster and "default" namespace by default

(base) dominiczhu@ubuntu:~/Desktop$ kubectl version
Client Version: v1.33.1
Kustomize Version: v5.6.0
Server Version: v1.32.0

# 说明这里启动了一个节点，叫做minikube
(base) dominiczhu@ubuntu:~/Desktop$ kubectl get nodes
NAME       STATUS   ROLES           AGE    VERSION
minikube   Ready    control-plane   3d1h   v1.32.0

# 创建了一个叫做kubernetes-bootcamp的deployment
(base) dominiczhu@ubuntu:~/Desktop$ kubectl create deployment kubernetes-bootcamp --image=gcr.io/google-samples/kubernetes-bootcamp:v1
deployment.apps/kubernetes-bootcamp created

# 输出字段的含义可以问豆包
(base) dominiczhu@ubuntu:~/Desktop$  kubectl get deployments
NAME                  READY   UP-TO-DATE   AVAILABLE   AGE
kubernetes-bootcamp   1/1     1            1           2m19s

```

打开第二个窗口

```shell
(base) dominiczhu@ubuntu:~/Desktop$ kubectl proxy
Starting to serve on 127.0.0.1:8001

```

回到第一个窗口

```shell
# 现在可以通过http接口来访问集群了
(base) dominiczhu@ubuntu:~/Desktop$ curl http://localhost:8001/version
{
  "major": "1",
  "minor": "32",
  "gitVersion": "v1.32.0",
  "gitCommit": "70d3cc986aa8221cd1dfb1121852688902d3bf53",
  "gitTreeState": "clean",
  "buildDate": "2024-12-11T17:59:15Z",
  "goVersion": "go1.23.3",
  "compiler": "gc",
  "platform": "linux/amd64"
}


# 这个命令就是获取pods的name
(base) dominiczhu@ubuntu:~/Desktop$ kubectl get pods -o go-template --template '{{range .items}}{{.metadata.name}}{{"\n"}}{{end}}'
kubernetes-bootcamp-9bc58d867-x9x9v

(base) dominiczhu@ubuntu:~/Desktop$ curl http://localhost:8001/api/v1/namespaces/default/pods/kubernetes-bootcamp-9bc58d867-x9x9v:8080/proxy/
Hello Kubernetes bootcamp! | Running on: kubernetes-bootcamp-9bc58d867-x9x9v | v=1

```



### 了解你的应用



#### pod

pod的英文是豌豆荚，在k8s之中，他代表着一个最小运行单元，可以运行多个容器。

#### 节点

> 容器运行时（如 Docker）负责从镜像仓库中拉取容器镜像、解压缩容器以及运行应用。

container runtime翻译成"容器运行时"，不如翻译成容器运行环境


### 公开地暴露你的应用

Pod需要对外提供应用，但外部应用并不需要关注pod是啥，于是就抽象出了一个中间层，外部通过service来访问pod



```shell

(base) dominiczhu@ubuntu:~/Desktop$ kubectl get pods
NAME                                  READY   STATUS    RESTARTS   AGE
kubernetes-bootcamp-9bc58d867-x9x9v   1/1     Running   0          58m
(base) dominiczhu@ubuntu:~/Desktop$ kubectl get services
NAME         TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)   AGE
kubernetes   ClusterIP   10.96.0.1    <none>        443/TCP   3d2h
(base) dominiczhu@ubuntu:~/Desktop$ kubectl get deployments
NAME                  READY   UP-TO-DATE   AVAILABLE   AGE
kubernetes-bootcamp   1/1     1            1           58m

# 将一个部署通过service对外暴露出去，相当于将deployment/kubernetes-bootcamp这个部署里的pod的容器的8080端口映射到minikube集群的某个端口，这里就是30822
(base) dominiczhu@ubuntu:~/Desktop$ kubectl expose deployment/kubernetes-bootcamp --type="NodePort" --port 8080
service/kubernetes-bootcamp exposed
(base) dominiczhu@ubuntu:~/Desktop$ kubectl get services
NAME                  TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
kubernetes            ClusterIP   10.96.0.1       <none>        443/TCP          3d2h
kubernetes-bootcamp   NodePort    10.103.27.125   <none>        8080:30822/TCP   42s
(base) dominiczhu@ubuntu:~/Desktop$ kubectl describe services/kubernetes-bootcamp

# 查看这个service的端口
(base) dominiczhu@ubuntu:~/Desktop$ kubectl get services/kubernetes-bootcamp -o go-template='{{(index .spec.ports 0).nodePort}}'
30822
# 查看集群（即minikube）的ip
(base) dominiczhu@ubuntu:~/Desktop$ minikube ip
192.168.49.2
(base) dominiczhu@ubuntu:~/Desktop$ curl http://192.168.49.2:30822
Hello Kubernetes bootcamp! | Running on: kubernetes-bootcamp-9bc58d867-x9x9v | v=1
```



使用标签，标签相当于给deployment、pod等等打的一个记号，可以通过标签来查询、标识

```shell
# 输出结果可以看到label
(base) dominiczhu@ubuntu:~/Desktop$ kubectl describe deployment/kubernetes-bootcamp
Name:                   kubernetes-bootcamp
Namespace:              default
CreationTimestamp:      Tue, 20 May 2025 21:35:24 +0800
Labels:                 app=kubernetes-bootcamp

# 可以基于标签进行查询
(base) dominiczhu@ubuntu:~/Desktop$ kubectl get deployments -l app=kubernetes-bootcamp
NAME                  READY   UP-TO-DATE   AVAILABLE   AGE
kubernetes-bootcamp   1/1     1            1           68m

# 对于service、pod也是同理
(base) dominiczhu@ubuntu:~/Desktop$ kubectl describe pod/kubernetes-bootcamp-9bc58d867-x9x9v


(base) dominiczhu@ubuntu:~/Desktop$ kubectl get pods -l app=kubernetes-bootcamp
NAME                                  READY   STATUS    RESTARTS   AGE
kubernetes-bootcamp-9bc58d867-x9x9v   1/1     Running   0          69m

# 手动打标签
(base) dominiczhu@ubuntu:~/Desktop$ kubectl label pods kubernetes-bootcamp-9bc58d867-x9x9v version=v1

(base) dominiczhu@ubuntu:~/Desktop$ kubectl describe pod/kubernetes-bootcamp-9bc58d867-x9x9v


```



删除service

```shell
(base) dominiczhu@ubuntu:~/Desktop$ kubectl delete service -l app=kubernetes-bootcamp
service "kubernetes-bootcamp" deleted
```

四种公开service的类型：https://www.qianwen.com/share/chat/4744d61337be4882a4925452b63ed61f。其中ExternalName的service只是说这个内的dns服务了注册一个指向外部域名的cname，这样内部节点可以通过访问这个service访问外部节点了。

> Endpoints 对象：https://www.qianwen.com/share/chat/adeb67e40dd247fe96a0eab72b787606


### 扩缩你的应用

目前有一个deployment在运行着

```shell
(base) dominiczhu@ubuntu:~$ kubectl get deployment
NAME                  READY   UP-TO-DATE   AVAILABLE   AGE
kubernetes-bootcamp   1/1     1            1           47h
(base) dominiczhu@ubuntu:~$ kubectl get pod
NAME                                  READY   STATUS    RESTARTS     AGE
kubernetes-bootcamp-9bc58d867-x9x9v   1/1     Running   1 (8h ago)   47h

# 暴露一个服务
(base) dominiczhu@ubuntu:~$ kubectl expose deployment/kubernetes-bootcamp --type="LoadBalancer" --port 8080
service/kubernetes-bootcamp exposed
(base) dominiczhu@ubuntu:~$ kubectl get service
NAME                  TYPE           CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
kubernetes            ClusterIP      10.96.0.1       <none>        443/TCP          5d1h
kubernetes-bootcamp   LoadBalancer   10.98.139.129   <pending>     8080:30467/TCP   7s
```



> Service 有一个集成的负载均衡器， 将网络流量分配到一个可公开访问的 Deployment 的所有 Pod 上。 Service 将会通过 Endpoints 来持续监视运行中的 Pod 集合，保证流量只分配到可用的 Pod 上。

Q：那这岂不是可以取代服务发现功能

A：简单场景还真可以。豆包说的。



开始扩容

```shell
(base) dominiczhu@ubuntu:~$ kubectl scale deployments/kubernetes-bootcamp --replicas=4
deployment.apps/kubernetes-bootcamp scaled
(base) dominiczhu@ubuntu:~$ kubectl get deployments
NAME                  READY   UP-TO-DATE   AVAILABLE   AGE
kubernetes-bootcamp   4/4     4            4           2d
(base) dominiczhu@ubuntu:~$ kubectl get pods -o wide
NAME                                  READY   STATUS    RESTARTS     AGE   IP            NODE       NOMINATED NODE   READINESS GATES
kubernetes-bootcamp-9bc58d867-5p7d2   1/1     Running   0            15s   10.244.0.53   minikube   <none>           <none>
kubernetes-bootcamp-9bc58d867-jjrb2   1/1     Running   0            15s   10.244.0.52   minikube   <none>           <none>
kubernetes-bootcamp-9bc58d867-jvmbl   1/1     Running   0            15s   10.244.0.54   minikube   <none>           <none>
kubernetes-bootcamp-9bc58d867-x9x9v   1/1     Running   1 (8h ago)   2d    10.244.0.46   minikube   <none>           <none>

(base) dominiczhu@ubuntu:~$ kubectl get rs
NAME                            DESIRED   CURRENT   READY   AGE
kubernetes-bootcamp-9bc58d867   4         4         4       2d

(base) dominiczhu@ubuntu:~$ kubectl describe services/kubernetes-bootcamp
Name:                     kubernetes-bootcamp
Namespace:                default
Labels:                   app=kubernetes-bootcamp
Annotations:              <none>
Selector:                 app=kubernetes-bootcamp
Type:                     LoadBalancer
IP Family Policy:         SingleStack
IP Families:              IPv4
IP:                       10.98.139.129
IPs:                      10.98.139.129
Port:                     <unset>  8080/TCP
TargetPort:               8080/TCP
NodePort:                 <unset>  30467/TCP
Endpoints:                10.244.0.46:8080,10.244.0.54:8080,10.244.0.52:8080 + 1 more...
Session Affinity:         None
External Traffic Policy:  Cluster
Internal Traffic Policy:  Cluster
Events:                   <none>

```

随后可以访问这个service了



```shell
# 首先查看这个service映射到主机的端口
(base) dominiczhu@ubuntu:~$ kubectl get services/kubernetes-bootcamp -o go-template='{{(index .spec.ports 0).nodePort}}'
30467

# 查看集群的ip
(base) dominiczhu@ubuntu:~$ minikube ip
192.168.49.2

# 可以看到负载均衡了
(base) dominiczhu@ubuntu:~$ curl http://"$(minikube ip):30467"
Hello Kubernetes bootcamp! | Running on: kubernetes-bootcamp-9bc58d867-jjrb2 | v=1
(base) dominiczhu@ubuntu:~$ curl http://"$(minikube ip):30467"
Hello Kubernetes bootcamp! | Running on: kubernetes-bootcamp-9bc58d867-5p7d2 | v=1
(base) dominiczhu@ubuntu:~$ curl http://"$(minikube ip):30467"
Hello Kubernetes bootcamp! | Running on: kubernetes-bootcamp-9bc58d867-x9x9v | v=1
(base) dominiczhu@ubuntu:~$ curl http://"$(minikube ip):30467"
Hello Kubernetes bootcamp! | Running on: kubernetes-bootcamp-9bc58d867-jvmbl | v=1
(base) dominiczhu@ubuntu:~$ curl http://"$(minikube ip):30467"
Hello Kubernetes bootcamp! | Running on: kubernetes-bootcamp-9bc58d867-5p7d2 | v=1

```



接下来开始缩容

```shell
(base) dominiczhu@ubuntu:~$ kubectl scale deployments/kubernetes-bootcamp --replicas=2
deployment.apps/kubernetes-bootcamp scaled
(base) dominiczhu@ubuntu:~$ kubectl get deployments
NAME                  READY   UP-TO-DATE   AVAILABLE   AGE
kubernetes-bootcamp   2/2     2            2           2d
(base) dominiczhu@ubuntu:~$ kubectl get pods -o wide
NAME                                  READY   STATUS        RESTARTS     AGE     IP            NODE       NOMINATED NODE   READINESS GATES
kubernetes-bootcamp-9bc58d867-5p7d2   1/1     Terminating   0            6m52s   10.244.0.53   minikube   <none>           <none>
kubernetes-bootcamp-9bc58d867-jjrb2   1/1     Running       0            6m52s   10.244.0.52   minikube   <none>           <none>
kubernetes-bootcamp-9bc58d867-jvmbl   1/1     Terminating   0            6m52s   10.244.0.54   minikube   <none>           <none>
kubernetes-bootcamp-9bc58d867-x9x9v   1/1     Running       1 (9h ago)   2d      10.244.0.46   minikube   <none>           <none>

```



小结：

这里就知道了什么是replica，这里指的就是一个deployment有多少个副本，

```shell
(base) dominiczhu@ubuntu:~$ kubectl get rs
NAME                            DESIRED   CURRENT   READY   AGE
kubernetes-bootcamp-9bc58d867   2         2         2       2d

## 顺道服务删了

kubectl delete service kubernetes-bootcamp
```



### 更新你的应用

先把这一节要用的镜像捞下来

```shell
docker pull jocatalin/kubernetes-bootcamp:v2

minikube image load jocatalin/kubernetes-bootcamp:v2
# 验证一下
kubectl run test-bootcamp --image=jocatalin/kubernetes-bootcamp:v2

# 成功，删除
kubectl delete pod test-bootcamp
```



开始升级

```shell
(base) dominiczhu@ubuntu:~$ kubectl get pods
NAME                                  READY   STATUS    RESTARTS     AGE
kubernetes-bootcamp-9bc58d867-jjrb2   1/1     Running   0            37m
kubernetes-bootcamp-9bc58d867-x9x9v   1/1     Running   1 (9h ago)   2d

(base) dominiczhu@ubuntu:~$ kubectl describe pods
Containers:
  kubernetes-bootcamp:
    Container ID:   docker://18cd7539e1674cd0dbd4905d15160ce9e299ea36cf579c70bfff5b6b8e6a4d37
    Image:          gcr.io/google-samples/kubernetes-bootcamp:v1



(base) dominiczhu@ubuntu:~$ kubectl set image deployments/kubernetes-bootcamp kubernetes-bootcamp=jocatalin/kubernetes-bootcamp:v2
deployment.apps/kubernetes-bootcamp image updated
(base) dominiczhu@ubuntu:~$ kubectl get pods
NAME                                  READY   STATUS        RESTARTS     AGE
kubernetes-bootcamp-9bc58d867-jjrb2   1/1     Terminating   0            38m
kubernetes-bootcamp-9bc58d867-x9x9v   1/1     Terminating   1 (9h ago)   2d
kubernetes-bootcamp-c8bff69bf-7pgnp   1/1     Running       0            3s
kubernetes-bootcamp-c8bff69bf-lprkq   1/1     Running       0            4s

(base) dominiczhu@ubuntu:~$ kubectl get pods
NAME                                  READY   STATUS    RESTARTS   AGE
kubernetes-bootcamp-c8bff69bf-7pgnp   1/1     Running   0          51s
kubernetes-bootcamp-c8bff69bf-lprkq   1/1     Running   0          52s

(base) dominiczhu@ubuntu:~$ kubectl expose deployment/kubernetes-bootcamp --type="NodePort" --port 8080
service/kubernetes-bootcamp exposed
(base) dominiczhu@ubuntu:~$ kubectl get services/kubernetes-bootcamp -o go-template='{{(index .spec.ports 0).nodePort}}'
32543

(base) dominiczhu@ubuntu:~$ curl http://"$(minikube ip):32543"
Hello Kubernetes bootcamp! | Running on: kubernetes-bootcamp-c8bff69bf-lprkq | v=2

# 确认升级
(base) dominiczhu@ubuntu:~$ kubectl rollout status deployments/kubernetes-bootcamp
deployment "kubernetes-bootcamp" successfully rolled out

(base) dominiczhu@ubuntu:~$ kubectl describe pods | grep "Image" -C 5
  IP:           10.244.0.58
Controlled By:  ReplicaSet/kubernetes-bootcamp-c8bff69bf
Containers:
  kubernetes-bootcamp:
    Container ID:   docker://677d25ff66b6522d994f09a5486c22b40ccfe20bb89fa083f6a834dc0007c1d4
    Image:          jocatalin/kubernetes-bootcamp:v2

```



尝试一次失败的更新



```shell

(base) dominiczhu@ubuntu:~$ kubectl set image deployments/kubernetes-bootcamp kubernetes-bootcamp=gcr.io/google-samples/kubernetes-bootcamp:v10
deployment.apps/kubernetes-bootcamp image updated

(base) dominiczhu@ubuntu:~$ kubectl get pods
NAME                                   READY   STATUS         RESTARTS   AGE
kubernetes-bootcamp-75bd5fd495-xzsz2   0/1     ErrImagePull   0          21s
kubernetes-bootcamp-c8bff69bf-7pgnp    1/1     Running        0          7m27s
kubernetes-bootcamp-c8bff69bf-lprkq    1/1     Running        0          7m28s

# 这个镜像本身不存在
(base) dominiczhu@ubuntu:~$ kubectl describe pods kubernetes-bootcamp-75bd5fd495-xzsz2
  Normal   Pulling    2s (x2 over 34s)  kubelet            Pulling image "gcr.io/google-samples/kubernetes-bootcamp:v10"

(base) dominiczhu@ubuntu:~$ kubectl rollout undo deployments/kubernetes-bootcamp
deployment.apps/kubernetes-bootcamp rolled back
(base) dominiczhu@ubuntu:~$ kubectl get pods
NAME                                  READY   STATUS    RESTARTS   AGE
kubernetes-bootcamp-c8bff69bf-7pgnp   1/1     Running   0          8m44s
kubernetes-bootcamp-c8bff69bf-lprkq   1/1     Running   0          8m45s

# 记得清理本地集群：
kubectl delete deployments/kubernetes-bootcamp services/kubernetes-bootcamp

```





# 概念

## 概述

```shell
kubectl apply -f nginx-deployment.yaml
kubectl get deployment
```

### Kubernetes对象
我理解所谓的对象，就是K8s集群中的一种抽象的定义，节点、部署、pod都可以是一个对象，一个对象最关键的就是期望状态和当前状态，k8s作为一个集群，会尽可能让一个对象达到期望的状态。创建对象的时候通过Spec描述Kubernetes对象，例如描述一个Deployment需要多少个副本；

#### Kubernetes对象管理

有三种方式

1. 指令式命令：就是kubectl的命令直接创建、管理对象，并且在命令行里配置对象；
2. 指令式对象配置：仍然使用kubectl，但是相关对象的配置都在一个yaml文件中。比如`kubectl create -f nginx.yaml`就是按照`nginx.yaml`来**创建**，这里指明了具体的行为是create。
3. 声明式对象配置：`kubectl apply`，作用就是k8s自动来看当前集群的状态和yaml配置文件的差异，然后使得k8s集群变更为yaml配置文件的状态，不需要手动指定动作，k8s自己判断要做什么动作，例如创建、变更，例如下面功能，通过`kubectl diff`来比较当前与配置文件的差异，例如如果发现配置文件中的对象不存在，那么kubectl就希望创建一个，但diff操作只是预览，通过apply才是真正执行来修正差异，可能是创建、删除、变更名称等等。

  ```shell
kubectl diff -f nginx-deployment.yaml
kubectl apply -f nginx-deployment.yaml
```



#### 对象名称和ID


在spec定义中，会有一个apiVersion定义，例如`apiVersion: apps/v1`，但是这个`v1`并不会作为区分对象的一个变量。

#### 标签和选择算符

因为名字要在同一资源下唯一，所以k8s提供了标签功能，用于给对象打上标记，这些标记可以用来按照使用者自己的意愿来帮助区分不同的对象。

##### 标签选择算符

用于根据标签选择一部分的对象，例如文中提到的，可以基于标签来让一个pod运行在某个节点上。

##### 在 API 对象中设置引用

就是说k8s里内置的对象也是通过label来定位其他对象的，例如service就是通过label和标签选择算符来圈定一部分其他对象的。

```shell
kubectl get pods -l app=nginx -L tier

```

这段指令的含义是过滤所有label中`app=nginx`的pod，并且新增一列展示这些pod的tier标签。



#### 命名空间



在启动了 minikube之后，可以查看并操作 命名空间了



```shell
(base) dominiczhu@ubuntu:~/Desktop$ kubectl get namespace
NAME                   STATUS   AGE
default                Active   3d20h
kube-node-lease        Active   3d20h
kube-public            Active   3d20h
kube-system            Active   3d20h
kubernetes-dashboard   Active   3d20h

# 查看当前所有命名空间中的pods
(base) dominiczhu@ubuntu:~/Desktop$ kubectl get pods --all-namespaces
NAMESPACE              NAME                                         READY   STATUS    RESTARTS        AGE
default                kubernetes-bootcamp-9bc58d867-x9x9v          1/1     Running   0               19h
kube-system            coredns-668d6bf9bc-8kvp2                     1/1     Running   7 (2d23h ago)   3d20h
kube-system            etcd-minikube                                1/1     Running   7 (2d23h ago)   3d20h
kube-system            kube-apiserver-minikube                      1/1     Running   7 (2d23h ago)   3d20h
kube-system            kube-controller-manager-minikube             1/1     Running   7 (2d23h ago)   3d20h
kube-system            kube-proxy-4pdnz                             1/1     Running   7 (2d23h ago)   3d20h
kube-system            kube-scheduler-minikube                      1/1     Running   7 (2d23h ago)   3d20h
kube-system            storage-provisioner                          1/1     Running   14 (19h ago)    3d20h
kubernetes-dashboard   dashboard-metrics-scraper-5d59dccf9b-lpv4m   1/1     Running   0               19h

```

先创建一个 命名空间，这次尝试用一下指令式对象配置，先创建一个application.yaml文件

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: dev  # 命名空间名称
  labels:    # 可选标签，用于分类和选择
    env: development
```





随后启动一个pod并且赋予命名空间，

```shell
# 创建一个命名空间
(base) dominiczhu@ubuntu:~/Coding/talk-is-cheap/container/kubernetes/tutorials$ kubectl apply -f create-namespace.yaml
namespace/dev created

(base) dominiczhu@ubuntu:~$ kubectl get namespace -L env
NAME                   STATUS   AGE     ENV
default                Active   3d21h   
dev                    Active   69s     development
kube-node-lease        Active   3d21h   
kube-public            Active   3d21h   
kube-system            Active   3d21h   
kubernetes-dashboard   Active   3d21h   

```
这次换个方法来拉镜像，依靠另一位大佬的[工程](https://github.com/tech-shrimp/docker_image_pusher)

```shell
docker pull xxxxxx.cn-hangzhou.personal.cr.aliyuncs.com/goose-good/busybox:1.37.0
docker tag xxxxxx.cn-hangzhou.personal.cr.aliyuncs.com/goose-good/busybox:1.37.0 goose-good/busybox:1.37.0


```

这时候还不行，因为我使用的是minikube，minikube此时并不知道这个镜像到本地了，需要重新加载，参考[博客](minikube(k8s单机)安装和dashboard镜像拉取不到的处理)与[博客]([在Minikube中运行本地Docker镜像的简单方式](https://www.cnblogs.com/xiao2/p/16047455.html))

> 因为Kubernetes默认从注册表中提取镜像，所以Kubernetes一般是不会使用本地镜像，并且在生产环境中也不应该使用本地镜像。



```shell
# 加载镜像
minikube image load goose-good/busybox:1.37.0
```

Q: 但是针对一些镜像，又能直接用本地docker的，例如`gcr.io/google-samples/kubernetes-bootcamp:v1`

A: 可以查看minikube里的镜像，如下：

```shell
(base) dominiczhu@ubuntu:~/Desktop$ minikube image ls
registry.k8s.io/pause:3.10
registry.k8s.io/metrics-server/metrics-server:<none>
registry.k8s.io/kube-scheduler:v1.32.0
registry.k8s.io/kube-proxy:v1.32.0
registry.k8s.io/kube-controller-manager:v1.32.0
registry.k8s.io/kube-apiserver:v1.32.0
registry.k8s.io/etcd:3.5.16-0
registry.k8s.io/e2e-test-images/agnhost:2.39
registry.k8s.io/coredns/coredns:v1.11.3
registry.cn-hangzhou.aliyuncs.com/google_containers/metrics-scraper:v1.0.8
registry.cn-hangzhou.aliyuncs.com/google_containers/dashboard:v2.7.0
gcr.io/k8s-minikube/storage-provisioner:v5
gcr.io/google-samples/kubernetes-bootcamp:v1
docker.io/kubernetesui/metrics-scraper:<none>
docker.io/kubernetesui/dashboard:<none>
docker.io/goose-good/busybox:1.37.0

```





```shell


# 如果要用本地的已经拉下来的镜像，必须指定版本，否则还是会重新拉
(base) dominiczhu@ubuntu:~$ kubectl run my-busybox --image=goose-good/busybox:1.37.0 --namespace=dev
pod/my-busybox created

# 也可以用下面的方法创建
(base) dominiczhu@ubuntu:~/Desktop$ kubectl apply -f - <<EOF
apiVersion: v1
kind: Pod
metadata:
  name: my-busybox
  namespace: dev  # 指定命名空间
spec:
  containers:
  - name: my-goose-busybox
    image: goose-good/busybox:1.37.0  # 直接使用本地标签
    imagePullPolicy: IfNotPresent  # 重要！
EOF


# 查看 这是因为busybox本身就不是一个可以在后台持续运行的容器，所以直接completed了
(base) dominiczhu@ubuntu:~/Desktop$ kubectl get pods --namespace=dev
NAME         READY   STATUS      RESTARTS      AGE
my-busybox   0/1     Completed   2 (20s ago)   21s

kubectl describe pod my-busybox --namespace=dev


# 删除pod，因为这个pod在某个特定的namespace里，所以必须指定命名空间
(base) dominiczhu@ubuntu:~$ kubectl delete pod my-busybox --namespace=dev
pod "my-busybox" deleted
# 示例：删除名为dev的命名空间
(base) dominiczhu@ubuntu:~/Desktop$ kubectl delete namespace dev
namespace "dev" deleted
```



> You can permanently save the namespace for all subsequent kubectl commands in that context.

意思是说：原本默认的名称空间是default，执行`get pods`操作的时候，如果希望查特定namespace的，必须指定，但是也可以修改当前默认的namespace

> When you create a [Service](https://kubernetes.io/docs/concepts/services-networking/service/), it creates a corresponding [DNS entry](https://kubernetes.io/docs/concepts/services-networking/dns-pod-service/). This entry is of the form `<service-name>.<namespace-name>.svc.cluster.local`, which means that if a container only uses `<service-name>`, it will resolve to the service which is local to a namespace. 

当启动了一个service之后，就会创建一个与这个service相关的DNS入口，对应的域名就是`<service-name>.<namespace-name>.svc.cluster.local`，这个很像一个真正的网站，其他容器可以通过`<service-name>.<namespace-name>.svc.cluster.local`来访问这个service，同一个namespace里的容器可以只使用`<service-name>`访问这个服务；



Q：svc.cluster.local是啥？

A：当创建一个`<service>`的时候，k8s会加这个后缀之后丢进dns服务中，目的是为了和其他的真实的域名做区分吧


Q：如果服务会被解析成`<service-name>.<namespace-name>.svc.cluster.local`，那即使我有一个叫做com的namespace，某个pod如果希望访问`someservice.com`的时候，应该也不会收到这个叫做com的namespace影响，而是应该直接访问外部的dns服务器才对把？

A：参考[Service 与 Pod 的 DNS](https://kubernetes.io/zh-cn/docs/concepts/services-networking/dns-pod-service/)，DNS查询会被自动扩展，如下所示。

> DNS 查询可以使用 Pod 中的 `/etc/resolv.conf` 展开。 Kubelet 为每个 Pod 配置此文件。 例如，对 `data` 的查询可能被扩展为 `data.test.svc.cluster.local`




#### 注解
注解和label类似，只不过label可以用来区分、查询不同的对象，但同时对label的字符限制更多，而注解不能用来区分、查询不同的对象，但对注解的字符限制更少，简单说，注解更像是一些贴在对象上的备注。另外，注解为各类控制器、插件、中间件传递配置参数

```shell

kubectl apply -f create-annotation.yaml


(base) dominiczhu@ubuntu:~/Coding/talk-is-cheap/container/kubernetes/tutorials$ kubectl describe pod annotations-demo
Name:             annotations-demo
Namespace:        default
Priority:         0
Service Account:  default
Node:             minikube/192.168.49.2
Start Time:       Thu, 22 May 2025 13:07:21 +0800
Labels:           <none>
Annotations:      imageregistry: https://hub.docker.com/

```







#### 字段选择算符

就是过滤一些对象的方法和指令，比如过滤metadata.name=xxx的对象。

```shell
(base) dominiczhu@ubuntu:~/Desktop$ kubectl get pods --field-selector status.phase=Running
NAME                                  READY   STATUS    RESTARTS      AGE
kubernetes-bootcamp-9bc58d867-x9x9v   1/1     Running   1 (26m ago)   39h

```






#### Finalizers
和java的finalize方法真的很像，流程大概是：
1. 用户告知K8s要删除一个对象；
2. k8s将这个对象标记为删除中，并查看这个对象的finalizer；
3. 执行这个对象的finalizer进行一些垃圾请理工作，主要是清楚这个对象拥有的资源，我理解例如持有的依赖、内存等等
4. 执行完毕后删除finalizer对象
5. 当全部finalizer对象都被清空后，k8s认为删除操作已经完成，然后真正的删除这个对象自己。



#### 属主与附属

在Finalizers章节里提到了

> Job 控制器还为这些 Pod 添加了“属主引用”，指向创建 Pod 的 Job。 如果你在这些 Pod 运行的时候删除了 Job， Kubernetes 会使用属主引用（而不是标签）来确定集群中哪些 Pod 需要清理。

以最常见的deployment为例，他会创建replicaSet，然后由replicaSet管理每个pod（详细参考自概念-工作负载-replicaSet），那么每个pod就有指向管理自己的replicaSet对象的引用。具体怎样控制删除对象的，请看垃圾回收章节


```shell
(base) dominiczhu@ubuntu:~$ kubectl get pod kubernetes-bootcamp-9bc58d867-x9x9v -o yaml
 ownerReferences:
  - apiVersion: apps/v1
    blockOwnerDeletion: true
    controller: true
    kind: ReplicaSet
    name: kubernetes-bootcamp-9bc58d867
    uid: 6053a328-013e-434f-9fc6-c7ef3f3134c5

```
#### 推荐使用的标签

> 共享标签和注解都使用同一个前缀：app.kubernetes.io。没有前缀的标签是用户私有的。 共享前缀可以确保共享标签不会干扰用户自定义的标签。

> 通过“命名空间前缀”来区分“系统/工具用的标准标签”和“用户自己随便起的标签”，防止两者冲突。

https://www.qianwen.com/share/chat/c1583d7095d24e88958ee50dcc1e2530

#### Storage Version

在k8s中，你定义的各种yaml都会持久化到一个etcd中，StorageVersion指定了按照什么来将数据存储在yaml里

```shell
kubectl apply -f example-crd-v1beta1-storage.yaml
kubectl get crd examples.demo.example.com -o yaml

```

可以看到
```shell
v1beta1:
  storage: true

v1:
  storage: false
```

```shell
kubectl apply -f example-v1.yaml
kubectl get example test -o yaml

# 会发现count:100消失了，但是在kubectl.kubernetes.io/last-applied-configuration却能看到count
```
这是因为，在`example-crd-v1beta1-storage.yaml`中定义了存储版本，`v1beta`的存储版本里没有定义`count`，并不会将`count`持久化到etcd中。

```shell

# 切换storage version
kubectl apply -f example-crd-v1-storage.yaml

kubectl apply -f example-v1.yaml

kubectl get example test -o yaml


```

CRD的作用是：Kubernetes 提供的"你可以自己定义一种资源，我帮你接入 API 系统"，但是 CRD 本身不会做事情真正做事情的是：Controller，因此CRD + Controller = Operator


## Kubernetes 架构

### 概述

- 控制平面：负责管理节点，一般单独搞台机器部署。
- 节点：负责运行Pod

这一章节有一个大体的模糊认识即可，看不懂的可以略过。

#### 控制平面组件

##### Kube-apiserver

回答如下，本质上就是向外提供api接口，用于对外提供集群管理和使用功能。

##### etct

键值存储键值存储数据库，有点类似于zookeeper


##### kube-scheduler

调度器，调度某个pod应该在哪个节点运行的组件。


##### kube-controller-manager

kube-controller-manager 是控制器集合，内部跑各种 Controller，作为客户端主动调用 apiserver 完成集群调谐（Reconcile），循环对比期望状态 vs 实际状态，不断调平集群。例如，保证副本数量严格匹配 spec.replicas，Pod 少了新建、多了删除。

##### cloud-controller-manager

结合起来我的理解，这些功能的作用是通过这个服务与云平台进行交互，例如：

1. 自己的集群里运行了几个服务，但其中几个服务用的是云服务商提供的，例如几个对象存储服务，那么自己集群的control plane就需要通过这个cloud-controller-manager与云服务进行交互。
2. 全部应用都是运行在云服务商中，例如使用的是阿里云的云计算，于是我们可以通过cloud-controller-manager来与阿里云进行交互，新增、删除、管理运行中的pod

#### 节点

先要知道几个基本概念，：

- 节点代表物理机、虚拟机，真实的运行计算的机器。
- 容器代表基于线程的虚拟化运行的应用，区别于蕴含完整操作系统、相互隔离的虚拟机，容器仍然是复用当前系统的内核等资源，不包含完整的系统，只包含必要的依赖，只通过命名空间等手段实现隔离，更加轻量化，容器是是围绕应用构建的，应用关闭了容器就消失了。
- pod：一组容器

##### kubelet

一句话，负责在节点中执行容器的创建、管理

##### kube-proxy

这东西有点类似于实际网络的路由器和网关的作用，关键在于提供网络功能，负责对外提供服务时的网络数据转发、分发。

##### 容器运行时

Container Runtime 就是“负责真正运行容器的东西”。以前很多人会直接把它理解成 Docker，但在现代 Kubernetes 里，Docker 只是 Container Runtime 生态中的一个具体实现，而且现在 Kubernetes 默认不直接使用 Docker，因为Docker Engine 不实现 CRI，Docker Engine使用的也只是containerd

containerd和CRI-O是不同的运行时，目前，kubelet直接通过CRI(Container Runtime Interface)与Container Runtime交互。
```
kubelet
   |
   | CRI
   |
   +----------------+
   |                |
containerd        CRI-O
   |                |
   |                |
 runc             runc

```

runc 是真正创建 Linux Container 的低层工具。runc 很通用，但不是所有场景的最佳选择。


#### 插件

除了控制平面组件和节点之外，提供其余的扩展类的功能。



### 节点

节点作为运行pod的物理/虚拟计算单元，必须提供管理节点的方法，例如注册节点、删除节点或者调度节点。节点支持自动注册，也支持手动注册。对于Kubernetes来说，是通过每个节点的名字来定位唯一一个节点，因此节点名字必须唯一，并且也因此，针对某个节点的重启后部分字段可能不支持热修改，详见文中节点名称唯一性与配置更新的说明。

#### 节点控制器

k8s通过节点控制器来管理节点的状态；

节点控制器的作用：

1. CIDR是什么？在 Kubernetes（K8s）中，**CIDR（无类别域间路由，Classless Inter-Domain Routing）\**是一种用于分配和表示 IP 地址范围的方法，也是 K8s 网络模型的核心概念之一。它通过\**IP 地址 + 前缀长度**的形式（例如 `192.168.0.0/16`）定义一个连续的 IP 地址块，用于集群内部的网络划分和地址分配。

2. "保持节点控制器内的节点列表与云服务商所提供的可用机器列表同步"。针对这个我的理解是，如果我们的节点来自于云服务商，那么节点控制器自然要去监控云服务商提供的机器列表。从而实现后续的节点管理等。。
3. 监控，监控节点健康状况。







### 节点与控制面之间的通信

关键在于双向的相互认证

#### 节点到控制面

节点访问控制面的api server组件，同时需要给节点的kubelet配置**客户端证书**，用于告知api server自己是可信的。



#### 控制面到节点

- apiserver访问节点的kubelet，下面这段话好像没有翻译完全，“为了对这个连接进行认证，使用 `--kubelet-certificate-authority` 标志给 API 服务器提供一个根证书包，用于 kubelet 的服务证书。”，应该“是用于验证kubelet 的服务证书”。我不知道这个服务证书和上面节点到控制面的客户端证书是不是同一个证书，感觉像是同一个，因为作用都是为了向控制面证明自己是个真实的节点；这里有点怪，并不像我们访问网页那样，我们的客户端浏览器验证服务器证书，而是反过来的，是api server去验证每个节点的证书是否可信。
- API服务器直接到节点、POD或者服务：这个我理解就是api server直接访问节点的ip之类的吧，是说没有建立安全的链接。

### 控制器

控制器的概念在自动化控制里出现过，在后端开发面向前端提供接口时，也有很多controller，例如`RestController`。他的含义是：通过控制、操纵其他组件的行为，从而达到什么目标。思考一下，在后端的开发过程中，controller做的是不是就是这个活。

控制器模式有两种控制手段：

1. 通过API服务器控制：就是说一个控制器与API server交互，并利用API server来控制、操作部分功能，从而达到某个目的；
2. 直接控制：这类控制器不是对集群内的节点操作，而是对集群外进行操作，文中 的例子中，相当于控制器从Api Server领来了一个保持集群中节点数量的任务，然后对外部进行操作，保证拥有一定数量的节点。

### 租约（Lease）

作用：

1. 心跳：通过update租约来实现心跳
2. 领导者选举：类似于通过etcd的租约机制来进行领导者选举，即竞争同一个租约 ID、续期租约来保持领导权（类似于心跳）、非领导者节点监听租约ID

### 云控制器管理器

控制器的管理器，作用就是管理一组控制器。参考设计图，云控制器管理器可以让自己的集群与云平台 的API交互，例如在混合云中，一部分节点是企业自己私有的，一部分是公有云上的，私有云和公有云需要统一起来管理，那么就需要这个云控制器与私有云、公有云进行交互。从而让K8s能够管理起来整体的集群，才能够知道：集群里有多少节点、节点是否健康等等。

> 在 Kubernetes（K8s）架构中，** 云控制器管理器（Cloud Controller Manager，CCM）**是用于集成云提供商特定功能的组件**

#### 云控制器管理器的功能

##### 服务控制器

服务管理器（Service Controller）** 是云控制器管理器中的一个核心模块，主要负责管理 K8s 中的服务（Service）资源与云提供商底层基础设施（如负载均衡器、DNS 等）的对接。

> K8s 中的服务支持多种类型（如 `ClusterIP`、`NodePort`、`LoadBalancer` 等），其中 **`LoadBalancer` 类型**需依赖云提供商的负载均衡器实现外部访问。
> 服务管理器的主要职责包括：
>
> 
>
> - **创建云负载均衡器**：当用户创建一个 `LoadBalancer` 类型的服务时，服务管理器会调用云提供商的 API，在底层云基础设施中创建对应的负载均衡器实例，并将其与 K8s 服务关联。
> - **配置监听规则与后端节点**：将负载均衡器的监听端口、协议等参数与 K8s 服务的端口配置同步，并将服务对应的 Pod 所在节点（或 IP）注册为负载均衡器的后端，实现流量转发。

我理解：比如说我们有一个k8s集群，里面启动了七八个pod对外提供服务，然后我问们使用阿里云的负载均衡器，当有一个流量请求我们的pod服务的时候，会先打到阿里云的负载均衡器，然后负载均衡器来判断要将流量分发到哪一个pod上。

### 容器运行时接口（CRI）

因为应用还是要依靠container runtime来运行，以docker为例，具体的容器还是由docker来创建和运行的；而k8s通过每个节点的kubelet来启动docker容器的接口，就是这个CRI

### 垃圾收集

[快速复习：我在学习k8s文档，阅读这篇文章的时候看得不太明白，请你向我讲解，https://kubernetes.io/zh-cn/docs/concepts/architecture/garbage-collection/](https://chatgpt.com/s/t_6a51c7ede5ac8191871189ab592fa356)





### 混合版本代理
[我在学习k8s文档，阅读这篇文章的时候看得不太明白，请你向我讲解，https://kubernetes.io/zh-cn/docs/concepts/architecture/mixed-version-proxy/](https://chatgpt.com/s/t_6a51c8b403c881918e5cfb752bec95f6)





### 关于minikube
当你执行`minikube start`的时候，实际上做的是：帮你创建一个运行 Kubernetes 的"节点（Node）"，而这个 Node 可以是虚拟机或者Docker容器，这个节点具备kubectl+容器运行时。不仅如此，这个节点还自带Control Plane Node

也就是说，通常生产环境下是
```
                Kubernetes Cluster

        +-----------------------------+
        |      Control Plane          |
        |                             |
        | kube-apiserver              |
        | etcd                        |
        | scheduler                   |
        | controller-manager          |
        +-----------------------------+

                    │

        +-----------+-----------+
        |                       |
+---------------+       +---------------+
| Worker Node 1 |       | Worker Node 2 |
| kubelet       |       | kubelet       |
| containerd    |       | containerd    |
+---------------+       +---------------+
```

Minikube 默认是


```
                Minikube

           +-------------------+
           |   一个 Node        |
           |                   |
           | kube-apiserver    |
           | etcd              |
           | scheduler         |
           | controller-manager|
           | kubelet           |
           | containerd        |
           +-------------------+
```

更进一步的，执行的是`minikube start --driver=xxx`，这个`xxx`默认情况下是`auto-detect`，比如我的电脑里有docker，所以这个操作的完整含义是，使用docker创建一个k8s节点。你可以通过`docker ps`看到有一个minikube的容器在运行。

回到开头minikube失效的问题，在真实的生产环境里，worker称为k8s集群中的一个节点的过程中，其中一步是“下载证书”，这个证书如果失效了，节点就无法运行。

## 容器

### 镜像

如果不指定仓库，那么就默认使用的是docker的公共镜像。在 Kubernetes（K8s）中，**本身并不直接存储或管理镜像**，镜像通常存储在镜像仓库（如 Docker Hub、私有仓库）或节点本地缓存中。

#### 镜像名称
根据镜像名称来判断是从哪个仓库来拉镜像。

#### 带镜像索引的多架构镜像

相当于nginx:latest背后实际上是不同架构的不同镜像，第一次拉取的时候会获取一个manifests，然后根据当前系统拉取对应的镜像。
```json
{
  "manifests":[
    {
      "platform":{
        "os":"linux",
        "architecture":"amd64"
      },
      "digest":"sha256:aaa"
    },
    {
      "platform":{
        "os":"linux",
        "architecture":"arm64"
      },
      "digest":"sha256:bbb"
    }
  ]
}

```
https://chatgpt.com/s/t_6a4f36ed64ac8191b4674ef026c18f1b

#### 使用私有仓库 
大体看了个概念，就是说可以通过各种方式使用私有仓库，具体还是要看任务章节里的实操。

### 容器环境
指的是k8s给容器提供的信息、资源，例如文件系统、告诉每个容器你的hostnaame是啥、告诉每个容器都有啥其他的对象（例如service）。比如某个pod启动的时候，这个pod里的容器会获得当前名字空间的其他service信息作为环境变量。

### 容器运行时类（Runtime Class）

用来确认使用什么容器运行时。

默认：

Pod
 |
containerd
 |
runc

如果：

runtimeClassName: kata

可能变成：

Pod
 |
containerd
 |
kata-runtime
 |
轻量虚拟机
 |
Linux kernel

如果：

runtimeClassName: gvisor

变成：

Pod
 |
containerd
 |
runsc
 |
gVisor sandbox



## 工作负载

这一章节的目的是

> 理解 Kubernetes 中可部署的最小计算对象 Pod 以及辅助 Pod 运行的上层抽象。

deamonSet

> **DaemonSet** 是一种用于部署系统级守护进程的控制器，它确保在集群的每个节点（或指定节点）上**恰好运行一个副本**的 Pod。DaemonSet 通常用于部署监控代理、日志收集器、网络插件等需要在所有节点上运行的系统组件。

### pod

**什么是pod**

Pod是一种特定于应用的“逻辑主机”；在一个节点上运行多个Pod应用，就像不使用虚拟化技术在同一台物理机运行多个程序一样。

todo：Q：什么情况下需要**运行多个协同工作的容器的 Pod**？如何配置？

A：后面提到的sidecar和init容器，和主应用容器就是在一起的，视为一个pod



```shell
(base) dominiczhu@ubuntu:pods$ pwd
/home/dominiczhu/Coding/talk-is-cheap/container/kubernetes/tutorials/concept/workloads/pods
(base) dominiczhu@ubuntu:pods$ minikube image load nginx:1.27.3
(base) dominiczhu@ubuntu:pods$ kubectl apply -f simple-pod.yaml 
pod/nginx created
```


**Pod 操作系统**


如果要控制某个pod去哪里，你需要自己写nodeSelector。目前Scheduler 目前根本不看.spec.os.name这个字段。

**Pod模板**

前面提到过，我们一般不会直接创建Pod，而是创建Deployment等工作负载，让Deployment控制器来创建负载，下面是Job工作负载的例子

```shell
(base) dominiczhu@ubuntu:pods$ kubectl apply -f job-pod.yaml 
job.batch/hello created

(base) dominiczhu@ubuntu:pods$ kubectl get pod
NAME          READY   STATUS    RESTARTS   AGE
hello-p6hpt   1/1     Running   0          48s
(base) dominiczhu@ubuntu:pods$ kubectl logs hello-p6hpt
Hello, Kubernetes!
```

**Pod生成**
当kubectl apply的时候，会把最新的spec送往api server，但是可能pod还没有真的完成更新。

因此，可以把这两个字段理解为：
metadata.generation：API Server 中 最新期望状态（Desired State） 的版本号。代表Spec（期望状态）的版本号，无法指定，由API server管理。
status.observedGeneration：kubelet 已经观察并据此生成当前状态（Observed State） 的版本号。

**pod联网**

> 在同一个 Pod 内，所有容器共享一个 IP 地址和端口空间，并且可以通过 `localhost` 发现对方。

参考https://www.doubao.com/thread/w311f3926edc93aee，看起来即使同一个pod里有多个容器，容器可以通过localhost访问到彼此，也就是说，对于每个容器来说，他们并不知道他们访问的其实是不同的容器

**静态pod**

[静态 Pod 的存在主要是为了启动 Kubernetes 自己。作为一个bootstrap](https://chatgpt.com/s/t_6a51cec172fc8191bac05dfa16713e6a)

**Pod 管理多个容器**

这里又一次提到了特性门控

> 启用 `SidecarContainers` [特性门控](https://kubernetes.io/zh-cn/docs/reference/command-line-tools-reference/feature-gates/)（默认启用）允许你为 Init 容器指定 `restartPolicy: Always`。

在 Kubernetes（K8s）中，**特性门控（Feature Gates）\**是一种\**动态开关机制**，用于控制实验性或不稳定功能的启用与禁用。通过特性门控，K8s 团队可以在不影响主版本稳定性的前提下，向用户提前开放新功能进行测试，同时保留在生产环境中禁用风险功能的能力。其实就是一些灰度出来的功能罢了，门控就是控制功能是否应用的开关。

上文中“允许你为 Init 容器指定 `restartPolicy: Always`”，就是允许你使用边车容器

**看不懂的部分**

pod安全设置、

#### Pod的生命周期



**Pod 阶段**

Pod 的 status 字段是一个 PodStatus 对象，其中包含一个 phase 字段。而PodCondition是更细粒度的细粒度多维检查标记，是一组布尔型状态数组，用来描述 Pod各个环节是否正常，排查故障全靠它。 

[PodCondition是什么](https://chatgpt.com/s/t_6a4fb4bbe9048191b08aa6d6a1e10778)



**Pod 如何处理容器问题**

在刚开始拉取镜像失败的时候，会发现pod在反复重试，每两次重试之间的事件间隔都以指数增长，这个就是回退延迟机制。而CrashLoopBackOff说明当前这个pod在反复的失败中，即只要重试过一次失败了，这个pod就是这个状态了。可以通过`kubectl describe`看到

**Pod就绪探针-ReadinessProb**

https://chatgpt.com/s/t_6a4fb9d248c4819184f3dca9bdfdd2fb
readinessProbe（就绪探针）用于告诉 Kubernetes：这个容器现在是否已经可以对外提供服务。这个就绪探针是由kubelet提供的，并且不支持扩展。
```shell
kubectl apply -f readiness-prob.yaml
kubectl describe pod nginx-readiness


kubectl describe pod nginx-readiness

kubectl delete pod nginx-readiness

```


**就绪门控**

ReadinessProb是由kubelet自己管理和控制的，用来管理这个pod自身是否满足了什么条件，但是如果这个pod不光自身要满足条件，也要满足外界的某个条件后（比如负载均衡器（Load Balancer）还得把它注册进去。），才能提供服务，readinessProb就不够用了，readinessGate相当于给外面提供了一个功能，可以由外界控控制一个容器是否满足自定义的就绪条件

readinessProbe 是"我（Pod）自己说我准备好了"。
ReadinessGate 是"别人（Controller）说你还不能接流量"。

https://chatgpt.com/s/t_6a4fba0c2ce081918ec6956bebcf791f

> 你在日常开发 Spring Boot、Nginx、Go 服务时，99% 的情况下只会配置 readinessProbe。ReadinessGate 更多是 Kubernetes 平台开发者、Operator 开发者、云厂商控制器开发者使用的高级扩展能力。

```shell
(base) dominiczhu@ubuntu:pod-lifecycle$ pwd
/home/dominiczhu/Coding/talk-is-cheap/container/kubernetes/tutorials/concept/workloads/pods/pod-lifecycle

(base) dominiczhu@ubuntu:pod-lifecycle$ kubectl apply -f readiness-pod.yaml 
pod/web-server created

# 查看这个pod，会发先Condition还不是ready，根据condition.ready的定义，此时这个pod无法为请求提供服务
(base) dominiczhu@ubuntu:pod-lifecycle$ kubectl describe pod/web-server
Readiness Gates:
  Type                              Status
  load-balancer.example.com/ready   <none> 
Conditions:
  Type                        Status
  PodReadyToStartContainers   True 
  Initialized                 True 
  Ready                       False 
  ContainersReady             True 
  PodScheduled                True 
  
  
(base) dominiczhu@ubuntu:pod-lifecycle$ kubectl expose pod/web-server --type="NodePort" --port 80
service/web-server exposed
# 可以发现READINESS GATES没有就绪
(base) dominiczhu@ubuntu:pod-lifecycle$ kubectl get pod -o wide
NAME         READY   STATUS    RESTARTS   AGE     IP            NODE       NOMINATED NODE   READINESS GATES
web-server   1/1     Running   0          7m51s   10.244.0.77   minikube   <none>           0/1
(base) dominiczhu@ubuntu:pod-lifecycle$ kubectl get service
NAME         TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)        AGE
kubernetes   ClusterIP   10.96.0.1      <none>        443/TCP        7d2h
web-server   NodePort    10.98.237.69   <none>        80:30964/TCP   5s
# 无法访问
(base) dominiczhu@ubuntu:pod-lifecycle$ curl http://"$(minikube ip):30964"
# curl: (7) Failed to connect to 192.168.49.2 port 30964 after 0 ms: Couldn't connect to server

# 查看pod的状态
(base) dominiczhu@ubuntu:pod-lifecycle$ kubectl get pod/web-server -o json
    "status": {
        "conditions": [
        ...{
                "lastProbeTime": null,
                "lastTransitionTime": "2025-05-24T14:33:58Z",
                "message": "corresponding condition of pod readiness gate \"load-balancer.example.com/ready\" does not exist.",
                "reason": "ReadinessGatesNotReady",
                "status": "False",
                "type": "Ready"
            }
        ...]
        
# 所以我们只要在status.conditions中新增"load-balancer.example.com/ready":"True"的condition就好了
# 但是命令 kubectl patch 不支持修改对象的状态。 如果需要设置 Pod 的 status.conditions，应用或者 Operators 需要使用 PATCH 操作。所以下面的操作不会带来任何结果，需要客户端。
# patch：直译为补丁，可以直接修改对象的内容
kubectl patch pod/web-server --type=json  -p '[
  {
    "op": "add",
    "path": "/status/conditions/-",
    "value": {
      "type": "load-balancer.example.com/ready",
      "status": "True",
      "reason": "Configured",
      "message": "Load balancer has been configured successfully"
    }
  }
]'
```



创建个python客户端

```shell
(base) dominiczhu@ubuntu:pod-lifecycle$ conda create --name k8s python=3.10
(base) dominiczhu@ubuntu:pod-lifecycle$ conda activate k8s
(k8s) dominiczhu@ubuntu:pod-lifecycle$ pip install kubernetes
(k8s) dominiczhu@ubuntu:pod-lifecycle$ python patch-status-conditions.py 
Node: minikube, Status: Ready
....

(base) dominiczhu@ubuntu:pod-lifecycle$ kubectl describe pod/web-server
Readiness Gates:
  Type                              Status
  load-balancer.example.com/ready   True 
Conditions:
  Type                              Status
  load-balancer.example.com/ready   True 
  PodReadyToStartContainers         True 
  Initialized                       True 
  Ready                             True 
  ContainersReady                   True 
  PodScheduled                      True 
  
(base) dominiczhu@ubuntu:pod-lifecycle$ kubectl expose pod/web-server --type="NodePort" --port 80
service/web-server exposed
(base) dominiczhu@ubuntu:pod-lifecycle$ kubectl get service
NAME         TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)        AGE
kubernetes   ClusterIP   10.96.0.1       <none>        443/TCP        7d3h
web-server   NodePort    10.102.101.36   <none>        80:32650/TCP   7s
(base) dominiczhu@ubuntu:pod-lifecycle$ curl http://"$(minikube ip):32650"
可以正常访问

```



#### Init容器

**使用 Init 容器、示例**

直接看示例来理解吧，总的来说就只是在创建容器之前，做一些准备工作，从而控制容器的启动或者为主容器做一些准备

**使用initpod的情况**

```shell
(base) dominiczhu@ubuntu:init-container$ kubectl apply -f init-pods.yaml 
pod/myapp-pod created
(base) dominiczhu@ubuntu:init-container$ kubectl get -f init-pods.yaml 
NAME        READY   STATUS     RESTARTS   AGE
myapp-pod   0/1     Init:0/2   0          9s

(base) dominiczhu@ubuntu:init-container$ kubectl describe -f init-pods.yaml 
  init-mydb:
    Container ID:  
    Image:         goose-good/busybox:1.37.0
    Image ID:      
    Port:          <none>
    Host Port:     <none>
    Command:
      sh
      -c
      until nslookup mydb.$(cat /var/run/secrets/kubernetes.io/serviceaccount/namespace).svc.cluster.local; do echo waiting for mydb; sleep 2; done
    State:          Waiting
      Reason:       PodInitializing
 
 
 kubectl logs myapp-pod -c init-myservice
# 准备init-contianer需要的service
 (base) dominiczhu@ubuntu:init-container$ kubectl apply -f my-db-service.yaml
service/myservice created
service/mydb created

# 可以看到init-container的状态已经是Terminated,reason是Completed
(base) dominiczhu@ubuntu:init-container$ kubectl describe -f init-pods.yaml 
Init Containers:
  init-myservice:
    Container ID:  docker://c5f689109b60faae19d8f0d97ca8d4901fdd642d7d00d3f80ec9c1c75a165efc
    Image:         goose-good/busybox:1.37.0
    Image ID:      docker://sha256:ff7a7936e9306ce4a789cf5523922da5e585dc1216e400efb3b6872a5137ee6b
    Port:          <none>
    Host Port:     <none>
    Command:
      sh
      -c
      until nslookup myservice.$(cat /var/run/secrets/kubernetes.io/serviceaccount/namespace).svc.cluster.local; do echo waiting for myservice; sleep 2; done
    State:          Terminated
      Reason:       Completed
# 主容器启动了
(base) dominiczhu@ubuntu:init-container$ kubectl get -f init-pods.yaml 
NAME        READY   STATUS    RESTARTS   AGE
myapp-pod   1/1     Running   0          2m27s

```

**看不懂的部分**
具体行为

#### 边车容器

Kubernetes 将边车容器作为 [Init 容器](https://kubernetes.io/zh-cn/docs/concepts/workloads/pods/init-containers/)的一个特例来实现， Pod 启动后，边车容器仍保持运行状态。只要你可以为 Pod 的 `initContainers` 字段中列出的容器指定 `restartPolicy`，这个容器就成为了边车容器。

> 这些可重新启动的**边车（Sidecar）** 容器独立于其他 Init 容器以及同一 Pod 内的主应用容器， 这些容器可以启动、停止和重新启动，而不会影响主应用容器和其他 Init 容器。

sidecar容器挂了并不会影响主应用容器

```shell
minikube image load goose-good/alpine:3

# 创建了一个带有sidecar的deployment，sidecar的作用就是tail -f
(base) dominiczhu@ubuntu:sidecar-containers$ kubectl apply -f deployment-sidecar.yaml 
deployment.apps/myapp created
(base) dominiczhu@ubuntu:sidecar-containers$ kubectl get -f deployment-sidecar.yaml 
NAME    READY   UP-TO-DATE   AVAILABLE   AGE
myapp   1/1     1            1           13s

# 查看边车容器里的日志
(base) dominiczhu@ubuntu:sidecar-containers$ kubectl logs deployment/myapp -c logshipper
tail: can't open '/opt/logs.txt': No such file or directory
tail: /opt/logs.txt has appeared; following end of new file
logging
logging
logging

# 或者这样也可以
(base) dominiczhu@ubuntu:sidecar-containers$ kubectl get pods
NAME                     READY   STATUS    RESTARTS   AGE
myapp-78bd75d687-lp5cb   2/2     Running   0          5m43s
(base) dominiczhu@ubuntu:sidecar-containers$ kubectl logs myapp-78bd75d687-lp5cb
Defaulted container "myapp" out of: myapp, logshipper (init)
(base) dominiczhu@ubuntu:sidecar-containers$ kubectl logs myapp-78bd75d687-lp5cb -c logshipper
tail: can't open '/opt/logs.txt': No such file or directory
tail: /opt/logs.txt has appeared; following end of new file
logging
logging

# 下面是job的例子
(base) dominiczhu@ubuntu:sidecar-containers$ kubectl apply -f job-sidecar.yaml 
job.batch/myjob created
(base) dominiczhu@ubuntu:sidecar-containers$ kubectl logs -f job-sidecar.yaml  
error: error from server (NotFound): pods "job-sidecar.yaml" not found in namespace "default"
(base) dominiczhu@ubuntu:sidecar-containers$ kubectl logs -f job/myjob
Defaulted container "myjob" out of: myjob, logshipper (init)
(base) dominiczhu@ubuntu:sidecar-containers$ kubectl logs -f job/myjob -c logshipper
tail: can't open '/opt/logs.txt': No such file or directory
tail: /opt/logs.txt has appeared; following end of new file
logging
```

**容器内的资源共享**

pod申请的cpu或者内存的资源如下公式所示
Pod Effective Request=max(    普通 Init Container 最大 request,
    应用容器 request 总和    +    Sidecar request 总和)


```shell
kubectl get pods -l app=myapp
kubectl describe pod myapp-db56966bc-6f8xz
# 可以看到这个节点上的所有pod分配的资源，可以看到myapp-db56966bc-6f8xz请求了128mi，limit是256
kubectl describe pod minikube 
```

#### 临时容器

```shell
 kubectl apply -f ephemeral-demo.yaml

# 因为这个容器镜像中没有 shell。
kubectl exec -it ephemeral-demo -- sh
# 有时有必要检查现有 Pod 的状态。例如，对于难以复现的故障进行排查。 在这些场景中，可以在现有 Pod 中运行临时容器来检查其状态并运行任意命令。
kubectl debug -it ephemeral-demo --image=goose-good/busybox:1.37.0 --target=ephemeral-demo 
```


#### 探针
[我是一名java开发工程师，目前在学习k8s文档，请向我进行讲解这篇文章：https://kubernetes.io/docs/concepts/workloads/pods/probes/](https://chatgpt.com/s/t_6a51cb601dc8819198938f39131c8414)

1. Startup:看有没有启动
2. Readiness:看能不能接请求
3. Liveness:看要不要重启

在前面Pod的生命周期中其实见过readinessProb，用来控制这个pod能不承接流量

我有3个问题：
1. 我看你给的例子里，只有关于就绪探针和存活探针的配置，没有启动探针的配置，我猜是不是启动探针是kubelet内置的，并不会提供扩展自定义配置？
2. 是不是先就绪探针成功之后，才会开始存活探针的探测？
3. 容器运行过程中，就绪探针和存活探针可以相互独立探测？

A：https://chatgpt.com/s/t_6a51cca8a2bc8191ba04ae6966c735f5

#### 处理干扰

干扰指的是应用受到了一些影响从而导致不能正常运行，这种影响被称为干扰。干扰预算指的是“能够容忍多少的干扰”，比如可以创建一个需要3个pod额deployment，同时创建maxUnavailable: 1的PodDisruptionBudget对象，并且通过`.spec.selector.matchLabels`匹配到对应pod上，那么代表着虽然这个deployment希望有3个pod副本，但是可以容忍有一个副本挂掉，即最多容忍有一段时间只有1pod不能被删掉。

**PodDisruptionBudget 例子**

讲的很详细了

#### Pod QoS 类

有些pod为了能够稳定的运行下去，在启动的时候就告知集群，我需要多少内存、多少的cpu，集群分配节点的资源的时候，必须保证这些内存、cpu；而有些pod不指定这些；根据申请资源的不同，将pod分为不停的QoS类别，QoS类的不同会影响kubelet的节点压力驱逐行为
> 节点压力驱逐：kubelet 监控集群节点的内存、磁盘空间和文件系统的 inode 等资源。 当这些资源中的一个或者多个达到特定的消耗水平， kubelet 可以主动地使节点上一个或者多个 Pod 失效，以回收资源防止饥饿。

```shell

(base) dominiczhu@ubuntu:deployment$ kubectl get pod/nginx-deployment-ff948bdf8-rkq5l -o json

        "qosClass": "BestEffort",
        "startTime": "2025-05-25T07:18:36Z"

```


#### pod主机名

主机名只会用在系统内部，而完全限定域名（FQDN）将会被用在集群的dns之中。


#### 用户命名空间

看不懂，感觉大体意思是说，在容器里，用户是root，但这个root可以映射到宿主节点的另一个用户身上。

```shell

(base) dominiczhu@ubuntu:sidecar-containers$ kubectl run test-uns --image=nginx:1.27.3
pod/test-uns created
(base) dominiczhu@ubuntu:sidecar-containers$ kubectl get pods
NAME       READY   STATUS    RESTARTS   AGE
test-uns   1/1     Running   0          2s

(base) dominiczhu@ubuntu:sidecar-containers$ kubectl exec test-uns -i -t -- id
uid=0(root) gid=0(root) groups=0(root)
```

但我并不知道容器里的root映射到了谁的身上

#### Downward API

容器需要知道其上层一些配置信息，例如pod里的一些配置信息传递给pod里的容器，例如容器怎么也得知道自己叫啥名、要多少个cpu信息吧，这些信息是通过这个Downward API将这些信息暴露个容器的，具体包括环境变量、[`downwardAPI` 卷中的文件](https://kubernetes.io/zh-cn/docs/tasks/inject-data-application/downward-api-volume-expose-pod-information/)。

### 工作负载管理（重要）

终于到了介绍deployment之类的工作负载了，指的是k8s中运行的应用程序，我们通常是通过他们来构建应用，而不是直接创建pod。




#### deployment

```shell
(base) dominiczhu@ubuntu:deployment$ kubectl get deployments
NAME               READY   UP-TO-DATE   AVAILABLE   AGE
nginx-deployment   3/3     3            3           11s
(base) dominiczhu@ubuntu:deployment$ kubectl rollout status deployment/nginx-deployment
deployment "nginx-deployment" successfully rolled out
(base) dominiczhu@ubuntu:deployment$ kubectl get rs
NAME                         DESIRED   CURRENT   READY   AGE
nginx-deployment-ff948bdf8   3         3         3       42s
(base) dominiczhu@ubuntu:deployment$ kubectl get pods --show-labels
NAME                               READY   STATUS    RESTARTS   AGE   LABELS
nginx-deployment-ff948bdf8-hrnbl   1/1     Running   0          51s   app=my-nginx,pod-template-hash=ff948bdf8
nginx-deployment-ff948bdf8-hx9n6   1/1     Running   0          51s   app=my-nginx,pod-template-hash=ff948bdf8
nginx-deployment-ff948bdf8-rgjv4   1/1     Running   0          51s   app=my-nginx,pod-template-hash=ff948bdf8

# 删除一个pod之后，deployment控制器会再调起一个pod
(base) dominiczhu@ubuntu:deployment$ kubectl delete pod/nginx-deployment-ff948bdf8-rgjv4
pod "nginx-deployment-ff948bdf8-rgjv4" deleted
(base) dominiczhu@ubuntu:deployment$ kubectl get deployments
NAME               READY   UP-TO-DATE   AVAILABLE   AGE
nginx-deployment   3/3     3            3           2m46s

# 可以看到每个pod都有一个pod-template-hash标签，标签的取值都是这个deployment对应的rs的名字的
# 这个标签用于确定每个pod和对应的replicaSet
(base) dominiczhu@ubuntu:deployment$ kubectl get pods --show-labels
NAME                               READY   STATUS    RESTARTS   AGE     LABELS
nginx-deployment-ff948bdf8-fvvnl   1/1     Running   0          12s     app=my-nginx,pod-template-hash=ff948bdf8
nginx-deployment-ff948bdf8-hrnbl   1/1     Running   0          2m51s   app=my-nginx,pod-template-hash=ff948bdf8
nginx-deployment-ff948bdf8-hx9n6   1/1     Running   0          2m51s   app=my-nginx,pod-template-hash=ff948bdf8
```

`kubectl rollout status`的作用是持续观察（Watch）某个资源的滚动发布（Rollout）是否完成。实际上：就是一直观察：Deployment 的 Status。
```
status:
  observedGeneration: 3

  replicas: 3

  updatedReplicas: 3

  readyReplicas: 3

  availableReplicas: 3
```


**更新Deployment**



```shell
# 更新镜像的版本 仅当 Deployment Pod 模板（即 .spec.template）发生改变时，例如模板的标签或容器镜像被更新， 才会触发 Deployment 上线。
(base) dominiczhu@ubuntu:deployment$ kubectl set image deployment/nginx-deployment nginx=goose-good/nginx:1.28.0
deployment.apps/nginx-deployment image updated
(base) dominiczhu@ubuntu:deployment$ kubectl get rs
NAME                          DESIRED   CURRENT   READY   AGE
nginx-deployment-864d95888d   3         3         3       13s
nginx-deployment-ff948bdf8    0         0         0       30m
(base) dominiczhu@ubuntu:deployment$ kubectl get pods
NAME                                READY   STATUS    RESTARTS   AGE
nginx-deployment-864d95888d-2lq2x   1/1     Running   0          18s
nginx-deployment-864d95888d-lhsx5   1/1     Running   0          16s
nginx-deployment-864d95888d-n97tr   1/1     Running   0          17s

(base) dominiczhu@ubuntu:deployment$ kubectl describe deployments
Events:
  Type    Reason             Age   From                   Message
  ----    ------             ----  ----                   -------
  Normal  ScalingReplicaSet  30m   deployment-controller  Scaled up replica set nginx-deployment-ff948bdf8 from 0 to 3
  Normal  ScalingReplicaSet  24s   deployment-controller  Scaled up replica set nginx-deployment-864d95888d from 0 to 1
  Normal  ScalingReplicaSet  23s   deployment-controller  Scaled down replica set nginx-deployment-ff948bdf8 from 3 to 2
  Normal  ScalingReplicaSet  23s   deployment-controller  Scaled up replica set nginx-deployment-864d95888d from 1 to 2
  Normal  ScalingReplicaSet  22s   deployment-controller  Scaled down replica set nginx-deployment-ff948bdf8 from 2 to 1
  Normal  ScalingReplicaSet  22s   deployment-controller  Scaled up replica set nginx-deployment-864d95888d from 2 to 3
  Normal  ScalingReplicaSet  21s   deployment-controller  Scaled down replica set nginx-deployment-ff948bdf8 from 1 to 0
## 也可以这样编辑
kubectl edit deployment/nginx-deployment
```



**回滚**

```shell
# 通过edit修改，搞一个不存在的镜像版本
(base) dominiczhu@ubuntu:deployment$ kubectl edit deployment/nginx-deployment
deployment.apps/nginx-deployment edited
    spec:
      containers:
      - image: goose-good/nginx:1.281.0


(base) dominiczhu@ubuntu:deployment$ kubectl rollout status deployment/nginx-deployment
Waiting for deployment "nginx-deployment" rollout to finish: 1 out of 3 new replicas have been updated...
^C(base) dominiczhu@ubuntu:deployment$ kubectl get rs
NAME                          DESIRED   CURRENT   READY   AGE
nginx-deployment-586f7b497    1         1         0       40s
nginx-deployment-864d95888d   3         3         3       5m11s
nginx-deployment-ff948bdf8    0         0         0       35m
(base) dominiczhu@ubuntu:deployment$ kubectl get pods
NAME                                READY   STATUS             RESTARTS   AGE
nginx-deployment-586f7b497-qdlxr    0/1     ImagePullBackOff   0          49s
nginx-deployment-864d95888d-2lq2x   1/1     Running            0          5m20s
nginx-deployment-864d95888d-lhsx5   1/1     Running            0          5m18s
nginx-deployment-864d95888d-n97tr   1/1     Running            0          5m19s

# 查看历史的rs
(base) dominiczhu@ubuntu:deployment$ kubectl describe deployment
OldReplicaSets:  nginx-deployment-ff948bdf8 (0/0 replicas created), nginx-deployment-864d95888d (3/3 replicas created)
NewReplicaSet:   nginx-deployment-586f7b497 (1/1 replicas created)
```

**更改标签选择算符**
[直接贴过去问chatgpt](https://chatgpt.com/s/t_6a51fb66027081918042c13fc2c9f4ec)

```shell
kubectl apply -f label-selector-updates.yaml
kubectl get deployment label-selector-updates
kubectl get rs -o yaml  -l app=pod-my-nginx
    # ownerReferences:
    # - apiVersion: apps/v1
    #   blockOwnerDeletion: true
    #   controller: true
    #   kind: Deployment
    #   name: label-selector-updates
    #   uid: ebd277a6-4e93-4505-b535-49778f1bd950


kubectl get pod -l app=pod-my-nginx


kubectl delete deployment/label-selector-updates  --cascade=orphan
# 发现这个rs还在，但是他的ownerReferences已经没有了
kubectl get rs -o yaml  -l app=pod-my-nginx

```
对于增加或者变更`selector`中的条件，当重新apply的时候，会从头创建新的rs、pod。但如果是删除了`selector`的其中一个条件，比如将`version: v1`给删掉（不删除定义中的pod的label），然后重新apply

```shell
# 会发现ownerReferences又回来了，这个不是重新创建的，因为hash和创建时间戳都没有变更。
kubectl get rs -o yaml  -l app=pod-my-nginx

    # ownerReferences:
    # - apiVersion: apps/v1
    #   blockOwnerDeletion: true
    #   controller: true
    #   kind: Deployment
    #   name: label-selector-updates
    #   uid: 70b74d5d-e9e7-44e1-9d95-9e12e8946306
```

并且建议，后面将pod spec中的label也同步进行删除，然后进行kubectl apply进行上线。


**检查 Deployment 上线历史**



```shell
# 从头来了一次
kubectl delete -f nginx-deployment.yaml
kubectl apply -f nginx-deployment.yaml

(base) dominiczhu@ubuntu:deployment$ kubectl set image deployment/nginx-deployment nginx=goose-good/nginx:1.28.0
deployment.apps/nginx-deployment image updated

# 打上修订标签
(base) dominiczhu@ubuntu:deployment$ kubectl annotate deployment/nginx-deployment kubernetes.io/change-cause="image updated to 1.28.0"
deployment.apps/nginx-deployment annotated


(base) dominiczhu@ubuntu:deployment$ kubectl rollout history deployment/nginx-deployment
deployment.apps/nginx-deployment 
REVISION  CHANGE-CAUSE
1         <none>
2         image updated to 1.28.0

# 查看修订的详细信息
(base) dominiczhu@ubuntu:deployment$ kubectl rollout history deployment/nginx-deployment --revision=2
deployment.apps/nginx-deployment with revision #8
Pod Template:
  Labels:       app=my-nginx
        pod-template-hash=864d95888d
  Annotations:  kubernetes.io/change-cause: image updated to 1.28.0
  Containers:
   nginx:
    Image:      goose-good/nginx:1.28.0
    Port:       80/TCP
    Host Port:  0/TCP
    Environment:        <none>
    Mounts:     <none>
  Volumes:      <none>
  Node-Selectors:       <none>
  Tolerations:  <none>


kubectl set image deployment/nginx-deployment nginx=goose-good/nginx:1.28212.0

# 回滚到上一版本  
kubectl rollout undo deployment/nginx-deployment
# 回归到指定版本
kubectl rollout undo deployment/nginx-deployment  --to-revision=2
```

**缩放 Deployment**

略

**暂停、恢复 Deployment 的上线过程**

kubectl rollout pause 不会停止服务，也不会停止 Pod，更不会影响 Service 转发流量。

它只是告诉 Deployment Controller：

"我接下来可能还要继续修改 Deployment，请先不要开始滚动更新，等我执行 kubectl rollout resume 后，再把这些修改一次性应用到 ReplicaSet 和 Pod 上。"

因此，在生产环境中，rollout pause 常用于批量修改 Deployment 配置，而不是用于停机维护。

**一些思考**

文章中提到：

1. deployment的状态包含完成，并且只要对应的replicaset创建完成，这个deployment就完成了；
2. deployment的Deployment 的修订历史记录存储在它所控制的 ReplicaSet 中。

可以看出，其实这也是deployment->replicaSet->pod的分层设计，一层管理下一层。



#### ReplicaSet

需要gb-frontend:v5和hello-app两个镜像

```shell

(base) dominiczhu@ubuntu:replicaset$ kubectl apply -f frontend.yaml 
replicaset.apps/frontend created
(base) dominiczhu@ubuntu:replicaset$ kubectl get rs
NAME       DESIRED   CURRENT   READY   AGE
frontend   3         3         3       6s

(base) dominiczhu@ubuntu:replicaset$ kubectl describe rs/frontend

(base) dominiczhu@ubuntu:replicaset$ kubectl get pods
NAME             READY   STATUS    RESTARTS   AGE
frontend-89gkl   1/1     Running   0          46s
frontend-clltj   1/1     Running   0          46s
frontend-v8wr7   1/1     Running   0          46s

# 创建pod-rs，这里面的pod和rs的定义完全匹配，所以相当于让这个几个pod强行加入frontend的管理
(base) dominiczhu@ubuntu:replicaset$ kubectl apply -f pod-rs.yaml 
pod/pod1 created
pod/pod2 created
# 但是会发现pod1和pod2并没有被维持下来，因为副本数已经超了
(base) dominiczhu@ubuntu:replicaset$ kubectl get pods
NAME             READY   STATUS    RESTARTS   AGE
frontend-89gkl   1/1     Running   0          12m
frontend-clltj   1/1     Running   0          12m
frontend-v8wr7   1/1     Running   0          12m

# 反过来，先创建pod1、pod2，再创建replica
(base) dominiczhu@ubuntu:replicaset$ kubectl delete rs/frontend
replicaset.apps "frontend" deleted
(base) dominiczhu@ubuntu:replicaset$ kubectl apply -f pod-rs.yaml 
pod/pod1 created
pod/pod2 created
(base) dominiczhu@ubuntu:replicaset$ kubectl apply -f frontend.yaml 
replicaset.apps/frontend created
# 会发现，因为pod1和pod2与rs的label选择符匹配，所以pod1和pod2也被rs当做他管理的pod的
(base) dominiczhu@ubuntu:replicaset$ kubectl get pods
NAME             READY   STATUS    RESTARTS   AGE
frontend-gdjbz   1/1     Running   0          3s
pod1             1/1     Running   0          8s
pod2             1/1     Running   0          8s
```



#### StatefulSet

StatefulSet 不是因为"有状态"才存在，而是因为它能够为每个 Pod 提供稳定且唯一的身份（名称、DNS、存储），从而满足数据库、中间件等分布式系统对节点身份的要求。他创建的pod名称会是稳定的pod-1/pod-2 当应用只需要"任意一个健康实例"时，用 Deployment；当应用需要"固定的那个实例"时，就应该考虑 StatefulSet。

**稳定的网络 ID**

在“限制”中提到

> - StatefulSet 当前需要[无头服务](https://kubernetes.io/zh-cn/docs/concepts/services-networking/service/#headless-services)来负责 Pod 的网络标识。你需要负责创建此服务。

如果一个service配置了`clusterIP: None`，他就是一个无头service。普通 Service 提供一个固定的虚拟 IP（ClusterIP）；无头 Service 不提供这个虚拟 IP，而是直接把后端 Pod 的 IP 告诉客户端。比如nslookup会直接返回后面的pod的ip



```shell
# 首先要删除无法创建的PersistentVolumeClaims
# 之前我启动了statfulset.yaml，所以pvc里有个这么个东西
(base) dominiczhu@ubuntu:statefulset$ kubectl get pvc
NAME        STATUS    VOLUME   CAPACITY   ACCESS MODES   STORAGECLASS       VOLUMEATTRIBUTESCLASS   AGE
www-web-0   Pending                                      my-storage-class   <unset>                 50m

(base) dominiczhu@ubuntu:statefulset$ kubectl delete pvc/www-web-0
persistentvolumeclaim "www-web-0" deleted

# 随后注释掉storageClassName: "my-storage-class"，就可以发现这个应用启动起来了
(base) dominiczhu@ubuntu:statefulset$ kubectl apply -f statfulset.yaml 

(base) dominiczhu@ubuntu:statefulset$ kubectl get -f statfulset.yaml 
NAME            TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)   AGE
service/nginx   ClusterIP   None         <none>        80/TCP    9m9s

NAME                   READY   AGE
statefulset.apps/web   3/3     9m9s


(base) dominiczhu@ubuntu:statefulset$ kubectl delete -f statfulset.yaml 
service "nginx" deleted
statefulset.apps "web" deleted
# 删除statefuleset以及服务后，发现pvc仍然在，这样的话，再次启动创建statfulset.yaml ，这些pvc也就是数据仍然会被复用
(base) dominiczhu@ubuntu:statefulset$ kubectl get pvc
NAME        STATUS   VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS   VOLUMEATTRIBUTESCLASS   AGE
www-web-0   Bound    pvc-c48936ff-91ef-43c3-92f5-dddaa32d4b5b   1Gi        RWO            standard       <unset>                 7m2s
www-web-1   Bound    pvc-a0ff239e-8865-46f5-b337-2c0fe62b8358   1Gi        RWO            standard       <unset>                 6m42s
www-web-2   Bound    pvc-e927831a-13e9-4f27-beac-8ecffd96d47a   1Gi        RWO            standard       <unset>                 6m22s
```







**PersistentVolumeClaim retention PersistentVolumeClaim 保留**



> The StatefulSet [controller](https://kubernetes.io/docs/concepts/architecture/controller/) adds [owner references](https://kubernetes.io/docs/concepts/overview/working-with-objects/owners-dependents/#owner-references-in-object-specifications) to its PVCs, which are then deleted by the [garbage collector](https://kubernetes.io/docs/concepts/architecture/garbage-collection/) after the Pod is terminated. This enables the Pod to cleanly unmount all volumes before the PVCs are deleted (and before the backing PV and volume are deleted, depending on the retain policy). When you set the `whenDeleted` policy to `Delete`, an owner reference to the StatefulSet instance is placed on all PVCs associated with that StatefulSet.
>
> The `whenScaled` policy must delete PVCs only when a Pod is scaled down, and not when a Pod is deleted for another reason. When reconciling, the StatefulSet controller compares its desired replica count to the actual Pods present on the cluster. Any StatefulSet Pod whose id greater than the replica count is condemned and marked for deletion. If the `whenScaled` policy is `Delete`, the condemned Pods are first set as owners to the associated StatefulSet template PVCs, before the Pod is deleted. This causes the PVCs to be garbage collected after only the condemned Pods have terminated.

这段话说的不明白。

1. 如果whenDelete=Delete，那么在创建pvc之后，这些pvc会拥有一个指向StatefulSet的ownerreference，如下，

   ```shell
   (base) dominiczhu@ubuntu:statefulset$ kubectl apply -f statfulset.yaml 
   service/nginx created
   statefulset.apps/web created
   (base) dominiczhu@ubuntu:statefulset$ kubectl get pvc/www-web-0 -o json | grep "owner" -A 8
           "ownerReferences": [
               {
                   "apiVersion": "apps/v1",
                   "blockOwnerDeletion": true,
                   "controller": true,
                   "kind": "StatefulSet",
                   "name": "web",
                   "uid": "94649009-d845-4230-aa40-623a70dba032"
               }
   ```

   这样的话，如果statefulset被删除，那么随后，根据垃圾收集章节中提到的“当属主对象进入**删除进行中**状态后，控制器会删除其已知的依赖对象。 在删除所有已知的依赖对象后，控制器会删除属主对象。 这时，通过 Kubernetes API 就无法再看到该对象。”，这些pvc也会被删除。

2. 如果whenScale=Delete，那么在缩容的过程中，会往要被删除的pod对应的pvc加一个ownerReference，指向要被删除的POD，从而实现缩容时被删除。

3. "The StatefulSet [controller](https://kubernetes.io/docs/concepts/architecture/controller/) adds [owner references](https://kubernetes.io/docs/concepts/overview/working-with-objects/owners-dependents/#owner-references-in-object-specifications) to its PVCs, which are then deleted by the [garbage collector](https://kubernetes.io/docs/concepts/architecture/garbage-collection/) after the Pod is terminated."

 

#### DaemonSet


DaemonSet 可用于确保所有符合条件的节点都运行该 Pod 的一个副本。这样就可以通过这种pod来监控每个节点的健康情况、收集日志等。


```shell
(base) dominiczhu@ubuntu:daemonset$ kubectl apply -f daemonset.yaml 
daemonset.apps/fluentd-elasticsearch created

(base) dominiczhu@ubuntu:daemonset$ kubectl get ds --namespace kube-system
NAME                    DESIRED   CURRENT   READY   UP-TO-DATE   AVAILABLE   NODE SELECTOR            AGE
fluentd-elasticsearch   1         1         1       1            1           <none>                   3m42s
kube-proxy              1         1         1       1            1           kubernetes.io/os=linux   9d

# 节点亲和性的设置，说明这个pod要选择name in minikube的节点创建pod
(base) dominiczhu@ubuntu:daemonset$ kubectl get pod/fluentd-elasticsearch-5hrdr --namespace kube-system -o json | grep "affinity" -A 20
        "affinity": {
            "nodeAffinity": {
                "requiredDuringSchedulingIgnoredDuringExecution": {
                    "nodeSelectorTerms": [
                        {
                            "matchFields": [
                                {
                                    "key": "metadata.name",
                                    "operator": "In",
                                    "values": [
                                        "minikube"
                                    ]
                                }
                            ]
                        }
                    ]
                }
            }
        },
(base) dominiczhu@ubuntu:daemonset$ kubectl get pod/fluentd-elasticsearch-5hrdr --namespace kube-system -o json | grep "nodeName" -A 20
        "nodeName": "minikube",
```


#### Job

deployment会维持pod一直存在，即使比如说pod对应的容器是个busybox，并且仅仅做一个echo xxx的动作执行完成就退出，deployment会不断restart这几个pod。但是job就不会，运行结束了就是结束了。


这一章节翻译的及其难受

```shell
(base) dominiczhu@ubuntu24LTS:job$ kubectl apply -f job.yaml 
job.batch/pi created
(base) dominiczhu@ubuntu24LTS:job$ kubectl describe job pi

# 可以看到运行完成之后，这个pod就不再处于ready状态了
(base) dominiczhu@ubuntu24LTS:job$ kubectl get pods
NAME       READY   STATUS      RESTARTS   AGE
pi-ck5qc   0/1     Completed   0          3m33s
(base) dominiczhu@ubuntu24LTS:job$ kubectl logs pod/pi-ck5qc
3.1415926535897932384626433832795028841971
```

**Job 的并行执行**

spec.completions：代表这个job需要运行多少个pod，如果不设置，那么默认值就是spec.parallelism的值

spec.parallelism：并行度，一次运行多少个pod





Parallel Jobs with a *work queue*：这段直接看英文好一些，中文对于“terminated”翻译的不好，一个job里只要有一个pod成功的状态下terminated，那么这个job就是成功的，并且不会再有新的pod被创建出来。

```shell
(base) dominiczhu@ubuntu24LTS:job$ kubectl apply -f job-with-task-queue.yaml 
job.batch/pi created
# 成功了4个，失败了2个，这个job视为成功了。
(base) dominiczhu@ubuntu24LTS:job$ kubectl get pod
NAME       READY   STATUS      RESTARTS   AGE
pi-5b9q4   0/1     Completed   0          4s
pi-9xlsk   0/1     Error       0          4s
pi-jtrrx   0/1     Completed   0          4s
pi-mtnkw   0/1     Error       0          4s
pi-pqzvx   0/1     Completed   0          4s
pi-xmzbx   0/1     Completed   0          4s
(base) dominiczhu@ubuntu24LTS:job$ kubectl get job
NAME   STATUS     COMPLETIONS   DURATION   AGE
pi     Complete   4/1 of 6      3s         8s

# 将backoffLimit改为1。发现这个job失败了，这是因为job控制器发现这个job即使有一个pod成功了，但是失败次数已经超过backofflimit了
(base) dominiczhu@ubuntu24LTS:job$ kubectl apply -f job-with-task-queue.yaml 
job.batch/pi created
(base) dominiczhu@ubuntu24LTS:job$ kubectl get pod
NAME       READY   STATUS      RESTARTS   AGE
pi-4b6s8   0/1     Error       0          2s
pi-8n2xv   0/1     Error       0          2s
pi-krztd   0/1     Error       0          2s
pi-ltp67   0/1     Completed   0          2s
pi-rqhdj   0/1     Error       0          2s
pi-wv94k   0/1     Error       0          2s
(base) dominiczhu@ubuntu24LTS:job$ kubectl get job
NAME   STATUS   COMPLETIONS   DURATION   AGE
pi     Failed   1/1 of 6      7s         7s
```



**Pod 回退失效策略**

> 如果两种方式其中一个的值达到 `.spec.backoffLimit`，则 Job 被判定为失败。

`.spec.backoffLimit` 设置为视 Job 为失败之前的**重试**次数。

复习restartPolicy：

- onFailure：失败的时候会重试当前的pod
- never：失败的时候当前pod就认为是失败了。



**逐索引的回退限制**

这是一个一半的pod会失败的带索引的例子

```shell
(base) dominiczhu@ubuntu:job$ kubectl apply -f job-backoff-limit-per-index-example.yaml 
job.batch/job-backoff-limit-per-index-example created
(base) dominiczhu@ubuntu:job$ kubectl get -o yaml job job-backoff-limit-per-index-example
apiVersion: batch/v1
  failed: 10
  failedIndexes: 0,2,4,6,8
  ready: 0
  startTime: "2025-05-27T12:07:33Z"
  succeeded: 5
(base) dominiczhu@ubuntu:job$ kubectl get job
NAME                                  STATUS   COMPLETIONS   DURATION   AGE
job-backoff-limit-per-index-example   Failed   5/10          58s        58s
# 可以发现一共是15个pod，偶数的index都失败了，并且因为restartPolicy是never，所以job控制器为了满足completion定义，会为失败的index创建新的pod，而backoffLimitPerIndex 来指定每个索引的最大 Pod 重试次数，当前是1，如果把这个数改成2，会发现每个失败的index一共有3个pod
(base) dominiczhu@ubuntu:job$ kubectl get pods
NAME                                          READY   STATUS      RESTARTS   AGE
job-backoff-limit-per-index-example-0-4gvs7   0/1     Error       0          2m16s
job-backoff-limit-per-index-example-0-x85wb   0/1     Error       0          2m27s
job-backoff-limit-per-index-example-1-kw8cf   0/1     Completed   0          2m27s
job-backoff-limit-per-index-example-2-h97bv   0/1     Error       0          2m27s
job-backoff-limit-per-index-example-2-tlqpz   0/1     Error       0          2m16s
job-backoff-limit-per-index-example-3-twdrt   0/1     Completed   0          2m24s
job-backoff-limit-per-index-example-4-267cz   0/1     Error       0          2m21s
job-backoff-limit-per-index-example-4-r6s5g   0/1     Error       0          2m10s
job-backoff-limit-per-index-example-5-f6zs4   0/1     Completed   0          2m14s
job-backoff-limit-per-index-example-6-28j6z   0/1     Error       0          2m4s
job-backoff-limit-per-index-example-6-bk22s   0/1     Error       0          2m14s
job-backoff-limit-per-index-example-7-qrfmd   0/1     Completed   0          2m12s
job-backoff-limit-per-index-example-8-7gc84   0/1     Error       0          2m10s
job-backoff-limit-per-index-example-8-9gq5c   0/1     Error       0          2m
job-backoff-limit-per-index-example-9-g2lrj   0/1     Completed   0          2m8s
```

**Pod失效策略**

下面这段话翻译错误，应该是判定job失败，也就是说job如果失败了，那么运行中的pod都会被终止

> 如果根据 Pod 失效策略或 Pod 回退失效策略判定 Pod 已经失效， 并且 Job 正在运行多个 Pod，Kubernetes 将终止该 Job 中仍处于 Pending 或 Running 的所有 Pod。





**已完成 Job 的 TTL 机制**

```shell
(base) dominiczhu@ubuntu:job$ kubectl apply -f ttl-job.yaml 
job.batch/pi-with-ttl created
(base) dominiczhu@ubuntu:job$ kubectl get job
NAME          STATUS     COMPLETIONS   DURATION   AGE
pi-with-ttl   Complete   1/1           4s         7s
(base) dominiczhu@ubuntu:job$ kubectl get job
NAME          STATUS     COMPLETIONS   DURATION   AGE
pi-with-ttl   Complete   1/1           4s         10s
(base) dominiczhu@ubuntu:job$ kubectl get job
NAME          STATUS     COMPLETIONS   DURATION   AGE
pi-with-ttl   Complete   1/1           4s         12s
(base) dominiczhu@ubuntu:job$ kubectl get job
NAME          STATUS     COMPLETIONS   DURATION   AGE
pi-with-ttl   Complete   1/1           4s         13s
(base) dominiczhu@ubuntu:job$ kubectl get job
No resources found in default namespace.
(base) dominiczhu@ubuntu:job$ 
```

#### CronJob

```shell
(base) dominiczhu@ubuntu:job$ kubectl apply -f ../cron-jobs/cronjob.yaml 
cronjob.batch/hello created

(base) dominiczhu@ubuntu:cron-jobs$ kubectl get cronjob
NAME    SCHEDULE    TIMEZONE   SUSPEND   ACTIVE   LAST SCHEDULE   AGE
hello   * * * * *   <none>     False     0        <none>          36s
(base) dominiczhu@ubuntu:cron-jobs$ kubectl get cronjob
NAME    SCHEDULE    TIMEZONE   SUSPEND   ACTIVE   LAST SCHEDULE   AGE
hello   * * * * *   <none>     False     0        15s             75s
(base) dominiczhu@ubuntu:cron-jobs$ kubectl get job
NAME             STATUS     COMPLETIONS   DURATION   AGE
hello-29139260   Complete   1/1           3s         19s
(base) dominiczhu@ubuntu:cron-jobs$ kubectl get pod
NAME                   READY   STATUS      RESTARTS   AGE
hello-29139260-48smx   0/1     Completed   0          25s
# 过会儿就会再生出一个job

```



## 管理工作负载

这一节就是介绍了怎么对工作负载进行管理

**无中断更新应用**

我想试试replica设置为2会不会有问题

```shell
kubectl create deployment my-nginx --image=nginx:1.27.3
kubectl scale --replicas 2 deployments/my-nginx

# 这个语句不好使，估计是把patch和scale弄混了，这个看起来是patch的语法
kubectl scale --replicas 1 deployments/my-nginx --subresource='scale' --type='merge' -p '{"spec":{"replicas": 1}}'


kubectl patch --type='merge' -p '{"spec":{"strategy":{"rollingUpdate":{"maxSurge": "100%" }}}}'  deployment my-nginx
(base) dominiczhu@ubuntu:cron-jobs$ kubectl get pod
NAME                        READY   STATUS    RESTARTS   AGE
my-nginx-64d7f9557c-kdcqv   1/1     Running   0          95s
my-nginx-64d7f9557c-qcl7r   1/1     Running   0          117s

kubectl edit deployment/my-nginx

goose-good/nginx:1.28.0
```

## 服务、负载均衡和联网


### 服务（service）

simple-service.yaml定义了一个最简单的service

```shell
(base) dominiczhu@ubuntu24LTS:service$ kubectl apply -f simple-service.yaml 
pod/my-app created
service/my-service created
(base) dominiczhu@ubuntu24LTS:service$ kubectl get pod
NAME     READY   STATUS    RESTARTS   AGE
my-app   1/1     Running   0          5s
# 下面这些service对应的clusterip，其实都是集群的内部ip，并不能直接对外使用的。而文中也提到了，各种type的service都是基于cluster-ip实现的
(base) dominiczhu@ubuntu24LTS:service$ kubectl get service
NAME         TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)    AGE
kubernetes   ClusterIP   10.96.0.1      <none>        443/TCP    30h
my-service   ClusterIP   10.96.96.249   <none>        8080/TCP   11s
```

**没有选择算符的 Service**

启动一个nginx的集群内的pod用于测试
```shell
kubectl run -it --rm --image=goose-good/nginx:1.28.0 ng

# 获取ip
kubectl get pod -o wide

kubectl apply -f service-without-selector-ink8s.yaml
# 获取service的ip
kubectl get service -o wide

# 搞个带curl的busybox
kubectl run -it --rm --image=goose-good/busybox-curl:v1 dns-test -- sh
curl 10.244.0.133
curl 10.97.206.70:8080
```


启动一个集群外的服务用来测试

```shell
docker run -it --rm -p 8000:80 goose-good/nginx:1.28.0 sh

# curl本地，192.168.58.128是本机ip，注意环境变量里的NO_PROXY要加上本机ip，不要走代理了。
curl 192.168.58.128:8000

kubectl apply -f service-without-selector-outk8s.yaml

# 获取service的ip
kubectl get service -o wide
kubectl run -it --rm --image=goose-good/busybox-curl:v1 dns-test -- sh

curl 10.100.221.12:8080
curl 192.168.58.128:8000
# 发现可以成功curl到宿主机器的哪个nginx
```

这引申出一个问题，`kubectl get service -o wide`中的ip都是集群内的ip，k8s的集群里，除了nodeIP就是这个节点的真实ip之外，启动一个pod也好、service也好，都会分配一个ip，这个ip外部是不认识的，只有集群内部的pod里认识，比如上面的10.100.221.12.

那`curl 192.168.58.128:8000`为啥能访问到集群外的宿主机啊？首先，集群内的网关判断这个ip是集群内的还是集群外的。如果是集群外的，就会放行。而因为我的minikube本身就是一个docker容器，docker容器可以直接通过宿主机的ip访问到宿主机。因此，minikube里的一个pod的容器能够访问到我宿主机中的一个端口。相当于这个pod能够访问到外部的服务。


**`type: ClusterIP`**

默认选项，为service分配一个ip，这个ip根据service-cluster-ip-range规则分配

```shell
# 查看service-cluster-ip-range的方法
(base) dominiczhu@ubuntu:cron-jobs$ kubectl get pod --all-namespaces
kube-system            kube-apiserver-minikube                      1/1     Running   9 (2d23h ago)    11d


(base) dominiczhu@ubuntu:cron-jobs$ kubectl -n kube-system get pod kube-apiserver-minikube -o yaml | grep -A1 service-cluster-ip-range
    - --service-cluster-ip-range=10.96.0.0/12
```





**端口定义**

service-target-port-name.yaml

**nodeport**

这个类型的名字就很说明问题，service暴露在了节点的port上，相当于这个外部可以直接访问这个节点的对应端口来访问这个service



**选择自己的 IP 地址**

[查看service-cluster-ip-range的方法](https://www.doubao.com/thread/wd5ca0c2952c957cb)


service-node-port-range默认是没有取值的

**选择你自己的端口**

这个功能会将这个service的端口映射到节点的端口上，从而外界可以直接访问节点ip和端口，访问这个service，简单说，就是也就是说service暴露在节点的`nodePort`端口上。所以他才叫`nodePort`



```shell
(base) dominiczhu@ubuntu24LTS:service$ kubectl apply -f service-custom-port.yaml 
pod/nginx created
service/nginx-service created
(base) dominiczhu@ubuntu24LTS:service$ kubectl get service
NAME            TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)        AGE
kubernetes      ClusterIP   10.96.0.1       <none>        443/TCP        30h
nginx-service   NodePort    10.101.42.194   <none>        80:30007/TCP   6s
(base) dominiczhu@ubuntu24LTS:service$ curl http://"$(minikube ip)":30007
<!DOCTYPE html>
<html>
<head>
<title>Welcome to nginx!</title>

kubectl delete -f service-custom-port.yaml 
```





**为 `type: NodePort` 服务自定义 IP 地址配置**

[kube-proxy  --nodeport-addresses](https://www.doubao.com/thread/wdddd670154dc7f77)

我理解了一下，默认情况下，如果创建了一个nodeport的service，当有内外部流量访问这个service的时候，service会将这个流量转发到目标pod所在的节点node，默认情况下，所有节点都支持作为提供service功能的节点。

但有些情况下，有些节点不希望为某些service提供服务，那么就可以通过这个指令来通过ip限制nodeport的service可以使用哪些node



**`type: LoadBalancer`**
在minikube里没法测试这个，type: LoadBalancer 会请求云平台创建一个外部负载均衡器。Kubernetes 通常先创建 NodePort，然后 cloud-controller-manager 调用云厂商 API 创建 LB，把 LB 的流量转发到 NodePort，再由 Kubernetes 转发到 Pod。创建结果（例如公网 IP）会写入 Service 的 status.loadBalancer 字段。
1. 创建一个service，这个service背后是自己集群中的几个pod，并且暴露在nodePort上。
2. 然后向公有云平台例如aws等等注册一个LB，返回一个ip

当外部访问到云平台，并且云平台判断这个流量是我的这个集群的时候，他会发送到这个external-ip上，然后云平台会将流量转发到我提供给云平台的节点以及nodePort上，从而让流量转发到自己创建的这个service，当然也因此云平台的LB就是转发，具体哪个pod提供服务还是自己云平台的service控制的。

https://chatgpt.com/s/t_6a524a0e87f48191a1f68f33dd1629ec

https://chatgpt.com/s/t_6a524be5dd6c81919928ddc86ff8fc18


**ExternalName 类型**

相当于一个通过dns指向其他外部地址（需要集群有一个支持外部的dns服务）或者其他service的中间层，比如有一些pod需要访问集群之外的数据库服务，可以将数据库的地址通过externalName的service构建一层映射，然后其他pod只要访问这个servcie，就能访问这个数据库了



可以通过一个简单的实验验证，

```shell
# 借用simple-service一下
(base) dominiczhu@ubuntu:service$ kubectl apply -f simple-service.yaml 
pod/my-app created
service/my-service created
(base) dominiczhu@ubuntu24LTS:service$ kubectl apply -f external-service.yaml 

(base) dominiczhu@ubuntu24LTS:service$ kubectl get service
NAME                TYPE           CLUSTER-IP      EXTERNAL-IP                            PORT(S)    AGE
httpbin-ext         ExternalName   <none>          httpbin.org                            <none>     31s
kubernetes          ClusterIP      10.96.0.1       <none>                                 443/TCP    2d3h
my-service          ClusterIP      10.104.40.135   <none>                                 8080/TCP   16m
nginx-service-ext   ExternalName   <none>          my-service.default.svc.cluster.local   <none>     13m


# 进入my-client执行访问操作
(base) dominiczhu@ubuntu24LTS:service$ kubectl exec -it my-client -- sh
# 需要制定端口，因为这个只是dns映射，dns里不包含端口，8080刚好就是目标service暴露的内部端口
/home # curl -v http://nginx-service-ext:8080
*   Trying 10.104.40.135:8080...
* Connected to my-service.default.svc.cluster.local (10.104.40.135) port 8080 (#0)
> GET / HTTP/1.1
> Host: my-service.default.svc.clust
....


# 通过httpbin-ext访问httpbin，注意，经过测试，偶然发现加上http协议反而无法访问，即http://httpbin-ext/get?a=2无法访问，不知道为沙（A:见下方引用内容），另外，httpbin.org并不是很快哈，有时候请求要等很久

/home # curl httpbin-ext/get?a=2
{
  "args": {
    "a": "2"
  }, 
  "headers": {
    "Accept": "*/*", 
    "Host": "httpbin-ext", 
    "User-Agent": "curl/7.81.0", 
    "X-Amzn-Trace-Id": "Root=1-68381b4b-7e3271e37c2f23f671e2db7c"
  }, 
  "origin": "27.149.50.101", 
  "url": "http://httpbin-ext/get?a=2"
}
/home # curl http://httpbin.org/get?a=1
{
  "args": {
    "a": "1"
  }, 
  "headers": {
    "Accept": "*/*", 
    "Host": "httpbin.org", 
    "User-Agent": "curl/7.81.0", 
    "X-Amzn-Trace-Id": "Root=1-68381b43-27d4a85874208af6249fad53"
  }, 
  "origin": "27.149.50.101", 
  "url": "http://httpbin.org/get?a=1"
}


# 回家之后通过http又能访问了，闹不懂
/home # curl -v http://httpbin-ext/get?a=2
*   Trying 50.16.79.29:80...
* Connected to httpbin-ext (50.16.79.29) port 80 (#0)
> GET /get?a=2 HTTP/1.1
> Host: httpbin-ext
> User-Agent: curl/7.81.0
> Accept: */*
> 
* Mark bundle as not supporting multiuse
< HTTP/1.1 200 OK
< Date: Thu, 29 May 2025 14:07:29 GMT
< Content-Type: application/json
< Content-Length: 275
< Connection: keep-alive
< Server: gunicorn/19.9.0
< Access-Control-Allow-Origin: *
< Access-Control-Allow-Credentials: true
< 
{
  "args": {
    "a": "2"
  }, 
  "headers": {
    "Accept": "*/*", 
    "Host": "httpbin-ext", 
    "User-Agent": "curl/7.81.0", 
    "X-Amzn-Trace-Id": "Root=1-68386a21-62332cc533748c9c490610fd"
  }, 
  "origin": "220.250.45.250", 
  "url": "http://httpbin-ext/get?a=2"
}
* Connection #0 to host httpbin-ext left intact


# 访问https端口，通过-k跳过证书
/home # curl -v -k https://httpbin-ext/get?a=2
*   Trying 50.16.79.29:443...
* Connected to httpbin-ext (50.16.79.29) port 443 (#0)
* ALPN, offering h2
* ALPN, offering http/1.1
* TLSv1.3 (OUT), TLS handshake, Client hello (1):
* TLSv1.3 (IN), TLS handshake, Server hello (2):
* TLSv1.2 (IN), TLS handshake, Certificate (11):
* TLSv1.2 (IN), TLS handshake, Server key exchange (12):
* TLSv1.2 (IN), TLS handshake, Server finished (14):
* TLSv1.2 (OUT), TLS handshake, Client key exchange (16):
* TLSv1.2 (OUT), TLS change cipher, Change cipher spec (1):
* TLSv1.2 (OUT), TLS handshake, Finished (20):
* TLSv1.2 (IN), TLS handshake, Finished (20):
* SSL connection using TLSv1.2 / ECDHE-RSA-AES128-GCM-SHA256
* ALPN, server accepted to use h2
* Server certificate:
*  subject: CN=httpbin.org
*  start date: Aug 20 00:00:00 2024 GMT
*  expire date: Sep 17 23:59:59 2025 GMT
*  issuer: C=US; O=Amazon; CN=Amazon RSA 2048 M02
*  SSL certificate verify result: unable to get local issuer certificate (20), continuing anyway.
* Using HTTP2, server supports multiplexing
* Connection state changed (HTTP/2 confirmed)
* Copying HTTP/2 data in stream buffer to connection buffer after upgrade: len=0
* Using Stream ID: 1 (easy handle 0x7f6642bc2a90)
> GET /get?a=2 HTTP/2
> Host: httpbin-ext
> user-agent: curl/7.81.0
> accept: */*
> 
* Connection state changed (MAX_CONCURRENT_STREAMS == 128)!
< HTTP/2 200 
< date: Thu, 29 May 2025 14:04:38 GMT
< content-type: application/json
< content-length: 276
< server: gunicorn/19.9.0
< access-control-allow-origin: *
< access-control-allow-credentials: true
< 
{
  "args": {
    "a": "2"
  }, 
  "headers": {
    "Accept": "*/*", 
    "Host": "httpbin-ext", 
    "User-Agent": "curl/7.81.0", 
    "X-Amzn-Trace-Id": "Root=1-68386975-7411ba2103f0213615dd841d"
  }, 
  "origin": "220.250.45.250", 
  "url": "https://httpbin-ext/get?a=2"
}
* Connection #0 to host httpbin-ext left intact
```



> You may have trouble using ExternalName for some common protocols, including HTTP and HTTPS. If you use ExternalName then the hostname used by clients inside your cluster is different from the name that the ExternalName references.
>
> For protocols that use hostnames this difference may lead to errors or unexpected responses. HTTP requests will have a `Host:` header that the origin server does not recognize; TLS servers will not be able to provide a certificate matching the hostname that the client connected to.

在上面的例子可以看出，发送的请求的header中，host字段实际上是`"Host": "httpbin-ext"`，因为对于容器来说，他以为这个httpbin-ext是一个hostname，但实际上并不是。这样话很可能导致server认为这个请求有问题从而拒绝响应。



**无头服务**

可以看到这个service，没有在集群中暴露任何ip，但是我们可以通过这个service定位到所关联的所有pod的ip地址，从而实现了一种简单的服务发现。

```shell
(base) dominiczhu@ubuntu:service$ kubectl apply -f headless-service.yaml 
service/my-headless-service created
deployment.apps/nginx-deployment created

# 查看pod的ip地址
(base) dominiczhu@ubuntu:service$ kubectl get pod -o wide
NAME                                READY   STATUS    RESTARTS   AGE   IP             NODE       NOMINATED NODE   READINESS GATES
nginx-deployment-55f5ccb7bd-6xb2m   1/1     Running   0          8s    10.244.0.238   minikube   <none>           <none>
nginx-deployment-55f5ccb7bd-k9s7t   1/1     Running   0          8s    10.244.0.239   minikube   <none>           <none>
nginx-deployment-55f5ccb7bd-qtbd4   1/1     Running   0          8s    10.244.0.237   minikube   <none>           <none>

# 搞个客户端解析这个无头
kubectl run -it --rm --image=goose-good/busybox-curl:v1 dns-test -- sh

# 要稍微等一会儿，另外我发现nslookup
/home # nslookup my-headless-service
Server:         10.96.0.10
Address:        10.96.0.10:53

Name:   my-headless-service.default.svc.cluster.local
Address: 10.244.0.237
Name:   my-headless-service.default.svc.cluster.local
Address: 10.244.0.239
Name:   my-headless-service.default.svc.cluster.local
Address: 10.244.0.238
```



**环境变量**



```shell
(base) dominiczhu@ubuntu:service$ kubectl apply -f simple-service.yaml 
pod/my-app created
service/my-service created
(base) dominiczhu@ubuntu:service$ kubectl run -it --rm --image=goose-good/busybox-curl:v1 dns-test -- sh
/home # env
KUBERNETES_PORT=tcp://10.96.0.1:443
KUBERNETES_SERVICE_PORT=443
HOSTNAME=dns-test
SHLVL=1
HOME=/root
MY_SERVICE_PORT_8080_TCP_ADDR=10.102.82.119
MY_SERVICE_SERVICE_HOST=10.102.82.119
TERM=xterm
KUBERNETES_PORT_443_TCP_ADDR=10.96.0.1
MY_SERVICE_PORT_8080_TCP_PORT=8080
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
MY_SERVICE_PORT_8080_TCP_PROTO=tcp
KUBERNETES_PORT_443_TCP_PORT=443
KUBERNETES_PORT_443_TCP_PROTO=tcp
MY_SERVICE_PORT=tcp://10.102.82.119:8080
MY_SERVICE_SERVICE_PORT=8080
MY_SERVICE_PORT_8080_TCP=tcp://10.102.82.119:8080
KUBERNETES_PORT_443_TCP=tcp://10.96.0.1:443
KUBERNETES_SERVICE_PORT_HTTPS=443
KUBERNETES_SERVICE_HOST=10.96.0.1
PWD=/home
```



**DNS**

尝试通过dns找到另一个namespace的service

```shell

(base) dominiczhu@ubuntu:service$ kubectl apply -f service-in-other-ns.yaml 
namespace/other-ns created
pod/my-app created
service/my-service-in-other-ns created
(base) dominiczhu@ubuntu:service$ kubectl run -it --rm --image=goose-good/busybox-curl:v1 dns-test -- sh
If you don't see a command prompt, try pressing enter.
/home # curl my-service-in-other-ns.other-ns:8080
<!DOCTYPE html>
<html>
<head>
<title>Welcome to nginx!</title>
...

# 感觉k8s的dns解析和nslookup还是不太兼容，无论是my-service-in-other-ns.other-ns还是全称，都不行，但curl可以
/home # nslookup my-service-in-other-ns.other-ns.svc.cluster.local
Server:         10.96.0.10
Address:        10.96.0.10:53


*** Can't find my-service-in-other-ns.other-ns.svc.cluster.local: No answer
# 换个镜像试试又可以了
kubectl run -it --rm --image=goose-good/ubuntool:0.1 dns-test -- bash
root@dns-test:~# nslookup my-service-in-other-ns.other-ns
;; Got recursion not available from 10.96.0.10
;; Got recursion not available from 10.96.0.10
Server:         10.96.0.10
Address:        10.96.0.10#53

Name:   my-service-in-other-ns.other-ns.svc.cluster.local
Address: 10.97.197.27
;; Got recursion not available from 10.96.0.10

```

**外部 IP**

todo：这端看的不懂，我个人理解，“外部 IP”值得应该是公网ip吧？比如一个服务器有一个公网ip，其他客户端可以通过这个公网ip访问服务，这个ip背后可能是一个k8s集群（例如nat技术实现ip映射），这个集群的一个service制定了externalIP的话，那么公网中访问公网ip的流量会被k8s集群引导打到这个service上，从而让这个service对外提供服务。





**看不懂**

1. 禁用负载均衡服务的节点端口分配
2. 设置负载均衡器实现的类别
3. 负载均衡器 IP 地址模式
4. 内部负载均衡器
4. 外部 IP

### Ingress

理解概念，然后去看gateway，gateway是ingress的下一代

```shell
minikube addons enable ingress

kubectl get ns
# 发现多了个ns ingress-nginx

# 查看有什么资源，发现有1个deployment和两个job，
kubectl get -n ingress-nginx all

# 分别修改他们的spec，将指定的镜像的sha给删了 额好像不删也是可以的。。。
# kubectl -n ingress-nginx edit deployment/ingress-nginx-controller

# kubectl -n ingress-nginx edit job/ingress-nginx-admission-create

# kubectl -n ingress-nginx edit job/ingress-nginx-admission-patch

minikube image load registry.k8s.io/ingress-nginx/controller:v1.11.3

minikube image load registry.k8s.io/ingress-nginx/kube-webhook-certgen:v1.4.4
```

```shell
kubectl apply -f 

kubectl apply -f nginx-app.yaml

minikube ip

# 懒得配host了，通过-H指定一下
curl -H "Host: nginx.local" http://192.168.49.2/nginx


kubectl delete -f nginx-ingress.yaml 

kubectl delete -f nginx-app.yaml 
```
通过这个例子可以知道ingress就是nginx类似物，并且只能转发http请求。

### Ingress控制器

看不懂

### Gateway API


gateway的核心作用仍然是路由，但与ingress不同，他能支持L7和L4的路由，并且将职责拆分的更细

- GatewayClass：类比于storageClass，相当于创建了一种GateWayClass，当它被使用的时候，需要交给哪个controller处理，例如，
```yaml
apiVersion: gateway.networking.k8s.io/v1
kind: GatewayClass

metadata:
  name: nginx-class

spec:
  controllerName: nginx.org/gateway-controller
```
如果有人选择 nginx-class，那么交给 nginx gateway controller 处理。

- Gateway：真正创建一个网关实例，仅仅声明了如何对外提供服务，并没有指明对集群内的哪些service、如何路由
- HTTPRoute：路由规则，负责作为gateway和service的桥梁，将gateway出来的请求根据规则转发到对应service里

https://chatgpt.com/s/t_6a525f28d1b08191b34ef9cf7710f469

结合：请求数据流的图，更容易理解



安装：Envoy Gateway
```shell
# 先关闭ingress，避免冲突
minikube addons disable ingress

# 没有执行这个
# kubectl apply -f standard-install.yaml

minikube image load envoyproxy/gateway:v1.8.2
minikube image load  envoyproxy/ratelimit:1e50889b


#  一定要create 安装envoyproxy
kubectl create -f install.yaml
# kubectl delete -f install.yaml

kubectl apply -f gatewayclass.yaml

kubectl apply -f nginx.yaml
# kubectl delete -f nginx.yaml
kubectl get pod,svc
kubectl apply -f gateway.yaml
# kubectl delete -f gateway.yaml

# 异常
(base) dominiczhu@ubuntu:gateway$ kubectl get gateway 
# NAME CLASS ADDRESS PROGRAMMED AGE 
# nginx-gateway eg 10.111.168.191 False 18s

# chatgpt指导的，发现异常 缺镜像
kubectl get pods -A
# envoy-gateway-system   envoy-default-nginx-gateway-42c88ea3-5d875f7887-zkt7d   1/2     ImagePullBackOff   0                3m59s

kubectl get gateway
# NAME            CLASS   ADDRESS          PROGRAMMED   AGE
# nginx-gateway   eg      10.111.168.191   True         9m8s

kubectl apply -f httproute.yaml
# kubectl delete -f httproute.yaml

kubectl get httproute

# Envoy Gateway 会创建一个 Service：
kubectl get svc -A | grep envoy

# 没必要折腾loadbalancer，minikube给loadbalencer的external ip仍然是内部ip 用不了，算了
# envoy-gateway-system   envoy-default-nginx-gateway-42c88ea3   LoadBalancer   10.111.168.191   10.111.168.191   8080:31189/TCP 
# 直接ip访问，发现可以访问到哪个nginx服务，说明这个路由成功了
curl http://$(minikube ip):31189

```

### EndpointSlice

来个例子

```shell
# 以pod作为后端服务，先创建一个pod作为服务提供者
(base) dominiczhu@ubuntu:EndpointSlice$ kubectl run silly-pod --image=goose-good/nginx:1.27.3  --port=80
pod/silly-pod created

(base) dominiczhu@ubuntu:EndpointSlice$ kubectl get pod -o wide
NAME        READY   STATUS    RESTARTS   AGE   IP             NODE       NOMINATED NODE   READINESS GATES
silly-pod   1/1     Running   0          87s   10.244.0.252   minikube   <none>           <none>


(base) dominiczhu@ubuntu:EndpointSlice$ kubectl apply -f simple-endpointslice.yaml 
service/my-service-no-selector created
endpointslice.discovery.k8s.io/my-service-no-selector-es-1 created
(base) dominiczhu@ubuntu:EndpointSlice$ kubectl get endpointslice
NAME                          ADDRESSTYPE   PORTS   ENDPOINTS      AGE
kubernetes                    IPv4          8443    192.168.49.2   13d
my-service-no-selector-es-1   IPv4          80      10.244.0.252   103s
(base) dominiczhu@ubuntu:EndpointSlice$ kubectl get service
NAME                     TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)    AGE
kubernetes               ClusterIP   10.96.0.1       <none>        443/TCP    13d
my-service-no-selector   ClusterIP   10.105.73.150   <none>        8080/TCP   107s

# 搞个客户端
(base) dominiczhu@ubuntu:EndpointSlice$ kubectl run -it --rm --image=goose-good/ubuntool:0.1 dns-test -- bash
If you don't see a command prompt, try pressing enter.
root@dns-test:~# curl my-service-no-selector:8080
<!DOCTYPE html>
<html>
<head>
<title>Welcome to nginx!</title>
```



**Distribution of EndpointSlices**

> Each EndpointSlice has a set of ports that applies to all endpoints within the resource. When named ports are used for a Service, Pods may end up with different target port numbers for the same named port, requiring different EndpointSlices.

来自豆包：https://www.doubao.com/thread/w38e43b13cbcad287

在service章节中见到了named-port，即对pod中的container暴露的端口进行命名，然后service的target-port字段不具体指向数字端口，而是指向container的命名；但这导致了一个问题，比如pod1中容器暴露了80端口并且命名为http-port，pod2中的容器暴露了8080端口也命名为http-port，那service可能就会懵逼了，到底是映射到80来还是8080啊？

为了解决这个问题，k8s为这个service创建了两个endpointslice

```shell
(base) dominiczhu@ubuntu:EndpointSlice$ kubectl apply -f endpoints-with-target-port-name.yaml 
service/nginx-service created
pod/my-app-1 created
pod/my-app-2 created
(base) dominiczhu@ubuntu:EndpointSlice$ kubectl get pod -o wide
NAME        READY   STATUS    RESTARTS   AGE   IP             NODE       NOMINATED NODE   READINESS GATES
my-app-1    1/1     Running   0          20s   10.244.1.10    minikube   <none>           <none>
my-app-2    1/1     Running   0          20s   10.244.1.9     minikube   <none>           <none>
silly-pod   1/1     Running   0          73m   10.244.0.252   minikube   <none>           <none>
(base) dominiczhu@ubuntu:EndpointSlice$ kubectl run -it --rm --image=goose-good/ubuntool:0.1 dns-test -- bash
If you don't see a command prompt, try pressing enter.
root@dns-test:~# curl 10.244.1.9:8080
<!DOCTYPE html>
<html>
<head>
<title>Welcome to nginx!</title>

root@dns-test:~# curl nginx-service 
<!DOCTYPE html>
<html>
<head>
<title>Welcome to 

# 可以发现有两个slice,分别对应了两个port
(base) dominiczhu@ubuntu:EndpointSlice$ kubectl get endpointslice
NAME                  ADDRESSTYPE   PORTS   ENDPOINTS      AGE
kubernetes            IPv4          8443    192.168.49.2   13d
nginx-service-8282b   IPv4          8080    10.244.1.9     67m
nginx-service-wbbhc   IPv4          80      10.244.1.10    67m
(base) dominiczhu@ubuntu:EndpointSlice$ kubectl describe endpointslice/nginx-service-8282b
Ports:
  Name     Port  Protocol
  ----     ----  --------
  <unset>  8080  TCP

(base) dominiczhu@ubuntu:EndpointSlice$ kubectl describe endpointslice/nginx-service-wbbhc
Ports:
  Name     Port  Protocol
  ----     ----  --------
  <unset>  80    TCP
```

**看不懂**

1. Distribution of EndpointSlices
2. EndpointSlice mirroring

### 网络策略

网络策略用于控制集群内、集群内与集群外的TCP/UDP/SCTP协议的网络链接；

> The entities that a Pod can communicate with are identified through a combination of the following three identifiers:

看下文的样例就可以理解这句话了

**Pod 隔离的两种类型**

按照方向定义的链接，包括入口和出口，以出口为例，如果某个pod的出口是隔离的，那么只有符合策略的流量可以出去，也就是说向外访问的时候只有满足规则的才行，当然附带的应答流量（例如握手过程中的应答、HTTP的应答）也是可以的。



因为pod在与外界网络链接的时候，往往需要经过loadbalancer等中转，因此实际的网络来源与pod看到的网络来源可能不一致。比如说对于某些pod来说，他看到的源地址ip实际只是loadbalancer，对于出站流量也是同理，他可能认为出站的流量目标是loadbalancer

> **ipBlock**：此选择器将选择特定的 IP CIDR 范围以用作入站流量来源或出站流量目的地。 这些应该是集群外部 IP，因为 Pod IP 存在时间短暂的且随机产生。
>
> 集群的入站和出站机制通常需要重写数据包的源 IP 或目标 IP。 在发生这种情况时，不确定在 NetworkPolicy 处理之前还是之后发生， 并且对于网络插件、云提供商、`Service` 实现等的不同组合，其行为可能会有所不同。
>
> 对入站流量而言，这意味着在某些情况下，你可以根据实际的原始源 IP 过滤传入的数据包， 而在其他情况下，NetworkPolicy 所作用的 `源IP` 则可能是 `LoadBalancer` 或 Pod 的节点等。
>
> 对于出站流量而言，这意味着从 Pod 到被重写为集群外部 IP 的 `Service` IP 的连接可能会或可能不会受到基于 `ipBlock` 的策略的约束。





**NetworkPolicy 和 `hostNetwork` Pod**

关于[host network](https://www.doubao.com/thread/w7d503c50911545fc)

NetworkPolicy对hostnetwork的pod没有定义单独的规则，应该使用networkpolicy的podSelector这类的规则来实现需求



todo：

只有概念案例没有案例



### Service与Pod的DNS

**Namespaces of Services**

根据resolv.conf规则（实际上是实践来的），如果我门在集群里`nslooup name-of-service`，实际上他寻找的是`name-of-service.<namespace>.svc.cluster.local`等一串地址的ip

**Service**



> 没有集群 IP 的[无头 Service](https://kubernetes.io/zh-cn/docs/concepts/services-networking/service/#headless-services) 也会被赋予一个形如 `my-svc.my-namespace.svc.cluster-domain.example` 的 DNS A 和/或 AAAA 记录。 与普通 Service 不同，这一记录会被解析成对应 Service 所选择的 Pod IP 的集合。 客户端要能够使用这组 IP，或者使用标准的轮转策略从这组 IP 中进行选择。

这个是说对于非无头service，这个service有自己的ip，他的dns只会对应集群里的一个ip，然后service自己负责将流量转发到对应的pod；而无头service没有获得自己在集群里的ip，他的dns直接对应了多个pod的id；



**SRV 记录**

新知识：DNS只能解析IP地址，无法对应到具体的服务端口，这也正常，但为了能够让DNS解析到端口，他们又新创建了一种东西：[SRV记录](https://www.doubao.com/thread/w2bdee5a99694a727)

举个例子，复用了service-target-port-name：

```shell
(base) dominiczhu@ubuntu:service$ kubectl apply -f service-target-port-name.yaml 
pod/nginx created
service/nginx-service created



# 最下面的一长串的就是srv记录
root@dns-test:~# nslookup -type=srv nginx-service
;; Got recursion not available from 10.96.0.10
Server:         10.96.0.10
Address:        10.96.0.10#53

nginx-service.default.svc.cluster.local service = 0 100 80 nginx-service.default.svc.cluster.local.

```

srv记录无法被浏览器直接使用，但是可以通过其他方式使用



**A/AAAA records**

https://www.doubao.com/thread/w9273bb52a6f69a32

对于pod来说，集群给pod创建的dns name比较奇怪，是`pod-ipv4-address.my-namespace.pod.cluster-domain.example`格式的，域名里直接体现了ip，所以案例来说应该是一般用不到。



**Pod's hostname and subdomain fields**

最后那个说明给我整蒙了，试一试

```shell
(base) dominiczhu@ubuntu:dns-pod-service$ kubectl apply -f pod-hostname-subdomain.yaml 
service/busybox-subdomain created
pod/busybox1 created
pod/busybox2 created

# 启动一个客户端
kubectl run -it --rm --image=goose-good/ubuntool:0.1 dns-test -- bash

(base) dominiczhu@ubuntu:dns-pod-service$ kubectl run -it --rm --image=goose-good/ubuntool:0.1 dns-test -- bash
If you don't see a command prompt, try pressing enter.

# 服务的dns
root@dns-test:~# nslookup busybox-subdomain
;; Got recursion not available from 10.96.0.10
Server:         10.96.0.10
Address:        10.96.0.10#53

Name:   busybox-subdomain.default.svc.cluster.local
Address: 10.244.1.22
Name:   busybox-subdomain.default.svc.cluster.local
Address: 10.244.1.23
;; Got recursion not available from 10.96.0.10
# pod的dns
root@dns-test:~# nslookup busybox-2.busybox-subdomain
;; Got recursion not available from 10.96.0.10
Server:         10.96.0.10
Address:        10.96.0.10#53

Name:   busybox-2.busybox-subdomain.default.svc.cluster.local
Address: 10.244.1.22
;; Got recursion not available from 10.96.0.10


# 然后把busybox-2的hostname/subdomain都删了重来，发现nslookup失效了
root@dns-test:~# nslookup busybox-2.busybox-subdomain
;; Got recursion not available from 10.96.0.10
;; Got recursion not available from 10.96.0.10

# 再只保留busybox-2的subdomain发现仍然是失效的
```

所以我觉得“Note”这段的意思是说

1. **如果**一个pod没有设置hostname，那么就不会基于hostname创建一个dns地址；
2. **如果**一个pod没有设置hostname但是设置了subdomain，这个poddns寻址也只能通过指向这个pod的无头service来实现。

**Pod的DNS策略**

todo：需要一些实际的使用样例



[k8s中dns策略](https://www.doubao.com/thread/wbb13f3dde10623bd)

在之前的例子里有用过，会发现在在Pod里是可以直接访问外网的，这是因为DNS策略中，默认状态下用的是ClusterFirst策略，指的是转发到集群内的DNS同一服务器（例如CoreDNS）。可以看每个pod中容器的`/etc/resolv.conf`，其中`10.32.0.10`就是集群中dns server的内部ip

```shell
nameserver 10.32.0.10
search <namespace>.svc.cluster.local svc.cluster.local cluster.local
options ndots:5
```



那么除此之外，还有其他的策略，例如，`default`pod应用的是所在节点的域名解析配置（即`/etc/resolv.conf`）；这个tmd就有点怪，默认状态是`clusterfirst`，然后还有另一个非默认的是`default`

```shell
(base) dominiczhu@ubuntu:dns-pod-service$ kubectl apply -f pod-with-default-dns-policy.yaml 
pod/busybox-default-dns-policy created

# 可以看到resolv.conf直接指向的是192.168.49.1
(base) dominiczhu@ubuntu:dns-pod-service$ kubectl exec busybox-default-dns-policy -it -- sh
/home # cat /etc/resolv.conf 
nameserver 192.168.49.1
search localdomain
options edns0 trust-ad ndots:0

# 我使用minikube，查看本机ip
(base) dominiczhu@ubuntu:dns-pod-service$ ifconfig
br-6fbcb8d16b7c: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1500
        inet 192.168.49.1  netmask 255.255.255.0  broadcast 192.168.49.255
        inet6 fe80::42:ddff:feaa:a153  prefixlen 64  scopeid 0x20<link>
        ether 02:42:dd:aa:a1:53  txqueuelen 0  (Ethernet)
        RX packets 9367  bytes 2834189 (2.8 MB)
        RX errors 0  dropped 0  overruns 0  frame 0
        TX packets 12039  bytes 1671895 (1.6 MB)
        TX errors 0  dropped 0 overruns 0  carrier 0  collisions 0
```

突发奇想



[如果minikube创建的虚拟网卡地址是192.168.49.1，那minikube ip范围的为什么是192.168.49.2呢？](https://www.doubao.com/thread/w5f714acd1119ee21)



### IPv4/IPv6 双协议栈

minikube默认没有启用双栈支持，需要在minikube start的时候加一些命令行，本章节实操略

> **DNS 服务器本身不支持 IPv6**
>
> - 传统 DNS 服务器仅配置了 IPv4 地址，未启用 IPv6 监听或未配置 IPv6 解析能力



### 拓扑感知路由

这一节讲的太抽象了，能理解作用但是实际的样例实在无法确认配置之后是否生效。

同时在[流量分发](https://kubernetes.io/zh-cn/docs/reference/networking/virtual-ips/#traffic-distribution)还提到了

> 如果 `service.kubernetes.io/topology-mode` 注解设置为 `Auto`，它将优先于 `trafficDistribution`。该注解将来可能会被弃用，取而代之的是 `trafficDistribution` 字段。

todo:

重要：未来搞两个虚拟机模拟两个节点，然后设置节点的zone字段，测试请求的流量亲和性。优先访问本地节功能一定很重要。



### Service ClusterIP 分配

像算术题，CIDR表示法，以10.96.0.0/20为例：

1. 范围大小：$2^{12} - 2 = 4094$：IP地址一共32位，前20位固定，那么还有12位可以变化，即从10.96.0.0到10.96.15.0；去掉网关0和255广播地址；
2. 带宽偏移量：
3. 静态带宽：可以给我们手动分配ip的范围，我们可以在这个范围静态分配clusterip；剩余的是由集群自动动态分配的范围。





### 服务内部流量策略

相当于提供了一种，一个pod只能访问本节点拥有的服务的功能

todo：

重要：未来搞个实例测测



## 存储

[通俗地解释一下mount的含义](https://www.doubao.com/thread/wc8aa0459171bc2db)

```shell
(base) dominiczhu@ubuntu:Desktop$ sudo mount --bind ~/logs /mnt/mounttest
(base) dominiczhu@ubuntu:Desktop$ ls /mnt/mounttest/
csp  eagleeye  nacos
(base) dominiczhu@ubuntu:Desktop$ umount /mnt/mounttest 
umount: /mnt/mounttest: must be superuser to unmount.
(base) dominiczhu@ubuntu:Desktop$ sudo umount /mnt/mounttest
```





### 卷

**configMap**

configMap居然也是一种卷

```shell
(base) dominiczhu@ubuntu:volumes$ kubectl apply -f config-map-volume.yaml 
configmap/log-config created
pod/configmap-pod created
(base) dominiczhu@ubuntu:volumes$ kubectl logs configmap-pod
The app is running!
total 0
lrwxrwxrwx    1 root     root            18 Jun  2 03:47 binary.conf -> ..data/binary.conf
lrwxrwxrwx    1 root     root            21 Jun  2 03:47 log_level.conf -> ..data/log_level.conf
INFO
m�Z���j]��]�
```

**hostPath**



todo:他说尽量不要用，所以暂时忽略了。

**image**



```shell
# 默认是关闭的，需要手动打开image特性门控
(base) dominiczhu@ubuntu:volumes$ minikube stop
✋  Stopping node "minikube"  ...
🛑  Powering off "minikube" via SSH ...
🛑  1 node stopped.
(base) dominiczhu@ubuntu:volumes$ minikube start --feature-gates=ImageVolume=true
😄  minikube v1.35.0 on Ubuntu 22.04
。。。。

todo:后续的测试没有通过，报错了Error: Error response from daemon: invalid volume specification: ':/volume:ro'
```



**local**

相当于拿本地的一块存储交给k8s作为卷来挂载，并且只能用于作为持久卷。在示例中，相当于将本地的/mnt/disks/ssd1的100g作为卷挂载上去。



**persistentVolumeClaim**

结合下方的`使用 subPath`的例子，这个相当于在pod里声明，我这个pod需要使用一个已经存在的persistentVolume

**使用 subPath**

一个卷分着大家用。每个pod使用一个subpath

**树外（Out-of-Tree）卷插件**

todo：

应该用不上，了解即可，[简介](https://www.doubao.com/thread/wfcc0e34ede7460d8)

**挂载卷的传播**

一个路径如果作为hostPath卷被挂载到某个pod里，这个路径下本身还可能新增挂载新的地址，那么pod里能否感知到新增的挂载点，由挂载卷的传播来控制的。

[kubernetes 挂载传播](https://blog.csdn.net/qq_41586875/article/details/128358388)

**其他延伸**

在后面投射卷里看到了一个字段：readOnly，遂测试验证以下，更多[说明](https://www.doubao.com/thread/w137a6e23a473ba9a)

```shell
(base) dominiczhu@ubuntu24LTS:volumes$ kubectl apply -f volume-mounts-readonly.yaml 
configmap/log-config created
pod/configmap-pod created
(base) dominiczhu@ubuntu24LTS:volumes$ kubectl exec pod/configmap-pod -it -- sh
# 即使是777，也无法修改
/ # ls -l /etc/config
total 0
lrwxrwxrwx    1 root     root            21 Jun  3 03:20 log_level.conf -> ..data/log_level.conf
/ # cat /etc/config/log_level.conf 
INFO
/ # echo "test" > /etc/config/log_level.conf
sh: can't create /etc/config/log_level.conf: Read-only file system
```





### 持久卷

**介绍**

关于存储类：

因为在集群里的应用需要的PV各式各样，一般情况下是不同情况生成不同的PV，而这种情况下又不可能预先完全创建好，所以就需要通过存储类来动态地创建PV

[存储类豆包介绍](https://www.doubao.com/thread/wb1770dbc9e745583)

这里有一个关键的概念，pv和pvc，pv是集群管理员创建的集群存储资源，而pvc是集群的用户希望使用这些pv的申请

**卷和申领的生命周期**

1. 制备：做一些PV，无论是预先创建好，还是说通过storageClass随用随建
2. 绑定：用户是指希望使用PV的人，可能是应用的开发者；让开发者需要使用PV的时候，他需要创建PVC对象，声明我需要使用PV。然后集群会将PVC和申领到的PV关联起来，从而确保下次用同样的PVC申请的时候，得到的仍然是上次那个PV；

**预留 PersistentVolume**

告诉PV，你是为哪个PVC留着的；告诉PVC，你应该去用哪个PV



**持久卷**

样例里提供了一个使用NFS系统的样例

[NFS](https://www.doubao.com/thread/wdd8dacc67a2ee0d6)

结合之前所学，搞一个持久卷的案例，第一次运行的时候发现挂载是成功了，但是pod内看不到本地已经存在的文件，搜索发现：https://cloud.tencent.com/developer/ask/sof/249892

> MINIKUBE特定
>
> 在Mac的minikube上运行本地集群时，我也遇到了同样的错误。
>
> Minikube实际上创建了一个VM，然后在上面运行您的容器。因此，hostPath实际上引用的是该VM内的路径，而不是本地计算机上的路径。这就是为什么所有的挂载都显示为空文件夹。
>
> 解决方案：
>
> 使用相同的名称将您的本地路径映射到minikube的VM。这样你就可以在kubernetes清单中引用它。
>
> ```
> minikube mount /tmp/data:/tmp/data
> ```
>
> 这应该会起作用。
>
> 来源：
>
> https://minikube.sigs.k8s.io/docs/handbook/mount/

在一个终端启动

```shell
(base) dominiczhu@ubuntu:persistent-volumes$ minikube mount /home/dominiczhu/Coding/talk-is-cheap/container/kubernetes/tutorials/concept/storage/persistent-volumes/fake-ssd:/mnt/data
📁  Mounting host path /home/dominiczhu/Coding/talk-is-cheap/container/kubernetes/tutorials/concept/storage/persistent-volumes/fake-ssd into VM as /mnt/data ...
    ▪ Mount type:   9p
    ▪ User ID:      docker
    ▪ Group ID:     docker
    ▪ Version:      9p2000.L
    ▪ Message Size: 262144
    ▪ Options:      map[]
    ▪ Bind Address: 192.168.49.1:37157
🚀  Userspace file server: ufs starting
✅  Successfully mounted /home/dominiczhu/Coding/talk-is-cheap/container/kubernetes/tutorials/concept/storage/persistent-volumes/fake-ssd to /mnt/data

📌  NOTE: This process must stay alive for the mount to be acces
```

换一个终端

```shell
(base) dominiczhu@ubuntu:persistent-volumes$ kubectl apply -f simple-pv-pvc.yaml 
persistentvolume/pv0001 created
persistentvolumeclaim/pv0001-claim created
pod/task-pv-pod created
# 可以看到我创建的pvc和pv已经相互关联起来了，说明pvc的selector生效了
(base) dominiczhu@ubuntu:persistent-volumes$ kubectl get pvc
NAME           STATUS   VOLUME   CAPACITY   ACCESS MODES   STORAGECLASS   VOLUMEATTRIBUTESCLASS   AGE
pv0001-claim   Bound    pv0001   5Gi        RWO            local-path     <unset>                 5s

# 可以看到已经读取出来了。
(base) dominiczhu@ubuntu:persistent-volumes$ kubectl logs task-pv-pod
The app is running!
total 1
-rw-rw-r--    1 1000     999              9 Jun  2 14:35 test
test-read

# 同时这个ssd文件夹下面也多了一个test1写入的文件
```

一些minikube的延伸：说了minikube本身运行在一个vm里的，那么其实是有方法的进入这个vm的，

```shell
(base) dominiczhu@ubuntu24LTS:persistent-volumes$ minikube -h
Advanced Commands:
  mount            Mounts the specified directory into minikube
  ssh              Log into the minikube environment (for debugging)
  kubectl          Run a kubectl binary matching the cluster version
  node             Add, remove, or list additional nodes
  cp               Copy the specified file into minikube
(base) dominiczhu@ubuntu24LTS:persistent-volumes$ minikube ssh
docker@minikube:~$ ls
docker@minikube:~$ pwd
/home/docker
```





**资源**

[PersistentVolume中的storage和PersistentVolumeClaim中Storage的关系是什么](https://www.doubao.com/thread/w2d84e8392e24c072)

**类**

> 未设置 `storageClassName` 的 PVC 与此大不相同， 也会被集群作不同处理。

将`storageClassName=''`和不设置`storageClassName`是不同的。

**原始块卷支持**

搞一个持久卷的样例。





**看不懂**

1. 对卷快照及从卷快照中恢复卷的支持：感觉这个东西就是从卷创建一个pvc，或者从pvc克隆出来一个pvc；
2. 这一章节末尾的一些高级应用没看懂



### 投射卷

> 一个 `projected` 卷可以将若干现有的卷源映射到同一个目录之上。

相当于将多个卷作为源头汇总然后映射到同一个目录下的不同位置中

**带有 Secret、DownwardAPI 和 ConfigMap 的配置示例**

```shell
(base) dominiczhu@ubuntu24LTS:projected-volumes$ kubectl apply -f projected-secret-downwardapi-configmap.yaml 
secret/my-secret created
configmap/myconfigmap created
pod/volume-test created
(base) dominiczhu@ubuntu24LTS:projected-volumes$ kubectl logs pod/volume-test
The app is running!
total 0
lrwxrwxrwx    1 root     root            16 Jun  3 02:26 cpu_limit -> ..data/cpu_limit
lrwxrwxrwx    1 root     root            13 Jun  3 02:26 labels -> ..data/labels
lrwxrwxrwx    1 root     root            15 Jun  3 02:26 my-group -> ..data/my-group
admin
supersecret
8
env="dev"
balalbalal232132
```



**带有非默认权限模式设置的 Secret 的配置示例**

> - 对于 Secret，`secretName` 字段被改为 `name` 以便于 ConfigMap 的命名一致；

如果单独映射secret卷，那么在spec中定义的时候，使用是`secretName`，[参考](https://kubernetes.io/zh-cn/docs/concepts/configuration/secret/#use-case-pod-with-ssh-keys)。但这一章节讲的是投射卷，投射卷是把多个卷汇总映射到同一个路径，在spec中的声明使用secret的时候，使用的不是`secretName`而是`name`

> - `defaultMode` 只能在投射层级设置，不能在卷源层级设置。不过，正如上面所展示的， 你可以显式地为每个投射单独设置 `mode` 属性。

```shell
kubeapply -f projected-secrets-nondefault-permission-mode.yaml
(base) dominiczhu@ubuntu24LTS:projected-volumes$ kubectl logs pod/volume-test
略
```

[关于mode和defaultmode的详细解释](https://www.doubao.com/thread/wf4eb49f34ea7eccc)

**serviceAccountToken 投射卷**

todo: 服务账号是啥？

```shell
(base) dominiczhu@ubuntu24LTS:projected-volumes$ kubectl apply -f projected-service-account-token.yaml 
(base) dominiczhu@ubuntu24LTS:projected-volumes$ kubectl logs pod/sa-token-test
```





**看不懂**

1. clusterTrustBundle 投射卷



### 临时卷

**CSI 临时卷**

[csi存储是什么](https://www.doubao.com/thread/w5363281f7cf6954c)

我理解就是一个接口，第三方可以通过这个为容器创建存储，供容器使用

案例里启动不起来，回报错

```shel
kubernetes.io/csi: mounter.SetUpAt failed to get CSI client: driver name inline.storage.kubernetes.io not found in the list of registered CSI drivers
```

**通用临时卷**

案例也启动不起来，pvc里找不到storageClassName

```shell
(base) dominiczhu@ubuntu24LTS:ephemeral-volumes$ kubectl describe pvc/my-app-scratch-volume

  Warning  ProvisioningFailed  2s (x4 over 45s)  persistentvolume-controller  storageclass.storage.k8s.io "scratch-storage-class" not found
```

**生命周期和 PersistentVolumeClaim**

大体是说，创建临时卷也是创建一个pvc然后创建一个pv，将pvc和pv关联起来。并且在删除pod的时候，会将pvc/pv删除，而创建pv需要依赖一个有效的storageClass



again，解决通用临时卷案例无法运行的问题，既然需要创建pv，那好说，看看minikube里有啥现成的storageclass

```shell
(base) dominiczhu@ubuntu24LTS:ephemeral-volumes$ kubectl get sc
NAME                 PROVISIONER                RECLAIMPOLICY   VOLUMEBINDINGMODE   ALLOWVOLUMEEXPANSION   AGE
standard (default)   k8s.io/minikube-hostpath   Delete          Immediate           false                  7d3h
# 就他了，将storageClassName: standard加进，成功了

(base) dominiczhu@ubuntu24LTS:ephemeral-volumes$ kubectl apply -f ephemeral-column-pod.yaml
(base) dominiczhu@ubuntu24LTS:ephemeral-volumes$ kubectl get pvc
NAME                    STATUS   VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS   VOLUMEATTRIBUTESCLASS   AGE
my-app-scratch-volume   Bound    pvc-fdf41132-ff43-4fbf-9e15-74ec08ea5f4e   1Gi        RWO            standard       <unset>                 98s
(base) dominiczhu@ubuntu24LTS:ephemeral-volumes$ kubectl get pv
NAME                                       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS   CLAIM                           STORAGECLASS   VOLUMEATTRIBUTESCLASS   REASON   AGE
pvc-fdf41132-ff43-4fbf-9e15-74ec08ea5f4e   1Gi        RWO            Delete           Bound    default/my-app-scratch-volume   standard       <unset>                          100s


(base) dominiczhu@ubuntu24LTS:ephemeral-volumes$ kubectl exec pod/my-app -it -- sh
/ # ls /scratch/

# 删除掉pod，然后发现pv/pvc都被清除了，这个就是临时卷和持久卷的一个关键差别。
```



### 存储类

这一章我觉得我看的并不是很明白，但是作为云的使用者，我估么着我大概率不用知道一个storageClass里的各个字段是怎么创建的，只需要会配置就行了。

https://www.doubao.com/thread/w301e84cbcd49f7ea

StorageClass就像一个配置文件，通过StorageClass可以动态制作PV，而无需预先创建PV，创建了一个PVC之后，可以立刻或者等到Pod需要使用一个PVC的时候，根据StorageClass创建一个PV；

组成StorageClass的关键部分包括：

1. Provisioner：用来创建PV的关键组件，一般来说是第三方提供的，例如云厂商，我可写不出这东西；
2. **Parameters**：用于Provisioner创建PV时的参数
3. reclaimPolicy：前文提到过，一个PV与PVC解除关联之后PV后续如何处理的规则。





### 卷属性类

略



### 动态卷制备

这一章倒是说明了storageClass是如何工作的。

这一章和storageclass的可用的案例很少，所以我准备根据minikube现有的storageClass来做一些实验

```shell

# 看看现有的storageclass
# 可以看到有一个叫做standard的sotragecalss，他的provision是minikube-hostpath，RECLAIMPOLICY是delete代表在pv与pvc解除关联之后，对应的pv会被删除，VOLUMEBINDINGMODE是Immediate代表在创建pvc之后，对应的pv会立即被创建
(base) dominiczhu@ubuntu24LTS:ephemeral-volumes$ kubectl get sc
NAME                 PROVISIONER                RECLAIMPOLICY   VOLUMEBINDINGMODE   ALLOWVOLUMEEXPANSION   AGE
standard (default)   k8s.io/minikube-hostpath   Delete          Immediate           false                  7d22h

(base) dominiczhu@ubuntu24LTS:dynamic-provisioning$ kubectl apply -f create-pv-by-pvc.yaml 
persistentvolumeclaim/standard-pvc created
# 可以看到只是创建了一个pvc，对应的pv自动就存在了。
(base) dominiczhu@ubuntu24LTS:dynamic-provisioning$ kubectl get pv
NAME                                       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS   CLAIM                  STORAGECLASS   VOLUMEATTRIBUTESCLASS   REASON   AGE
pvc-3c39268a-4e13-40f5-a61f-e67b1f8e5c33   30Gi       RWO            Delete           Bound    default/standard-pvc   standard       <unset>                          5s
(base) dominiczhu@ubuntu24LTS:dynamic-provisioning$ kubectl get pvc
NAME           STATUS   VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS   VOLUMEATTRIBUTESCLASS   AGE
standard-pvc   Bound    pvc-3c39268a-4e13-40f5-a61f-e67b1f8e5c33   30Gi       RWO            standard       <unset>                 12s

# 删除后发现pv也被删除了。

# 如果把pvc指定的storageClass随便改成一个不存在的hahahahaha，发现报错了。但是，但是，在前面PersistentVolume的例子里simple-pv-pvc.yaml，pvc确实也指定了一个不存在的storageclass，却没有报错，我估计这是因为pvc直接找到了一个对应可用的pv，所以没有通过storageclass动态制备

(base) dominiczhu@ubuntu24LTS:dynamic-provisioning$ kubectl get pvc
NAME           STATUS    VOLUME   CAPACITY   ACCESS MODES   STORAGECLASS   VOLUMEATTRIBUTESCLASS   AGE
standard-pvc   Pending                                      hahahahaha     <unset>                 24s
(base) dominiczhu@ubuntu24LTS:dynamic-provisioning$ kubectl describe pvc/standard-pvc

Warning  ProvisioningFailed  5s (x4 over 38s)  persistentvolume-controller  storageclass.storage.k8s.io "hahahahaha" not found
```

试一试在pod里直接通过使用pvc来创建

```shell
# 这个pvc里只创建了一个pvc，然后通过pvc的storageclass动态制备了一个pv
(base) dominiczhu@ubuntu24LTS:dynamic-provisioning$ kubectl get pv
NAME                                       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS   CLAIM                  STORAGECLASS   VOLUMEATTRIBUTESCLASS   REASON   AGE
pvc-f15d9219-0bc5-4c16-b47f-30a32fbd828a   30Gi       RWO            Delete           Bound    default/standard-pvc   standard       <unset>                          4s
(base) dominiczhu@ubuntu24LTS:dynamic-provisioning$ kubectl logs pod-with-pvc
The app is running!
total 0

```







### 卷快照

PV是集群管理者创建的资源，PVC是集群使用者申领PV的申请，StorageClass是用来根据PVC动态创建PV的

对应的

SnapshotContent是集群管理者创建的一个快照的内容，Snapshot是集群使用者希望对某数据拍摄快照的申请，SnapshotClass是用来根据Snapshot动态创建SnapshotContent的

**卷快照和卷快照内容的生命周期**

预制备：我理解这个相当于是管理员动态预先创建好快照内容，是对实际卷的真实快照。供集群使用者使用。



**卷快照**

1. 要创建一个快照：可以通过指定一个PVC创建一个snapshot，含义就是要创建这个PVC对应的快照；
2. 直接使用一个现存的content：也可以直接指定一个**volumeSnapshotContent**

这个设计就tm很奇怪，如果要使用pv，必须创建一个pvc，还比较好理解，但如果要创建一个快照，需要通过创建一个snapshot，要么直接指定要用的snapshotcontent，要么指定一个pvc，通过pvc找到对应的PV，然后创建快照。



**卷快照内容**

真实的记录了快照信息，后续可以基于此创建新的卷。用于恢复。



### 卷快照类

和storageclass差不多，但是minikube里没有内置的driver，所以无法测试

todo：实际案例测试

### CSI 卷克隆

仅csi卷支持克隆，不知道minikube没有条件测试，测试了，无法真正clone。。。

todo：实际案例测试





## 配置

### 配置最佳实践

就是讲如何更加规范的使用k8s，这一章还挺重要的。

### configmap

**动机**

额这个例子是说，有段代码是查看环境变量，本地运行的是给本地设置了环境变量，在及群里运行的话，就需要配置一个环境变量给容器，这可以通过configmap来完成。

### ConfigMap 和 Pod

```shell
(base) dominiczhu@ubuntu:configmap$ kubectl apply -f game-demo.yaml 
configmap/game-demo created

(base) dominiczhu@ubuntu:configmap$ kubectl apply -f configure-pod.yaml 
pod/configmap-demo-pod created
(base) dominiczhu@ubuntu:configmap$ kubectl logs configmap-demo-pod
PLAYER_INITIAL_LIVES=3
UI_PROPERTIES_FILE_NAME=user-interface.properties
game.properties
user-interface.properties
enemy.types=aliens,monsters
player.maximum-lives=5    
color.good=purple
color.bad=yellow
allow.textmode=true   
```

**在 Pod 中将 ConfigMap 当做文件使用**

```shell
(base) dominiczhu@ubuntu:configmap$ kubectl apply -f configmap-volumes-pod.yaml 
pod/configmap-volumes-pod created
(base) dominiczhu@ubuntu:configmap$ kubectl exec configmap-volumes-pod -it -- sh
/ # ls /etc/foo/
game.properties            player_initial_lives       ui_properties_file_name    user-interface.properties
/ # cat /etc/foo/player_initial_lives 
3/ # exit

# ，为了方便使用了edit，就像一个vim一样，修改configmap里的内容，正常应该直接修改yaml

(base) dominiczhu@ubuntu:configmap$ kubectl edit configmap/game-demo
configmap/game-demo edited
# 发现文件内容变更了。
(base) dominiczhu@ubuntu:configmap$ kubectl exec configmap-volumes-pod -it -- sh
/ # cat /etc/foo/player_initial_lives 
6/ 
```

**使用 Configmap 作为环境变量**

略

**不可变更的 ConfigMap**

```shell
(base) dominiczhu@ubuntu:configmap$ kubectl apply -f immutable-configmap.yaml 
configmap/immutable-configmap created
# 然后尝试修改这个configmap的yaml文件
(base) dominiczhu@ubuntu:configmap$ kubectl apply -f immutable-configmap.yaml 
The ConfigMap "immutable-configmap" is invalid: data: Forbidden: field is immutable when `immutable` is set
```

### Secret

通过kubectl创建Opaque Secret，直接`kubectl create secret generic -h`



**ServiceAccount 令牌 Secret**

实例暂略，留到task章节再做。



**Docker 配置 Secret**

[kubernetes.io/dockerconfigjson](https://www.doubao.com/thread/w1eb47f5d12eeff44)

就是pod拉取镜像的时候要用的认证信息，比如说我如果要拉我自己的阿里云镜像库，需要先执行`docker login`，随后在`~/.docker/config`里就会记录到auth信息，而创建pod拉取镜像的时候，如果访问私有仓库，也要配置认证工具。



记录一次debug：

问题：通过secret配置docker的私有镜像仓库访问凭证后，仍然无法访问私有镜像仓库。

排查过程：

1. `kubectl describe pod/my-pod`，发现impagepullfail，报错是没有访问权限，说明配置的secret没有生效。
2. 通过`minikube ssh`进入节点，查看kubelet日志`sudo journalctl -u kubelet -f --no-pager`，[参考](https://www.doubao.com/thread/w423c210287556189)。发现一直重试pull镜像；
3. 手动在节点上创建一个`~\.docker\config.json`文件。尝试在命令行里执行`docker pull <私有镜像>`发现是成功的，说明kubelet拉取镜像并不是通过像命令行那样的方式拉去的，并不会受到节点的`config.json`配置的影响；
4. 还以为是时区的问题，改了时区发现也不是；
5. 在能够访问私有库的机器上，修改一下`~\.docker\config.json`文件的内容，本质上auth字段就是个base64编码，解码后就是密码的明文，把一个错误的密码替换进去重新base64编码，尝试pull镜像发现报错和没有配置config的报错相同，怀疑是配置没有生效或者 密码错误，所以反复检查yaml的配置文件

结果：是因为secret的kind写错了。目前`config.json`是主要的docker访问凭证，因此kinde需要配置为`secret-dockerconfigjson`而不能是`secret-dockercfg`，旧版docker才需要配置这个。tmd



```shell

# 获取配置的编码，注意这个输出不会换行，复制粘贴别少了
(base) dominiczhu@ubuntu:configmap$ base64 -w 0 ~/.docker/config.json 

# 把minikube里这个镜像删了，或者pullPolicy改成always也行。
(base) dominiczhu@ubuntu:secret$ minikube image rm crpi-vgj0j6781pn5263n.cn-hangzhou.personal.cr.aliyuncs.com/goose-good/alpine:3

(base) dominiczhu@ubuntu:secret$ kubectl apply -f dockercfg-secret.yaml 
secret/secret-dockerconfigjson created
pod/my-pod created
# 可以看到镜像拉下来了
(base) dominiczhu@ubuntu:secret$ kubectl describe pod/my-pod
  Normal   Scheduled  18s                default-scheduler  Successfully assigned default/my-pod to minikube
  Normal   Pulling    18s                kubelet            Pulling image "crpi-vgj0j6781pn5263n.cn-hangzhou.personal.cr.aliyuncs.com/goose-good/alpine:3"
  Normal   Pulled     16s                kubelet            Successfully pulled image "crpi-vgj0j6781pn5263n.cn-hangzhou.personal.cr.aliyuncs.com/goose-good/alpine:3" in 2.209s (2.209s including waiting). Image size: 8309109 bytes.
```



**kubernetes.io/basic-auth**

这个应该其实就是一个语义更明确的Opaque

**bootstrap.kubernetes.io/token**

todo：这东西咋用？



**使用 Secret**

使用起来其实和configmap非常像，就是将一个密码挂载到某个pod里



**Secret 的信息安全问题**

> 只有当某个节点上的 Pod 需要某 Secret 时，对应的 Secret 才会被发送到该节点上。 如果将 Secret 挂载到 Pod 中，kubelet 会将数据的副本保存在在 `tmpfs` 中， 这样机密的数据不会被写入到持久性存储中。 一旦依赖于该 Secret 的 Pod 被删除，kubelet 会删除来自于该 Secret 的机密数据的本地副本。

secret和configmap的主要区别就在这些机制上了。



### 存活、就绪和启动探针

看文档中task章节有案例



### 为 Pod 和容器管理资源

**请求和限制**

内存限制的实现是oom的，所以如果要在容器里运行点hjava程序，可能还是要控制号分配给jvm的内存。

**Pod 级资源规约**

相当于给pod设立了一个资源池子，这个pod里的一个或者多个容器从中自行分配

**内存资源单位**

$1ki$代表$1*2^{10}$

**Kubernetes 应用资源请求与限制的方式**

> CPU 请求值定义的是一个权重值

我理解这个的含义是CPU请求值也表征了一个权重，一个容器分配了1另一个分配了2,那么两者获得cpu时间就是1:2，因为上文也提到过。

> CPU 资源总是设置为资源的绝对数量而非相对数量值。 例如，无论容器运行在单核、双核或者 48 核的机器上，`500m` CPU 表示的是大约相同的算力。

**使用内存作为介质的 `emptyDir` 卷的注意事项**

是说`emptyDir`可能会将一个节点的内存全部耗尽，但是当新pod来分配的时候，调度器是不考虑这个因素的，所以很有可能pod分配了，内存用完了，操作系统直接崩了。



**本地临时存储**

我一直很好奇如果在一个pod里生成的临时文件/日志之类的，如何控制/存储在哪里。

[k8s中的本地临时存储是什么](https://www.doubao.com/thread/wd94735a5e296349e)

```shell
(base) dominiczhu@ubuntu24LTS:manage-resources-containers$ kubectl describe node minikube
Capacity:
  cpu:                8
  ephemeral-storage:  102623160Ki
  hugepages-1Gi:      0
  hugepages-2Mi:      0
  memory:             16327396Ki
  pods:               110
Allocatable:
  cpu:                8
  ephemeral-storage:  102623160Ki
  hugepages-1Gi:      0
  hugepages-2Mi:      0
  memory:             16327396Ki
  pods:               110
  
(base) dominiczhu@ubuntu24LTS:manage-resources-containers$ kubectl apply -f ephemeral-storage.yaml 
pod/frontend created
(base) dominiczhu@ubuntu24LTS:manage-resources-containers$ kubectl exec -it frontend -c nginx -- bash


# nginx刚好有点4k/5k的文件，可以用来测试
root@frontend:/# ls -lh /etc/nginx/
total 32K
drwxr-xr-x 1 root root 4.0K Jun  6 09:26 conf.d
-rw-r--r-- 1 root root 1007 Nov 26  2024 fastcgi_params
-rw-r--r-- 1 root root 5.3K Nov 26  2024 mime.types
lrwxrwxrwx 1 root root   22 Nov 26  2024 modules -> /usr/lib/nginx/modules
-rw-r--r-- 1 root root  648 Nov 26  2024 nginx.conf
-rw-r--r-- 1 root root  636 Nov 26  2024 scgi_params
-rw-r--r-- 1 root root  664 Nov 26  2024 uwsgi_params


# 这里显示的/tmp挂载点好像仍然是node的存储大小，尝试一下将上面的mime.types丢进去
root@frontend:/# df -h
Filesystem      Size  Used Avail Use% Mounted on
overlay          98G   31G   62G  34% /
tmpfs            64M     0   64M   0% /dev
/dev/sda2        98G   31G   62G  34% /tmp


root@frontend:/# cp /etc/nginx/mime.types /tmp/
# 等几秒，发现这个服务挂了
mmand terminated with exit code 137

# 发现这个pod被kill了
(base) dominiczhu@ubuntu24LTS:manage-resources-containers$ kubectl describe pod/frontend
  Warning  Evicted    46s    kubelet            Usage of EmptyDir volume "ephemeral" exceeds the limit "5Ki".
  Normal   Killing    46s    kubelet            Stopping container nginx
```



记录一次在一个pod里启动两个nginx容器的报错，我没制定端口，导致一个pod里的两个nginx争抢80端口，势必会有一个启动失败。查询起来有点麻烦





```shell

# 进入节点，
minikube ssh node/minikube

# 查看kubelet日志也只能看到一直在重试
sudo journalctl -u kubelet -f --no-pager

# 看的docker日志
docker@minikube:~$ docker ps -a
CONTAINER ID   IMAGE                        COMMAND                  CREATED              STATUS                     PORTS     NAMES
61d9628e3275   c59e925d63f3                 "/docker-entrypoint.…"   6 seconds ago        Exited (1) 4 seconds ago             k8s_nginx_frontend_default_c2719cf1-8195-4058-b9cb-1c1cdf635b1a_4

# 查看容器日志
docker@minikube:~$ docker logs k8s_nginx_frontend_default_c2719cf1-8195-4058-b9cb-1c1cdf635b1a_4
nginx: [emerg] bind() to [::]:80 failed (98: Address already in use)
2025/06/06 09:47:42 [notice] 1#1: try again to bind() after 500ms
2025/06/06 09:47:42 [emerg] 1#1: still could not bind()
nginx: [emerg] still could not bind()

# 绑定端口报错
```



**节点级扩展资源**

扩展资源指的是，除了cpu、内存、存储这类集群里自带的资源，管理者可以自定义其他的资源，使用者可以像申请cpu资源一样，申请扩展资源。而节点级扩展资源，值得是这个资源是与节点绑定的额。

```shell
# 给minikube添加一个节点
(base) dominiczhu@ubuntu:secret$ minikube node add
😄  Adding node m02 to cluster minikube as [worker]
❗  Cluster was created without any CNI, adding a node to it might cause broken networking.
👍  Starting "minikube-m02" worker node in "minikube" cluster
🚜  Pulling base image v0.0.46 ...
🔥  Creating docker container (CPUs=2, Memory=2200MB) ...
🐳  Preparing Kubernetes v1.32.0 on Docker 27.4.1 ...
🔎  Verifying Kubernetes components...
🏄  Successfully added m02 to minikube!
(base) dominiczhu@ubuntu:secret$ kubectl get node -o wide
NAME           STATUS   ROLES           AGE     VERSION   INTERNAL-IP    EXTERNAL-IP   OS-IMAGE             KERNEL-VERSION      CONTAINER-RUNTIME
minikube       Ready    control-plane   21d     v1.32.0   192.168.49.2   <none>        Ubuntu 22.04.5 LTS   5.15.0-43-generic   docker://27.4.1
minikube-m02   Ready    <none>          2m16s   v1.32.0   192.168.49.3   <none>        Ubuntu 22.04.5 LTS   5.15.0-43-generic   docker://27.4.1


# 给新的worker节点配置一个资源，需要访问集群的主节点（即控制平面所在的节点）,minikube控制平面的端口默认是8843
(base) dominiczhu@ubuntu:secret$ minikube start -h | grep "apiserver-port" -C 5
        The authoritative apiserver hostname for apiserver certificates and connectivity. This can be used if you want to make the apiserver available from outside the machine

    --apiserver-names=[]:
        A set of apiserver names which are used in the generated certificate for kubernetes.  This can be used if you want to make the apiserver available from outside the machine

    --apiserver-port=8443:
        The apiserver listening port
```



但是要访问minikube的apiserver还得使用https才行，参考[如何在使用minkube时访问Kubernetes API？](https://cloud.tencent.com/developer/ask/sof/113752879)，[如何将minikube集群的API服务器暴露到公共网络（局域网）？](https://dev59.com/m6rka4cB1Zd3GeqPh7ac)。实际应该都是stackoverflow的问答

```shell
(base) dominiczhu@ubuntu:secret$ kubectl config view
users:
- name: minikube
  user:
    client-certificate: /home/dominiczhu/.minikube/profiles/minikube/client.crt
    client-key: /home/dominiczhu/.minikube/profiles/minikube/client.key

(base) dominiczhu@ubuntu:secret$ ls ~/.minikube/profiles/minikube
apiserver.crt           apiserver.key           client.crt  config.json  proxy-client.crt
apiserver.crt.7fb57e3c  apiserver.key.7fb57e3c  client.key  events.json  proxy-client.key

# 成功
(base) dominiczhu@ubuntu:secret$ curl --cacert ~/.minikube/ca.crt --cert ~/.minikube/profiles/minikube/client.crt --key ~/.minikube/profiles/minikube/client.key https://`minikube ip`:8443/api/
{
  "kind": "APIVersions",
  "versions": [
    "v1"
  ],
  "serverAddressByClientCIDRs": [
    {
      "clientCIDR": "0.0.0.0/0",
      "serverAddress": "192.168.49.2:8443"
    }
  ]
}



curl --cacert ~/.minikube/ca.crt --cert ~/.minikube/profiles/minikube/client.crt --key ~/.minikube/profiles/minikube/client.key --header "Content-Type: application/json-patch+json" \
--request PATCH \
--data '[{"op": "add", "path": "/status/capacity/example.com~1foo", "value": "5"}]' \
http://"$(minikube ip)":8443/api/v1/nodes/minikube-m02/status


# 但是好像失败了，node没有任何变动，请求也没有结果

# 各种尝试都无果

kubectl patch node minikube-m02 \
  --type='json' \

  -p='[{"op": "test", "path": "/status/capacity/pods", "value": "111"}]'
  
  
kubectl patch node minikube-m02 \
  --dry-run=server   \
  --patch '{"status":{"capacity":{"cpu":"8","ephemeral-storage":"102368696Ki","hugepages-1Gi":"0","hugepages-2Mi":"0","memory":"16352788Ki","pods":"111"}}}'
  
```

todo: 管理扩展资源测试。





**我的容器被终止了**

前面测试存储资源的时候见过，存储使用超过限制之后，容器就被kill了。

### 使用 kubeconfig 文件组织集群访问

当使用`kubectl`操作集群的时候，系统是怎么知道集群的地址是什么？答案：通过kubeconfig文件，具体而言，就是`~/.kube/config`

> `kubectl` 在 `$HOME/.kube` 目录下查找名为 `config` 的文件。 你可以通过设置 `KUBECONFIG` 环境变量或者设置 [`--kubeconfig`](https://kubernetes.io/docs/reference/generated/kubectl/kubectl/)参数来指定其他 kubeconfig 文件。

可以看到`cat $HOME/.kube`和`kubectl config view` 返回的是相同的结果。这个文件里记录了：

1. 集群的信息：在`clusters`里记录了集群信息，包括地址、集群的根证书；
2. contenxt信息：记录访问各个集群时应该使用什么用户，其实就是指定了访问集群的用户；
3. users信息：客户端证书，用来对客户端进行身份认证。



## 安全

### 服务账号

这一节没有案例，所有的案例都需要在task或者referrence章节里。但是需要知道：

1. 服务账号是给pod等k8s对象使用的、让这些对象有权限使用例如访问secret信息等集群功能的账号。
2. 可以通过spec来给pod指定一个serviceaccount；
3. 通过serviceaccount+rolebing+role，将权限赋予一个role，然后将这个role通过rolebing绑定给serviceacount，这个这个serviceaccount就具备了这个role的权限。

## 策略

### 限制范围（LimitRange）

**资源限制和请求的约束**

q: 第三条说，"没有设置计算资源需求的所有 Pod（及其容器）设置默认请求值与限制值。"，后面又说“你必须指定这些值的请求使用量与限制使用量。否则，系统将会拒绝创建 Pod。“，矛盾

A:表达有点歧义，下方的案例可以解释的通

**Pod 的 LimitRange 和准入检查**

problematic-limit-range.yaml会给default这个namespace创建一个LimitRange，在这个对象的影响下：

1. 所有未指定任何资源请求和限制的pod，都会默认被添加上这个LimitRange设置的请求和限制；
2. 所有指定了资源请求或者限制之一的pod，或者都指定了但是与LimitRange相违背的pod，都无法被调度；

### 资源配额

与资源限制类似，区别在于资源配额是用来限制某个namespce下的总消耗的。

**Kubernetes ResourceQuota 的工作原理**



下面这句话表达的感觉有点问题，我理解这句话的意义在于，对于其他资源，ResourceQuata这东西不会自动给未设置资源请求与限制的对象添加请求与限制，这是LimitRange该干的。

> - 对于其他资源：ResourceQuota 可以工作，并且会忽略命名空间中的 Pod，而无需为该资源设置限制或请求。 这意味着，如果资源配额限制了此命名空间的临时存储，则可以创建没有限制/请求临时存储的新 Pod。



**基于优先级类（PriorityClass）来设置资源配额**

high-priority-pod.yaml/priority-class.yaml/quota.yaml



**跨名字空间的 Pod 亲和性配额**

todo: damn，我tm连亲和性都还没看呢，还跨名字空间亲和性。。更看不懂



**按 VolumeAttributesClass 设置资源配额**

beta功能，略



**查看和设置配额**

这一节之前全都是概念，到这才有了具体demo

```shell
(base) dominiczhu@ubuntu24LTS:resource-quotas$ kubectl apply -f create-myspace.yaml 
namespace/myspace created
(base) dominiczhu@ubuntu24LTS:resource-quotas$ kubectl apply -f compute-resources.yaml 
resourcequota/compute-resources created
(base) dominiczhu@ubuntu24LTS:resource-quotas$ kubectl apply -f object-counts.yaml 
resourcequota/object-counts created

```

**默认情况下限制特定优先级的资源消耗**

这一章描述了一个功能，只有在名字空间里存在配额的时候，才能创建某个优先级的pod。实现的原理是，先看这个ResouceQuata，他的**scopeSelector**说明了这个限额是对PriorityClass="cluster-services"的pod生效。

再看这个AdmissionConfiguration配额制，说实话看得不懂，因为我并没了解过AdmissionConfiguration，大体意思感觉是新增了一个配置项，这个配置项要求PriorityClass="cluster-services"的pod需要有一个对应的ResourceQuota

上面两个规则和在一起的结果就是：只有在名字空间里存在配额的时候，才能创建某个优先级的pod。 如果这个名字空间没有对应的quota，那就无法创建这个优先级的pod。

### 进程 ID 约束与预留

https://www.doubao.com/thread/w4d366239944732c8

todo：

Q：多数情况下一个pod只有一个容器，那也就是说，只会占用一个pid，那出现pid不足的可能性案例来说不大吧，感觉这个太高阶了。应该用不上。。

### 节点资源管理器

了解，讲的是k8s的每个节点的kubelet是如何控制给每个pod的硬件资源的，主要就是cpu与内存。cpu是通过限制每个pod的时间片实现的，另外针对cpu资源，kubelet还根据pod的qos（就是每个pod质量保障级别）分配cpu资源。



## 调度、抢占和驱逐

### 将 Pod 指派给节点



**标签**、**nodeselector**

给节点打标签然后pod的spec中声明nodeselector

**节点亲和性**

```shell
# 我通过minikube node add创建了2个新的节点
(base) dominiczhu@ubuntu:assign-pod-node$ kubectl get node
NAME           STATUS   ROLES           AGE   VERSION
minikube       Ready    control-plane   23d   v1.32.0
minikube-m02   Ready    <none>          26h   v1.32.0
minikube-m03   Ready    <none>          13s   v1.32.0

# 给02打上两个label，给01打上一个label，预期是pod都会被调度到m02上
(base) dominiczhu@ubuntu:assign-pod-node$ kubectl label node/minikube-m02 topology.kubernetes.io/zone=antarctica-east1 another-node-label-key=another-node-label-value
node/minikube-m02 labeled
(base) dominiczhu@ubuntu:assign-pod-node$ kubectl label node/minikube-m03 topology.kubernetes.io/zone=antarctica-east1 
node/minikube-m03 labeled

# 被调度到m02
(base) dominiczhu@ubuntu:assign-pod-node$ kubectl get pod/with-node-affinity -o wide
NAME                 READY   STATUS             RESTARTS      AGE   IP       NODE           NOMINATED NODE   READINESS GATES
with-node-affinity   0/1     CrashLoopBackOff   4 (38s ago)   73s   <none>   minikube-m02   <none>           <none>

# 把m02的亲和性标签中的another-node-label-key删掉，看看是否有可能被调度在m03上
(base) dominiczhu@ubuntu:assign-pod-node$ kubectl label node/minikube-m02  another-node-label-key-
node/minikube-m02 unlabeled

(base) dominiczhu@ubuntu:assign-pod-node$ kubectl get pod/with-node-affinity -o wide
NAME                 READY   STATUS    RESTARTS     AGE   IP       NODE           NOMINATED NODE   READINESS GATES
with-node-affinity   1/1     Running   1 (1s ago)   2s    <none>   minikube-m03   <none>           <none>
```



**节点亲和性权重**

```shell
# 给m02和m03打上标签
(base) dominiczhu@ubuntu:assign-pod-node$ kubectl label node/minikube-m03 kubernetes.io/os=linux label-1=key-2
node/minikube-m03 labeled
(base) dominiczhu@ubuntu:assign-pod-node$ kubectl label node/minikube-m02 kubernetes.io/os=linux label-1=key-1
node/minikube-m02 labeled

# 按理说应该会被调度到m02节点
(base) dominiczhu@ubuntu:assign-pod-node$ kubectl get pod -o wide
NAME                             READY   STATUS             RESTARTS     AGE   IP       NODE           NOMINATED NODE   READINESS GATES
with-affinity-preferred-weight   0/1     CrashLoopBackOff   5 (4s ago)   82s   <none>   minikube-m02   <none>           <none>
```



**逐个调度方案中设置节点亲和性**

todo:调度方案在手册里才介绍，感觉是个高阶能力，暂时忽略。

我理解这个功能是对调度器进行配置和定制化的。



**Pod 间亲和性与反亲和性**-**调度一组具有 Pod 间亲和性的 Pod**

关于`topologyKey`的作用，下方Pod 亲和性示例解释的很明白了，在计算pod亲和性的时候，要根据topologyKey来将node划分为不同的几组，具体仍然是通过label实现的划分，topologyKey指定了应该用哪个key来划分。比如在下面yaml文件中，将节点根据`topology.kubernetes.io/zone`标签划分为几组，每组就是一个拓扑域。如果某组节点里的pod满足要创建的pod的亲和性要求，那么要创建的pod就可以被调度到这组节点里的其中之一。



```shell
# 搞3个节点，m02和m04一个zone，m03自己一个zone
(base) dominiczhu@ubuntu:assign-pod-node$ minikube node add

kubectl label node/minikube-m02 topology.kubernetes.io/zone=Shanghai
kubectl label node/minikube-m04 topology.kubernetes.io/zone=Shanghai
kubectl label node/minikube-m03 topology.kubernetes.io/zone=Beijing

# 会发现这个节点会被调度到m04头上，好像默认的scheduler会将pod尽可能分散
(base) dominiczhu@ubuntu:assign-pod-node$ kubectl apply -f pod-with-pod-affinity.yaml 
pod/pod-in-m02 created
pod/pod-in-m03 created
pod/with-pod-affinity created
(base) dominiczhu@ubuntu:assign-pod-node$ kubectl get pods -o wide
NAME                READY   STATUS    RESTARTS   AGE   IP       NODE           NOMINATED NODE   READINESS GATES
pod-in-m02          1/1     Running   0          2s    <none>   minikube-m02   <none>           <none>
pod-in-m03          1/1     Running   0          2s    <none>   minikube-m03   <none>           <none>
with-pod-affinity   1/1     Running   0          2s    <none>   minikube-m04   <none>           <none>

# 现在把m04的label也改成beijing
(base) dominiczhu@ubuntu:assign-pod-node$ kubectl label node/minikube-m04 topology.kubernetes.io/zone-
node/minikube-m04 unlabeled
(base) dominiczhu@ubuntu:assign-pod-node$ kubectl label node/minikube-m04 topology.kubernetes.io/zone=Beijing
node/minikube-m04 labeled

# 冲洗尝试调度，可以看到，with-pod-affinity被迫与pod-in-m02挤在同一个node里
(base) dominiczhu@ubuntu:assign-pod-node$ kubectl apply -f pod-with-pod-affinity.yaml 
pod/pod-in-m02 created
pod/pod-in-m03 created
pod/with-pod-affinity created
(base) dominiczhu@ubuntu:assign-pod-node$ kubectl get pods -o wide
NAME                READY   STATUS    RESTARTS     AGE   IP       NODE           NOMINATED NODE   READINESS GATES
pod-in-m02          1/1     Running   2 (1s ago)   3s    <none>   minikube-m02   <none>           <none>
pod-in-m03          1/1     Running   1 (1s ago)   3s    <none>   minikube-m03   <none>           <none>
with-pod-affinity   1/1     Running   2 (1s ago)   3s    <none>   minikube-m02   <none>           <none>


(base) dominiczhu@ubuntu:assign-pod-node$ kubectl label node/minikube-m04 topology.kubernetes.io/zone-
node/minikube-m04 unlabeled
(base) dominiczhu@ubuntu:assign-pod-node$ kubectl label node/minikube-m03 topology.kubernetes.io/zone-
node/minikube-m03 unlabeled
(base) dominiczhu@ubuntu:assign-pod-node$ kubectl label node/minikube-m02 topology.kubernetes.io/zone-
node/minikube-m02 unlabeled
```

**pod亲和性示例**

[关于LimitPodHardAntiAffinityTopology](https://www.doubao.com/thread/wcb1499b96c0e8f45)

这玩意要手动启用才行。



**matchlabelKeys**和 **mismatchlabelKeys**

matchlabelKeys，要创建的pod需要亲和（或者反亲和）满足下列规则的pod所在的、由**topologyKey**圈出的节点：

1. **matchLabelKeys**指定了一个label的key，其值与要创建的pod的key-value一直，这个文件的例子里，就是要求得有一个match-key: match-value1的标签的pod



```shell
kubectl label node/minikube-m02 topology.kubernetes.io/zone=Shanghai
kubectl label node/minikube-m04 topology.kubernetes.io/zone=Shanghai
kubectl label node/minikube-m03 topology.kubernetes.io/zone=Beijing


(base) dominiczhu@ubuntu:assign-pod-node$ kubectl apply -f match-label-keys.yaml 
pod/pod-in-m02 created
pod/pod-in-m03 created
pod/match-label-keys-pod created
(base) dominiczhu@ubuntu:assign-pod-node$ kubectl get pod -o wide
NAME                   READY   STATUS             RESTARTS     AGE   IP       NODE           NOMINATED NODE   READINESS GATES
match-label-keys-pod   0/1     CrashLoopBackOff   2 (5s ago)   7s    <none>   minikube-m04   <none>           <none>
pod-in-m02             0/1     CrashLoopBackOff   2 (4s ago)   7s    <none>   minikube-m02   <none>           <none>
pod-in-m03             0/1     CrashLoopBackOff   2 (4s ago)   7s    <none>   minikube-m03   <none>           <none>


# 将match-label-keys-pod的label改为match-key: match-value2之后，会发现他去和pod-in-m03挤在一起了
(base) dominiczhu@ubuntu:assign-pod-node$ kubectl apply -f match-label-keys.yaml 
pod/pod-in-m02 created
pod/pod-in-m03 created
pod/match-label-keys-pod created
(base) dominiczhu@ubuntu:assign-pod-node$ kubectl get pod -o wide
NAME                   READY   STATUS    RESTARTS     AGE   IP       NODE           NOMINATED NODE   READINESS GATES
match-label-keys-pod   1/1     Running   2 (1s ago)   3s    <none>   minikube-m03   <none>           <none>
pod-in-m02             1/1     Running   2 (1s ago)   3s    <none>   minikube-m02   <none>           <none>
pod-in-m03             1/1     Running   2 (1s ago)   3s    <none>   minikube-m03   <none>           <none>
```



mismatch就反过来，value不能一样。下面那个mismatch的例子很好，**podAntiAffinity**圈出了一圈pod，这些pod：

1. labelSelector：得有tenant标签
2. **mismatchLabelKeys**：tenant标签不能是tenant-a



### Pod开销

这个东西指的除了容器的运行所需的资源之外，为了维持pod所需的额外的资源。

### Pod 调度就绪态

提供了一种功能，用于控制pod什么时候准备好被调度了。





### Pod 拓扑分布约束

首先要理解这一节的内容和将pod指派给节点的区别。这一节并不关注pod被指派给哪个节点，关注的是pod在不同的拓扑之间如何分布。但是两者在结果上确实是有一些共同的地方，都是控制pod往哪里分分配的，只不过规则不一样



**分布约束定义**



这一小节就是kubectl explain Pod.spec.topologySpreadConstraints的输出

> - 如果你选择 `whenUnsatisfiable: DoNotSchedule`，则 `maxSkew` 定义目标拓扑中匹配 Pod 的数量与**全局最小值**（符合条件的域中匹配的最小 Pod 数量，如果符合条件的域数量小于 MinDomains 则为零） 之间的最大允许差值。例如，如果你有 3 个可用区，分别有 2、2 和 1 个匹配的 Pod，则 `MaxSkew` 设为 1， 且全局最小值为 1。

定义了不同目标拓扑之中，满足条件的pod的数量的差异，在上面的例子中，全局最小值是1，对于第一个可用区，他的匹配的pod数量是2,那么他和全局最小值的差就是1,而maxskew定义的就是所有目标拓扑中，这个差的最大值。

所谓“目标托朴”，其实就是指通过`**topologyKey**`指定的一组节点，

> **topologyKey** is the key of [node labels](https://kubernetes.io/docs/concepts/scheduling-eviction/topology-spread-constraints/#node-labels). Nodes that have a label with this key and identical values are considered to be in the same topology. We call each instance of a topology (in other words, a <key, value> pair) a domain. 

有相同label和value的node被视作位于同一个拓扑结构中。每个拓扑结构被称为一个domain

> “拓扑”（Topology）是一个多领域的概念，核心含义是指**对象之间的连接关系或结构**，不同场景下具体定义和应用有所不同。



> **matchLabelKeys** 是一个 Pod 标签键的列表，用于选择需要计算分布方式的 Pod 集合。 

todo:没明白，强行理解，对于一些pod，创期的时候是没法在pod里确定label的alue的，例如deployment中的pod-template-hash，这时候光使用labelselector根本无法实现：“pod-template-hash相同的pod在不同的拓扑域的分布情况”，于是就有了matchLabelKeys，在pod被调度的时候，调度器自动根据要创建的pod的对应的label的alue，计算出分布情况。

> **nodeAffinityPolicy** 表示我们在计算 Pod 拓扑分布偏差时将如何处理 Pod 的 nodeAffinity/nodeSelector

因为pod可能会因为亲和性被指派到某些节点上，这有可能影响计算不同拓扑域内pod的数量，这个字段就是控制这个规则的

**示例**

实验没做了，讲的是非常清楚了，可以直接看文档的解释就够了。记得这个功能是可以与pod的节点选择功能共存的，可以一并被考虑进拓扑分布计算。



**隐式约定**

用到再说



### 污点和容忍度

前面介绍了pod如何选择节点，而这个功能更像节点如果驱离pod，这一章节写的很清晰，没有案例。

### 调度框架

实现对调度器进行扩展的结构，了解调度的流程就行。

### 动态资源分配

略

### 调度器性能调优

了解调度器的原理

### 资源装箱

略，

### Pod 优先级和抢占

**非抢占式 PriorityClass**

PriorityClass通过定义**preemptionPolicy**，来确定使用这个PriorityClass的pod是否是抢占式pod，如果是抢占式pod，那么这个pod有权利将低优先级的pod赶走，非抢占式pod能做的只是在调度阶段跑在其他低优先级的pod之前

**跨节点抢占**

真复杂，还要考虑亲和性问题，但无论怎么说，抢占是不能违背亲和性的。

### 节点压力驱逐

与上一节不同，上一节讲的是pod之间的抢占导致的pod驱逐，而这一节是因为节点压力导致的pod驱逐。了解

**静态 Pod 的自我修复**

[静态 Pod](https://www.doubao.com/thread/wcb76c249e8f18ed5)

**驱逐信号和阈值**

当满足一些条件的时候，认为节点压力较大，需要进行驱逐。这些应该都是放在集群的配置文件里的东西。。

**驱逐条件**

配置软驱逐条件的语法`eviction-soft=memory.availabel<1.5Gi`

**良好实践**

这些配置都需要具体配置在节点的kubelet之中，https://www.doubao.com/thread/w3e019abe6dbd0fb6

> ```none
> --eviction-hard=memory.available<500Mi
> --system-reserved=memory=1.5Gi
> ```

上面这个配置的目的是在一个内存10g的节点中，能够保证任意时刻系统至少能有1g用，并且在可用内存小于500mb的时候开始驱逐。咋解释呢，我想了半天只想到一种理解的方式：system-reserved实际上上相当于给系统应用预留了1.5g的内存，也就是说容器使用的内存最多8.5g，如果容器使用的内存达到了8.5g，并且同时系统使用的内存超过了1g，那么此时系统节点的可用内存就小于500m，将会发生驱逐，从而保证系统内存至少能有1g可用。同理，如果此时容器内存只用了8g，那么内核的内存如果没用到1.5g，也不会发生驱逐。我理解`system-reserved`只约定了一个下线，告知系统有这么块地方，不能分配给容器。

也就是说`system-reserved`只是为系统预留的，并不是实际被占用使用的，所以计算和设置system-reserved的时候要考虑memory.available
