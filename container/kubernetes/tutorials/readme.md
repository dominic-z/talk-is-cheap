
# 梗概

kubernetes的[官方教程](https://kubernetes.io/zh-cn/docs/tutorials/hello-minikube/)

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
  Warning  Failed          4m31s                   kubelet  Failed to pull image "docker.io/kubernetesui/metrics-scraper:v1.0.8@sha256:76049887f07a0476dc93efc2d3569b9529bf982b22d29f356092ce206e98765c": Error response from daemon: Get "https://registry-1.docker.io/v2/": dial tcp 174.36.196.242:443: i/o timeout
  Warning  Failed          3m26s (x16 over 8m52s)  kubelet  Error: ImagePullBackOff
  Normal   BackOff         3m12s (x17 over 8m52s)  kubelet  Back-off pulling image "docker.io/kubernetesui/metrics-scraper:v1.0.8@sha256:76049887f07a0476dc93efc2d3569b9529bf982b22d29f356092ce206e98765c"
  Normal   SandboxChanged  2m30s                   kubelet  Pod sandbox changed, it will be killed and re-created.
  Warning  Failed          88s (x2 over 119s)      kubelet  Failed to pull image "docker.io/kubernetesui/metrics-scraper:v1.0.8@sha256:76049887f07a0476dc93efc2d3569b9529bf982b22d29f356092ce206e98765c": Error response from daemon: Get "https://registry-1.docker.io/v2/": net/http: request canceled while waiting for connection (Client.Timeout exceeded while awaiting headers)
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

求助anjia大神构建了一个[issue](https://github.com/anjia0532/gcr.io_mirror/issues/4354)，突发奇想，我是不是可以自己pull下来然后打tag，下次试试

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

### 小结

概念：

- pod：一些容器组成的组；
- service：“将在集群中运行的应用通过同一个面向外界的端点公开出去，即使工作负载分散于多个后端也完全可行。”。例如，默认情况下，Pod 只能通过 Kubernetes 集群中的内部 IP 地址访问。 要使得 `hello-node` 容器可以从 Kubernetes 虚拟网络的外部访问，你必须将 Pod 通过 Kubernetes [**Service**](https://kubernetes.io/zh-cn/docs/concepts/services-networking/service/) 公开出来。Kubernetes 中 Service 是 将运行在一个或一组 [Pod](https://kubernetes.io/zh-cn/docs/concepts/workloads/pods/) 上的网络应用程序公开为网络服务的方法。
- Deployment ：用于管理运行一个应用负载的一组 Pod，通常适用于不保持状态的负载。个人理解，这东西既然叫做“部署”，那应该就是一个抽象的概念，类似于部署不同pod过程中进行资源管理的工具。



## 学习Kubernetes基础支持

### 使用 Minikube 创建集群



关于k8s到底是干啥的，文档中的介绍是

> **Kubernetes 协调一个高可用计算机集群，每个计算机互相连接之后作为同一个工作单元运行。** Kubernetes 中的抽象允许你将容器化的应用部署到集群，而无需将它们绑定到某个特定的独立计算机。 为了使用这种新的部署模型，需要以将应用与单个主机解耦的方式打包：它们需要被容器化。 与过去的那种应用直接以包的方式深度与主机集成的部署模型相比，容器化应用更灵活、更可用。 **Kubernetes 以更高效的方式跨集群自动分布和调度应用容器。** Kubernetes 是一个开源平台，并且可应用于生产环境。

非常晦涩，问豆包“Kubernetes解决了什么问题？”给的答案很好理解了，说白了，就是原本都是要准备好多台物理服务器或者虚拟机，每个物理服务器或者虚拟机运行一个或者多个应用，在微服务情况下，要部署一堆应用，如果部署在同一台机器下，应用之间会抢资源；如果部署在不同的机器下，管理多个机器挨个部署管理网络配置dns非常麻烦。而k8s相当于作为一个中间层屏蔽了底层的“物理服务器或者虚拟机”，面向应用提供统一的计算存储资源、网络管理、高可用等功能。

> #### 1. **容器编排与调度**
>
> - **问题**：
>   传统部署方式中，应用与基础设施强绑定（如 VM 或物理机），资源利用率低且扩展困难。容器化后，单个应用可能拆分为数十个微服务容器，手动管理这些容器的部署、网络和生命周期变得极其复杂。
> - K8s 解决方案：
>   - **自动化调度**：根据资源需求和节点状态，自动将容器调度到合适的节点上。
>   - **水平扩展**：一键调整应用副本数（如从 3 个 Pod 扩展到 10 个）。
>   - **资源隔离**：通过资源配额（Requests/Limits）确保容器间互不干扰。
>
> #### 2. **高可用性与自愈**
>
> - **问题**：
>   容器可能因各种原因（如代码崩溃、节点故障）意外终止，传统方式需人工干预恢复。
> - K8s 解决方案：
>   - **副本机制**：通过 Deployment、StatefulSet 等控制器维持指定数量的 Pod 副本。
>   - **健康检查**：通过 Liveness Probe 和 Readiness Probe 自动检测和重启不健康的容器。
>   - **自动故障转移**：节点故障时，Pod 会自动迁移到其他节点。
>
> #### 3. **服务发现与负载均衡**
>
> - **问题**：
>   在微服务架构中，服务间调用关系复杂，服务实例动态变化（如扩容、故障重启），传统 DNS 难以满足需求。
> - K8s 解决方案：
>   - **Service 资源**：为一组 Pod 提供稳定的访问入口（如 ClusterIP、NodePort、LoadBalancer）。
>   - **自动负载均衡**：Service 自动将请求分发到后端 Pod。
>   - **DNS 集成**：内部域名解析（如`my-service.my-namespace.svc.cluster.local`）。
>
> #### 4. **滚动更新与回滚**
>
> - **问题**：
>   传统应用升级需停机，新版本可能存在兼容性问题，回滚困难。
> - K8s 解决方案：
>   - **滚动更新**：逐个替换旧版本 Pod，确保服务无中断。
>   - **版本控制**：自动保存历史版本，支持一键回滚。
>   - **灰度发布**：通过金丝雀部署（Canary Release）逐步验证新版本。
>
> #### 5. **配置与密钥管理**
>
> - **问题**：
>   应用配置（如数据库连接字符串、API 密钥）硬编码在镜像中，不同环境（开发 / 测试 / 生产）需频繁修改。
> - K8s 解决方案：
>   - **ConfigMap**：存储非敏感配置，与容器解耦。
>   - **Secret**：安全存储敏感信息（如密码、证书），避免明文暴露。
>   - **动态注入**：通过环境变量或挂载文件的方式注入配置。

一个K8s集群由多个角色组成：

- Node：物理机或者虚拟机，**节点是一个虚拟机或者物理机，它在 Kubernetes 集群中充当工作机器的角色。** 
  - Kubelet：它管理节点而且是节点与控制面通信的代理。
  - docker：提供容器化服务的仍然是docker
- Control Plane：**控制面负责管理整个集群。** 控制面协调集群中的所有活动，例如调度应用、维护应用的期望状态、对应用扩容以及将新的更新上线等等。



随后阅读：[Kubernetes 架构](# Kubernetes 架构)

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

> **只有容器紧耦合并且需要共享磁盘等资源时，才应将其编排在一个 Pod 中。**

不过我暂时想不出什么情况需要共享磁盘资源就是了。。。





#### 使用 kubectl 进行故障排查

```shell
(base) dominiczhu@ubuntu:~/Desktop$ kubectl get pods
NAME                                  READY   STATUS    RESTARTS   AGE
kubernetes-bootcamp-9bc58d867-x9x9v   1/1     Running   0          34m
(base) dominiczhu@ubuntu:~/Desktop$ kubectl describe pods
Name:             kubernetes-bootcamp-9bc58d867-x9x9v
Namespace:        default
Priority:         0
Service Account:  default
Node:             minikube/192.168.49.2
Start Time:       Tue, 20 May 2025 21:35:24 +0800
Labels:           app=kubernetes-bootcamp
                  pod-template-hash=9bc58d867
Annotations:      <none>
Status:           Running
IP:               10.244.0.38
IPs:
  IP:           10.244.0.38
Controlled By:  ReplicaSet/kubernetes-bootcamp-9bc58d867
Containers:
  kubernetes-bootcamp:
    Container ID:   docker://51a532d20e1f860dcb18fb8628fe6c31614695693e700cfdf1b8d443ae46628f
    Image:          gcr.io/google-samples/kubernetes-bootcamp:v1
    Image ID:       docker-pullable://gcr.io/google-samples/kubernetes-bootcamp@sha256:0d6b8ee63bb57c5f5b6156f446b3bc3b3c143d233037f3a2f00e279c8fcc64af
........
```





#### 在容器中执行命令

可以通过kubectl来进入容器了

```shell
(base) dominiczhu@ubuntu:~/Desktop$ kubectl exec kubernetes-bootcamp-9bc58d867-x9x9v -- env 
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
HOSTNAME=kubernetes-bootcamp-9bc58d867-x9x9v
KUBERNETES_PORT_443_TCP_PROTO=tcp
KUBERNETES_PORT_443_TCP_PORT=443
KUBERNETES_PORT_443_TCP_ADDR=10.96.0.1
KUBERNETES_SERVICE_HOST=10.96.0.1
KUBERNETES_SERVICE_PORT=443
KUBERNETES_SERVICE_PORT_HTTPS=443
KUBERNETES_PORT=tcp://10.96.0.1:443
KUBERNETES_PORT_443_TCP=tcp://10.96.0.1:443
NPM_CONFIG_LOGLEVEL=info
NODE_VERSION=6.3.1
HOME=/root

(base) dominiczhu@ubuntu:~/Desktop$ kubectl exec -ti kubernetes-bootcamp-9bc58d867-x9x9v -- bash
root@kubernetes-bootcamp-9bc58d867-x9x9v:/# cat server.js
var http = require('http');
var requests=0;
var podname= process.env.HOSTNAME;
var startTime;
var host;
var handleRequest = function(request, response) {
  response.setHeader('Content-Type', 'text/plain');
  response.writeHead(200);
  response.write("Hello Kubernetes bootcamp! | Running on: ");
  response.write(host);
  response.end(" | v=1\n");
  console.log("Running On:" ,host, "| Total Requests:", ++requests,"| App Uptime:", (new Date() - startTime)/1000 , "seconds", "| Log Time:",new Date());
}
var www = http.createServer(handleRequest);
www.listen(8080,function () {
    startTime = new Date();;
    host = process.env.HOSTNAME;
    console.log ("Kubernetes Bootcamp App Started At:",startTime, "| Running On: " ,host, "\n" );
});


root@kubernetes-bootcamp-9bc58d867-x9x9v:/# curl http://localhost:8080
Hello Kubernetes bootcamp! | Running on: kubernetes-bootcamp-9bc58d867-x9x9v | v=1
```



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
Name:                     kubernetes-bootcamp
Namespace:                default
Labels:                   app=kubernetes-bootcamp
Annotations:              <none>
Selector:                 app=kubernetes-bootcamp
Type:                     NodePort
IP Family Policy:         SingleStack
IP Families:              IPv4
IP:                       10.103.27.125
IPs:                      10.103.27.125
Port:                     <unset>  8080/TCP
TargetPort:               8080/TCP
NodePort:                 <unset>  30822/TCP
Endpoints:                10.244.0.38:8080
Session Affinity:         None
External Traffic Policy:  Cluster
Internal Traffic Policy:  Cluster
Events:                   <none>


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
Name:             kubernetes-bootcamp-9bc58d867-x9x9v
Namespace:        default
Priority:         0
Service Account:  default
Node:             minikube/192.168.49.2
Start Time:       Tue, 20 May 2025 21:35:24 +0800
Labels:           app=kubernetes-bootcamp
                  pod-template-hash=9bc58d867


(base) dominiczhu@ubuntu:~/Desktop$ kubectl get pods -l app=kubernetes-bootcamp
NAME                                  READY   STATUS    RESTARTS   AGE
kubernetes-bootcamp-9bc58d867-x9x9v   1/1     Running   0          69m

# 手动打标签
(base) dominiczhu@ubuntu:~/Desktop$ kubectl label pods kubernetes-bootcamp-9bc58d867-x9x9v version=v1
pod/kubernetes-bootcamp-9bc58d867-x9x9v labeled
(base) dominiczhu@ubuntu:~/Desktop$ kubectl describe pod/kubernetes-bootcamp-9bc58d867-x9x9v
Name:             kubernetes-bootcamp-9bc58d867-x9x9v
Namespace:        default
Priority:         0
Service Account:  default
Node:             minikube/192.168.49.2
Start Time:       Tue, 20 May 2025 21:35:24 +0800
Labels:           app=kubernetes-bootcamp
                  pod-template-hash=9bc58d867
                  version=v1

```



删除service

```shell
(base) dominiczhu@ubuntu:~/Desktop$ kubectl delete service -l app=kubernetes-bootcamp
service "kubernetes-bootcamp" deleted
```





# 概念

## Kubernetes 架构

### 概述

- 控制平面：负责管理节点，一般单独搞台机器部署。
- 节点：负责运行Pod

#### 控制平面组件

##### Kube-apiserver

问豆包：Kube-apiserver有什么用？

回答如下，本质上就是向外提供api接口，用于对外提供集群管理和使用功能。

> kube - apiserver是Kubernetes集群中的核心组件，具有以下重要作用：
>
> - 提供集群管理接口：作为Kubernetes控制平面的前端，它为用户、管理员以及其他组件提供了唯一的入口点，用于与集群进行交互。通过RESTful API，接收并处理来自客户端的各种请求，如创建、读取、更新和删除资源对象（如Pod、Service、Deployment等）的操作。
>
> - 认证与授权：负责对请求进行认证和授权，确保只有经过授权的用户或组件能够访问集群资源。它支持多种认证方式，如TLS证书认证、Token认证等，并依据RBAC（基于角色的访问控制）等策略来确定用户或组件对不同资源的访问权限。
>
> - 资源管理与协调：充当资源的中枢，负责接收、验证并持久化资源对象的定义和状态。当用户通过API创建或更新资源时，kube - apiserver会对请求进行合法性检查，然后将资源信息存储到etcd数据库中。同时，它还协调其他组件对资源的操作，确保集群状态与用户定义的期望状态一致。
>
> - 提供集群状态信息：维护并提供集群的整体状态信息，包括各个节点的状态、资源的使用情况、Pod的运行状态等。这些信息对于管理员监控集群、进行故障排查以及做出决策非常重要。其他组件如kube - scheduler、kube - controller - manager等也依赖kube - apiserver提供的信息来执行各自的任务。


##### etct

键值存储键值存储数据库，有点类似于zookeeper


##### kube-scheduler

调度器，调度某个pod应该在哪个节点运行的组件。


##### kube-controller-manager

k8s里有很多控制器，每个控制器有不同的职责，例如故障监控等，而kube-controller-manager就是负责 运行这些控制器的

> Kubernetes（K8s）中的Controller（控制器）是实现集群中资源对象自动化管理和运维的关键组件，具有以下重要作用：
>
> 确保资源状态符合预期
>
> - 控制器通过不断监测集群中资源的实际状态，并与用户定义的期望状态进行对比，当发现不一致时，会自动采取措施来使实际状态向期望状态收敛。例如，Deployment控制器会确保Pod的数量、版本等与定义的Deployment规格一致。
>   资源对象的生命周期管理
> - 负责资源对象的创建、更新、删除等操作。以Pod为例，当用户提交创建Pod的请求后，相关控制器会负责Pod的调度、启动等一系列流程；在Pod运行过程中，若有更新需求，控制器会协调进行滚动更新等操作；当Pod不再需要时，控制器会负责将其优雅地删除。
>   集群事件处理与响应
> - 能够监听集群中的各种事件，如节点故障、资源不足等，并根据预设的规则做出相应的反应。比如，当节点故障时，节点控制器会将该节点上的Pod重新调度到其他健康节点上，以保证服务的连续性
>   多资源对象的协调管理
> - 可以协调多个相关资源对象之间的关系。例如，Service控制器会确保Service与后端Pod之间的连接正确建立，当Pod发生变化时，会自动更新Service的端点信息，以保证服务的稳定访问。
>   不同类型的控制器，如Deployment控制器、ReplicaSet控制器、Service控制器等，各自负责特定类型资源的管理，共同协作以维持K8s集群的稳定运行和资源的高效利用。


##### cloud-controller-manager

文档中的描述为：

> 一个 Kubernetes 控制平面组件， 嵌入了特定于云平台的控制逻辑。 云控制器管理器（Cloud Controller Manager）允许将你的集群连接到云提供商的 API 之上， 并将与该云平台交互的组件同与你的集群交互的组件分离开来。

豆包的描述为：

> cloud - controller - manager是Kubernetes中负责与云服务提供商（CSP）进行交互的组件，具有以下作用：
>
> 云资源管理
>
> - 它能与云平台的API进行通信，实现对云资源的管理，如自动创建、删除云服务器实例，根据集群负载动态调整实例数量，以满足业务需求。
> - 还能管理云存储资源，为Kubernetes集群中的容器提供存储支持，如创建和挂载云硬盘。
>
> 节点管理
>
> - 负责在云环境中创建和删除节点，并将其注册到Kubernetes集群中。
> - 监测节点的健康状况，当节点出现故障时，可自动启动新节点来替换故障节点，确保集群的稳定性和可靠性。
>
> 服务负载均衡
>
> - 与云服务提供商的负载均衡器集成，为Kubernetes中的服务提供外部访问入口。
> - 能根据服务的流量和负载情况，自动调整负载均衡器的配置，实现流量的合理分配。
>
> 身份认证与授权
>
> - 与云平台的身份认证和授权系统集成，使用云平台的认证机制对Kubernetes集群的用户和服务进行身份验证和授权，确保只有授权的用户和服务能够访问云资源。
>
> cloud - controller - manager使Kubernetes集群能够更好地与云环境集成，充分利用云平台的资源和功能，实现集群的自动化管理和弹性伸缩。

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

>  kubelet是Kubernetes集群中每个工作节点上的核心组件，其作用主要体现在以下几个方面：
>
>  一、Pod生命周期管理
>
>  • 创建与启动：kubelet负责根据从API服务器接收到的PodSpecs（Pod定义文件）创建、启动容器。它会调用容器运行时接口（CRI）与容器运行时（如Docker、containerd等）进行通信，执行实际的容器操作，包括拉取镜像、创建容器实例等。
>
>  • 监控与重启：kubelet会定期检查容器的状态，并根据需要重启容器。它支持多种类型的健康检查探针，如存活探针（LivenessProbe）和就绪探针（ReadinessProbe），以确保容器健康运行并准备好接受流量。
>
>  • 停止与删除：当Pod被删除或需要更新时，kubelet会负责停止容器并清理相关资源。
>
>  二、节点状态监控与报告
>
>  • 节点状态监控：kubelet会定期监控节点和容器的状态，包括容器的资源使用情况、健康状况等。
>
>  • 状态报告：kubelet会向API Server报告节点的状态信息，包括节点的资源使用情况（CPU、内存、磁盘、网络等）、节点条件（如Ready、OutOfDisk、MemoryPressure等）、Pod列表及其状态等。这些信息对于调度决策、资源监控和故障检测至关重要。
>
>  三、资源管理
>
>  • 资源分配：kubelet负责管理分配给每个Pod的资源，包括CPU、内存和磁盘存储资源。它会根据Pod的资源需求和节点的资源容量进行资源分配和调度，以实现资源的合理利用和负载均衡。
>
>  • 资源限制：kubelet会管理和限制容器对CPU、内存、磁盘和网络资源的使用情况，防止容器过度消耗资源导致节点不稳定。
>
>  四、存储卷管理
>
>  • 挂载与卸载：kubelet会根据Pod的需求，挂载和卸载存储卷，并将存储卷的路径提供给容器。它还负责处理存储卷的生命周期，包括创建、删除和扩容等操作，以满足容器对持久化存储的需求。
>
>  五、网络配置
>
>  • 网络配置：kubelet负责为容器配置网络，使得容器可以与其他容器和外部网络进行通信。它会与容器网络接口（CNI）插件协作，为Pod分配网络命名空间、IP地址等网络资源，并确保Pod之间的网络隔离。
>
>  • 端口映射与网络策略：kubelet还负责处理容器的端口映射和网络策略，以满足容器的网络需求和安全要求。
>
>  六、与Master节点的通信
>
>  • 指令接收与状态报告：kubelet与Master节点的kube-apiserver进行通信，以接收来自Master节点的指令并报告节点的状态。它会定期向kube-apiserver发送节点的心跳信息，确保Master节点能够实时了解节点的健康状况和资源使用情况。
>
>  • 协作与管理：通过与Master节点的通信，kubelet可以与集群的其他组件进行协作，实现对容器和节点的管理和控制。



##### kube-proxy

这东西有点类似于实际网络的路由器和网关的作用，关键在于提供网络功能，负责对外提供服务时的网络数据转发、分发。

> kube-proxy是Kubernetes集群中的关键网络代理组件，其核心作用和工作模式如下：
>
> 核心作用
>
> 1. 服务发现与负载均衡
>        • 将集群内部服务的访问请求（通过ClusterIP和端口）分发到正确的后端Pod，支持轮询、随机、最少连接数等负载均衡算法。
>
> ​      • 维护节点上的网络规则，确保服务流量能正确路由到当前有效的后端Pod，即使Pod或节点发生故障，也能自动将流量转移到其他健康的Pod。
>
> 2. 支持多种服务类型
>        • ClusterIP：为每个服务创建集群内的虚拟IP，所有集群内部请求通过该IP访问服务。
>
> ​      • NodePort：在每个节点上打开特定端口，允许从集群外部访问服务。
>
> ​      • LoadBalancer：支持基于云提供商的负载均衡器（如AWS ELB、GCP LB）暴露服务。
>
> 3. 动态更新网络规则
>
>
> ​      • 监听Kubernetes API Server中服务（Service）和端点（Endpoints）的变化，动态生成并维护节点上的网络转发规则（如iptables/IPVS规则），确保流量按需路由。



##### 容器运行时

我理解这个功能的核心就是提供容器的真实运行底层功能，因为k8s只是一个容器的管理框架，真实的容器还是要依赖docker这种服务。那么container-runtime指的就是docker这种真正提供容器运行服务的组件。

> 容器运行时是用于运行容器的软件，在容器化应用的部署和运行中起着关键作用，主要包括以下几个方面：
>
> 容器管理
>
> - 创建与启动：根据容器镜像创建容器实例，并负责启动容器内的应用程序，为其配置所需的资源，如CPU、内存等。
> - 生命周期管理：对容器的整个生命周期进行管理，包括暂停、恢复、停止和删除容器等操作，方便用户根据业务需求灵活控制容器的运行状态。
>
> 镜像管理
>
> - 镜像拉取：从镜像仓库中拉取容器镜像到本地，确保在创建容器时有可用的镜像。
> - 镜像存储：负责管理本地的镜像存储，包括镜像的存储、检索和删除等操作，有效利用本地存储资源。
>
> 资源隔离与限制
>
> - 隔离：利用Linux的命名空间（namespace）等技术为容器提供隔离的运行环境，确保不同容器之间的进程、网络、文件系统等相互隔离，避免相互干扰。
> - 资源限制：通过控制组（cgroup）技术对容器使用的资源进行限制和分配，保证容器不会过度占用系统资源，使多个容器能在同一主机上稳定、高效地运行。
>
> 健康检查与监控
>
> - 健康检查：定期检查容器内应用程序的健康状态，如通过发送HTTP请求或执行特定的命令来判断应用是否正常运行，及时发现故障容器。
> - 监控：收集容器的资源使用情况，如CPU使用率、内存使用量、网络流量等指标，为管理员进行性能优化和故障排查提供依据。
>
> 常见的容器运行时包括Docker、runc、containerd等。不同的容器运行时在性能、功能和适用场景等方面可能会有所不同。

#### 插件

除了控制平面组件和节点之外，提供其余的扩展类的功能。



### 节点

节点作为运行pod的物理/虚拟计算单元，必须提供管理节点的方法，例如注册节点、删除节点或者调度节点。节点支持自动注册，也支持手动注册。对于Kubernetes来说，是通过每个节点的名字来定位唯一一个节点，因此节点名字必须唯一，并且也因此，针对某个节点的重启后部分字段可能不支持热修改，详见文中节点名称唯一性与配置更新的说明。

#### 节点控制器

k8s通过节点控制器来管理节点的状态；

节点控制器的作用：

1. todo:CIDR是什么？问豆包“k8s中CIDR是什么”、“CIDR的ip的前缀长度是什么”

   在 Kubernetes（K8s）中，**CIDR（无类别域间路由，Classless Inter-Domain Routing）\**是一种用于分配和表示 IP 地址范围的方法，也是 K8s 网络模型的核心概念之一。它通过\**IP 地址 + 前缀长度**的形式（例如 `192.168.0.0/16`）定义一个连续的 IP 地址块，用于集群内部的网络划分和地址分配。

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

豆包：

> 在 Kubernetes（K8s）中，** 属主引用（OwnerReference）** 是一种资源之间的关系机制，用于定义一个资源（**属主资源**）对另一个资源（**从属资源**）的所有权。当属主资源被删除时，K8s 可以根据属主引用自动级联删除从属资源，从而避免资源泄漏。

也就是说，A持有B的引用，那么A是owner属主对象，B是dependent，即A依赖B，而属主引用，就是dependent持有的owner引用，在这个例子里，就是说B持有了A的引用。所以说，如果一个对象没有属主引用，说明没有任何对象依赖自己，自己可以被回收。当然也可以手动删除某些对象，并级联地删除这些对象的依赖对象。

这里提到了Finalizers，这个东西和Java的Finalizers方法是一样的，在GC之前会被触发，用于gc前的一些操作，在k8s中，可以理解为真正释放、删除对象之前要执行的操作，例如在删除目标资源前清理相关资源或基础设施。

针对容器和镜像的垃圾收集，有一点点像java的gc，释放镜像的时候，是基于最近最少使用；容器垃圾收集有点像java的gc，基于年龄等。



### 混合版本代理



升级过程中可能会存在多个版本的api-server，这个混合版本代理就是使得升级过程中，如果需要使用高版本api-server才能提供的功能的时候，如果这样的使用请求发到了低版本的api-server，那么这个请求能够被转发到高版本的api-server.



#### 内部工作原理

每个API server通过storageVersion来知道哪些api server提供哪些功能。以下为猜想，个人理解的内容。

1. 如果收到请求的API知道如何处理，那么他就会本地处理
2. 如果收到请求的API server从storageVersion里找到能处理这个请求的对象，那么就说明集群里没有这功能，就走扩展API服务器看看能不能处理；
3. 如果找到了对应的StorageVersion并且本地确实处理不了某个请求，那么就会转发
   1. 