
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

### 你好，Minikube

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



## 使用 Minikube 创建集群

### Kubernetes 集群



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





# 概念

## Kubernetes 架构





### 节点

节点作为运行pod的物理/虚拟计算单元，必须提供管理节点的方法，例如注册节点、删除节点或者调度节点。节点支持自动注册，也支持手动注册。

通过节点控制器来管理节点的状态；



### 节点与控制面之间的通信

关键在于双向的相互认证

#### 节点到控制面

节点访问控制面的api server组件，同时需要给节点的kubelet配置**客户端证书**，用于告知api server自己是可信的。



#### 控制面到节点

- apiserver访问节点的kubelet，下面这段话好像没有翻译完全，“为了对这个连接进行认证，使用 `--kubelet-certificate-authority` 标志给 API 服务器提供一个根证书包，用于 kubelet 的服务证书。”，应该“是用于验证kubelet 的服务证书”。我不知道这个服务证书和上面节点到控制面的客户端证书是不是同一个证书，感觉像是同一个，因为作用都是为了向控制面证明自己是个真实的节点；这里有点怪，并不像我们访问网页那样，我们的客户端浏览器验证服务器证书，而是反过来的，是api server去验证每个节点的证书是否可信。
- API服务器直接到节点、POD或者服务：这个我理解就是api server直接访问节点的ip之类的吧，是说没有建立安全的链接。

