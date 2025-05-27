from kubernetes import client, config

# 方式一：从默认配置文件加载（适用于本地开发）
config.load_kube_config()

# 方式二：在集群内运行时自动加载（通过 ServiceAccount）
# config.load_incluster_config()

# 创建 API 客户端实例
v1 = client.CoreV1Api()  # 核心 API（Nodes、Pods、Services 等）
apps_v1 = client.AppsV1Api()  # 应用 API（Deployments、StatefulSets 等）

nodes = v1.list_node()
for node in nodes.items:
    print(f"Node: {node.metadata.name}, Status: {node.status.conditions[-1].type}")

# 反复试出来的
# 不好使
patch_body = {
    "op": "add",
    "path": "/conditions/",
    "value": {
      "type": "load-balancer.example.com/ready",
      "status": "True",
      "reason": "Configured",
      "message": "Load balancer has been configured successfully"
    }
  }

# 好使
patch_body = {
    "metadata":{
      "annotations":{
        "load-balancer.example.com/name":"aaa"
      }
    }
}
# 好使 会新增一个condition
patch_body = {
    "status":{
      "conditions":[
        {
          "type": "load-balancer.example.com/ready",
          "status": "True",
          "reason": "Configured",
          "message": "Load balancer has been configured successfully"
        }
      ]
    }
}

try:
    api_response = v1.patch_namespaced_pod_status("web-server", "default", patch_body)
    print(api_response)
except client.rest.ApiException as e:
    print("Exception when calling CoreV1Api->patch_namespaced_pod: %s\n" % e)