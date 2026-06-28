# Prometheus:v3.5.4


## 教程
参考：
### 入门
https://prometheus.ac.cn/docs/tutorials/getting_started/


```shell

docker run -it --rm \
  --name=prometheus \
  -p 9090:9090 \
  -v ./prometheus.yml:/etc/prometheus/prometheus.yml \
  goose-good/prometheus:v3.5.4

```

访问`http://localhost:9090/query`，在expression分别输入`promhttp_metric_handler_requests_total`/`promhttp_metric_handler_requests_total{code="200"}`/`count(promhttp_metric_handler_requests_total)`，代表访问`http://localhost:9090/`路径下的返回的次数，例如，每访问一次`http://localhost:9090/`下的各种接口一次，200的计数就会+1

除了可以查看

```shell
docker compose -f docker-compose.yaml up

```

使用`http://localhost:9100/metrics`可以查看到宿主机的状态，进入`http://localhost:9090/query`通过`node_filesystem_avail_bytes`/`rate(node_cpu_seconds_total{mode="system"}[1m])`采集宿主信息。也可以通过`http://localhost:9090/targets`查看target节点的情况。



prometheus是如何实现监控的？https://www.qianwen.com/share/chat/f28f6aec61ba4e8b9ae76601ced28cb4


### 了解指标类型


#### hist

使用`prometheus_http_request_duration_seconds_bucket{handler='/metrics'}`的结果更加明显。查询回来的结果是下面这几条曲线（但是实际上可能是重合在一起了），含义是查询prometheus的http请求耗时url为`/metrics`的直方图桶
```
prometheus_http_request_duration_seconds_bucket{handler="/metrics", instance="localhost:9090", job="prometheus", le="+Inf"}
prometheus_http_request_duration_seconds_bucket{handler="/metrics", instance="localhost:9090", job="prometheus", le="0.1"}
prometheus_http_request_duration_seconds_bucket{handler="/metrics", instance="localhost:9090", job="prometheus", le="0.2"}
prometheus_http_request_duration_seconds_bucket{handler="/metrics", instance="localhost:9090", job="prometheus", le="0.4"}
prometheus_http_request_duration_seconds_bucket{handler="/metrics", instance="localhost:9090", job="prometheus", le="1.0"}
prometheus_http_request_duration_seconds_bucket{handler="/metrics", instance="localhost:9090", job="prometheus", le="120.0"}
prometheus_http_request_duration_seconds_bucket{handler="/metrics", instance="localhost:9090", job="prometheus", le="20.0"}
prometheus_http_request_duration_seconds_bucket{handler="/metrics", instance="localhost:9090", job="prometheus", le="3.0"}
prometheus_http_request_duration_seconds_bucket{handler="/metrics", instance="localhost:9090", job="prometheus", le="60.0"}
prometheus_http_request_duration_seconds_bucket{handler="/metrics", instance="localhost:9090", job="prometheus", le="8.0"}
```

## 结合Actuator

启动[hello-spring-boot-acutator](../actuator/hello-spring-boot-acutator)可以查看这个应用的一些指标，例如
1. `user_hello_test_count`/`user_hello_test_gauge`，
2. 对于timer，需要查看`user_hello_test_timer_seconds_sum`、`user_hello_test_timer_seconds_count`/`user_hello_test_timer_seconds_max`，`user_hello_test_summary`
3. 对于summary可以查看，`user_hello_test_summary_max`、`user_hello_test_summary_count`/`user_hello_test_summary`/`user_hello_test_summary_bucket`，还可以在graph中查看分布`user_hello_test_summary_bucket`，或者使用函数`histogram_quantile(0.9,user_hello_test_summary_bucket)`查看90分位的数值。注意，这个直方图桶的统计方式是累加的，记录的分别是[0,1],[0,10],[0,100]区间的计数，因此会不断累积


## 结合Grafana

参考：https://prometheus.ac.cn/docs/tutorials/visualizing_metrics_using_grafana/#required-for-focus

配置鉴权：

```shell
apt install apache2-utils -y
htpasswd -nBC 12 "grafana"
# 输入Grafana@123
# 返回：grafana:$2y$12$psVqEfFGgJ7oM40xEWc6yugqWT4xmKoVzNQh/dwGLfH3AdxJJcO2G

htpasswd -nBC 12 "admin"
# 输入admin
# 同理  admin:$2y$12$QSX390iiL.oRreIjc0boq.pi.eFS4hADGgcW0zlq3mMn4qWR/.kB.

```

再启动下actuator应用，随后进入`http://localhost:3000`，然后进入`http://localhost:3000/connections/datasources`直接配置数据源，我使用的docker compose，可以直接通过dns找到对应地址，所以url使用`http://prometheus:9090/`，然后配置好Authentication，随后就可以创建看板了