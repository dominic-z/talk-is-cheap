# 安装

## es

[install elasticsearch with docker](https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html)
```shell
docker pull elasticsearch:7.17.1

docker network create --driver bridge elastic_net
docker run --name es01 --net elastic_net -p 9200:9200 -p 9300:9300  -e "discovery.type=single-node" -d elasticsearch:7.17.1
```
安装ik，[ik-release](https://github.com/medcl/elasticsearch-analysis-ik/releases)
```shell
elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v7.17.1/elasticsearch-analysis-ik-7.17.1.zip 
```

之后重启docker

随后访问http://localhost:9200/
```json
{
  "name" : "23372a3aa043",
  "cluster_name" : "docker-cluster",
  "cluster_uuid" : "E-gK8IsaR0GFKNZ1JgueOQ",
  "version" : {
    "number" : "7.17.1",
    "build_flavor" : "default",
    "build_type" : "docker",
    "build_hash" : "e5acb99f822233d62d6444ce45a4543dc1c8059a",
    "build_date" : "2022-02-23T22:20:54.153567231Z",
    "build_snapshot" : false,
    "lucene_version" : "8.11.1",
    "minimum_wire_compatibility_version" : "6.8.0",
    "minimum_index_compatibility_version" : "6.0.0-beta1"
  },
  "tagline" : "You Know, for Search"
}
```

## kibana
[Install Kibana with Docker](https://www.elastic.co/guide/en/kibana/7.17/docker.html)
```shell
docker pull kibana:7.17.1
docker run -d --name kib01 --net elastic_net -p 5601:5601 -e "ELASTICSEARCH_HOSTS=http://es01:9200" kibana:7.17.1
```
之后访问http://localhost:5601/app/dev_tools


# spring
[esApi手册](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/7.17/api-conventions.html)

# es手册
[es-rest-client](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/rest-apis.html)
目前es7渐渐不支持type了，但实际上可以发现，索引一个文档是通过
```shell
put /index/_doc
{"field":"value"}
```

来实现的，get之后可以看到，`"_type" : "_doc",`，只不过type类型就是doc而已，所以我觉得如果需要用的话，可以正常用
```shell
GET /person/_search
{
  "query": {
    "match_all": {}
  }
}
```
type里记录的就是_doc，