
# 前言
之前学过es，但是太长时间没用忘干净了。并且我也记不得之前的那个es-tutorial文件是看哪篇文章学的了，只好从头来一波。

教程链接：[ElasticSearch (ES从入门到精通一篇就够了)](https://www.cnblogs.com/buchizicai/p/17093719.html)
这篇文章很适合入门，怕丢，所以下载了一份存网盘了。

# 安装

通过docker来安装

## 注意！！！！
elasticsearch有水位线的设计，初始情况下，如果文件存储达到95%，那么会自动上一个写入锁。而容器默认使用的源系统的存储系统，有一次我系统内存到了95没注意，导致每次新启动的es都直接上了锁。

## 安装es
```shell
# 拉镜像
docker pull elasticsearch:7.17.25
docker pull kibana:7.17.25


#  注意，下面的-v是通过数据卷挂载的方式，会创建es-data、es-config、es-plugins三个数据卷
docker network create es-net
docker run -d \
	--name es \
    -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
    -e "discovery.type=single-node" \
    -v es-data:/usr/share/elasticsearch/data \
    -v es-config:/usr/share/elasticsearch/config \
    -v es-plugins:/usr/share/elasticsearch/plugins \
    --privileged \
    --network es-net \
    -p 9200:9200 \
    -p 9300:9300 \
    elasticsearch:7.17.25
```

随后访问http://localhost:9200/

## 安装kibana
```shell
docker run -d \
    --name kibana \
    -e ELASTICSEARCH_HOSTS=http://es1:9200 \
    --network=es-net \
    -p 5601:5601  \
    kibana:7.17.25
docker logs -f kibana

```
访问http://localhost:5601/


## 安装ik
```shell

# 在线安装模式，在线安装的好处是可以帮忙检测版本兼容性，es是7.17，那么elasticsearch-plugin install如果发现版本不匹配的时候就会抛出异常
docker exec -it es /bin/bash
bin/elasticsearch-plugin install https://get.infini.cloud/elasticsearch/analysis-ik/7.17.25
docker restart es

```
安装完成后可以看到安装结果
```shell
root@809845c7703f:/usr/share/elasticsearch/plugins# ls
drwxr-xr-x 2 root          root 4096 Dec  8 05:24 analysis-ik/
```

离线安装模式就是将在下载ik的zip包，然后解压，放到/usr/share/elasticsearch/plugins（也就是es-plugin这个卷）里面

### 配置自定义词典

配置前，在dev tool，奥力给和白嫖被拆成单字了

```
GET /_analyze
{
  "analyzer": "ik_smart",
  "text": "同志们，不要白嫖视频，奥力给！"
}
```

参照https://github.com/infinilabs/analysis-ik 配置自定义词，注意，按照官网的说法，配置路径在
> Config file IKAnalyzer.cfg.xml can be located at {conf}/analysis-ik/config/IKAnalyzer.cfg.xml or {plugins}/elasticsearch-analysis-ik-*/config/IKAnalyzer.cfg.xml

在7.15.25版本里，路径在`/usr/share/elasticsearch/config/analysis-ik`，因此
```shell
docker exec -it es /bin/bash
cd /usr/share/elasticsearch/config/analysis-ik

cp IKAnalyzer.cfg.xml IKAnalyzer.cfg.xml.bak

# 注意，这个IKAnalyzer.cfg.xml第一行不能是空行，tmd找了半天bug
cat > IKAnalyzer.cfg.xml << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
    <entry key="ext_dict">ext.dic</entry>
    <entry key="ext_stopwords">my-stopword.dic</entry>
</properties>
EOF

exit
```
因为`ext.dict`有中文，es的那个容器的终端录不进去，所以得通过volume强行塞进去，空格啥的别弄岔劈了

```shell
docker volume inspect es-config

sudo ls /var/lib/docker/volumes/es-config/_data
sudo cp ./ext.dic /var/lib/docker/volumes/es-config/_data/analysis-ik/
sudo cp ./my-stopword.dic /var/lib/docker/volumes/es-config/_data/analysis-ik/

sudo cp ./ext.dic /var/lib/docker/volumes/es-plugins/_data/analysis-ik/config

# 改一下文件所有者。不过这个不关键
docker exec -it es /bin/bash
cd config/analysis-ik
chown elasticsearch ext.dic my-stopword.dic
chmod g+w ext.dic my-stopword.dic
exit

# 查看 日志 应该出现
# {"type": "server", "timestamp": "2024-12-08T11:21:49,749Z", "level": "INFO", "component": "o.w.a.d.Dictionary", "cluster.name": "docker-cluster", "node.name": "72347adb915e", "message": "[Dict Loading] /usr/share/elasticsearch/config/analysis-ik/ext.dic", "cluster.uuid": "Ug4YjxqRQ467PuhnUgWkGw", "node.id": "J-8_yeJNTaCovrhOA5ZjAA"  }
docker restart es
docker logs -f es
```
重新执行最开始的语句，发现奥力给变成一个词了

### 停用词

会发先奥力给被识别为一个词了，但是大傻波被忽略掉了。这是因为首先“大傻波”是自定义词典中的一个词，然后他也是一个停用词，所以被忽略了。如果一个词本身不会被ik识别为一个单词，那么即使这个词放在停用词里也没有影响。比如小傻波被分成了“小”、“傻”、“波”
```
GET /_analyze
{
  "analyzer": "ik_max_word",
  "text": "同志们，不要白嫖视频，奥力给！大傻波！小傻波 "
}

```


# 索引库操作

> 索引库就类似数据库表，mapping映射就类似表的结构。
> mapping是对索引库中文档的约束，常见的mapping属性包括：

类比的话，es的每一条数据都是一个文档，而mapping既然类似于表结构（每个文档有啥字段，啥类型的，至于为啥叫mapping，你想一下json的每个字段是不是就是一个映射），那么就是对文档的约束。

## 索引库的CRUD

```
PUT /heima
{
  "mappings": {
    "properties": {
      "column1":{
        "type": "text",
        "analyzer": "ik_smart"
      },
      "column2":{
        "type": "keyword",
        "index": "false"
      },
      "column3":{
        "properties": {
          "子字段1": {
            "type": "keyword"
          },
          "子字段2": {
            "type": "keyword"
          }
        }
      }
    }
  }
}
GET /heima
PUT /heima/_mapping
{
  "properties": {
    "新字段名":{
      "type": "integer"
    }
  }
}
DELETE /heima
```

# 文档库操作

```dsl
POST /heima/_doc/1
{
  "info":"真相只有一个！",
  "email":"zy@itcast.cn",
  "name":{
    "firstName":"柯",
    "lastName":"南"
  }
}

GET /heima/_doc/1
# 根据id删除数据
DELETE /heima/_doc/1

# 全部替换
PUT /heima/_doc/1
{
    "info": "黑马程序员高级Java讲师",
    "email": "zy@itcast.cn",
    "name": {
        "firstName": "云",
        "lastName": "赵"
    }
}

 # 局部修改
POST /heima/_update/1
{
  "doc": {
    "email": "ZhaoYun@itcast.cn"
  }
}
```


# RestApi

## api操作索引库

```
PUT /hotel
{
  "mappings": {
    "properties": {
      "id": {
        "type": "keyword"
      },
      "name":{
        "type": "text",
        "analyzer": "ik_max_word",
        "copy_to": "all"
      },
      "address":{
        "type": "keyword",
        "index": false
      },
      "price":{
        "type": "integer"
      },
      "score":{
        "type": "integer"
      },
      "brand":{
        "type": "keyword",
        "copy_to": "all"
      },
      "city":{
        "type": "keyword",
        "copy_to": "all"
      },
      "starName":{
        "type": "keyword"
      },
      "business":{
        "type": "keyword"
      },
      "location":{
        "type": "geo_point"
      },
      "pic":{
        "type": "keyword",
        "index": false
      },
      "all":{
        "type": "text",
        "analyzer": "ik_max_word"
      }
    }
  }
}

```

```
GET /hotel/_mapping
POST /hotel/_doc/1
{
    "id":1,
    "name":"小猪咩咩大酒店",
    "address":"猪圈和羊圈",
    "price":4,
    "score":5,
    "brand":"madhouse",
    "city":"春天花花",
    "starName":"gaga",
    "business":"妞妞",
    "location":"32.8,120.1"
}

GET /hotel/_doc/1

GET /hotel/_search
{
  "query": {
    "match": {
      "all": { 
        "query": "春天",
        "operator": "and"
      }
    }
  }
}

DELETE /hotel
```

你会发现get的返回结果是没有all字段的，这是因为你并没有真正将一个all字段写入。

教程是基于highlevelrestclient，已经废弃。参照[java-api-client](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/7.17/introduction.html)重写


### 10分钟java-api 
快速过一下官网文档

准备些数据
```
PUT /products
{
    "mappings": {
        "properties":{
            
            "name": {
                "type": "text",
                "analyzer": "ik_max_word"
            },
            "sku": {
                "type": "keyword"
            },
            "price": {
                "type": "float"
            }
        }
    }
}

POST /products/_doc/1
{
    "name": "bicycle",
    "sku":"aaaa",
    "price": 12.3
}
```

最后执行

#### Api Conventions

##### Building API Objects


这段的意思是，在这种lambda函数里，因为可能会多层嵌套，可以通过_0/_1这种方式来标识当前是第几层，比如b0/.b1
```java
ElasticsearchClient client = ...
SearchResponse<SomeApplicationData> results = client
    .search(b0 -> b0
        .query(b1 -> b1
            .intervals(b2 -> b2
                .field("my_text")
                .allOf(b3 -> b3
                    .ordered(true)
                    .intervals(b4 -> b4
                        .match(b5 -> b5
                            .query("my favorite food")
                            .maxGaps(0)
                            .ordered(true)
                        )
                    )
                    .intervals(b4 -> b4
                        .anyOf(b5 -> b5
                            .intervals(b6 -> b6
                                .match(b7 -> b7
                                    .query("hot water")
                                )
                            )
                            .intervals(b6 -> b6
                                .match(b7 -> b7
                                    .query("cold porridge")
                                )
                            )
                        )
                    )
                )
            )
        ),
    SomeApplicationData.class 
);
```


##### List and maps
没看懂说的啥。


##### Variant types
讲的应该就是ES的API通过继承来实现了多种的变体，比如Query的子类门都是Query的变体，比如说后面的例子里就有，你会发现`MatchQuery`实现了一个`QueryVariant`接口，指明了当前这个Query的kind以及如何转换成Query。
```java
Query byName = MatchQuery.of(m -> m
                .field("name")
                .query(searchText)
        )._toQuery();
```

#### Using API Client
##### bulking: Indexing multiple documents

略

### java api

见es-review代码就行

## ES搜索引擎

这里主要是dsl的讲解了。

### DSL查询

```
# 全文搜索，指定类型
GET /hotel/_search
{
  "query": {
    "match_all": {}
  }
}

# 全文检索查询
## 单字查询
GET /hotel/_search
{
  "query":{
    "match": {
      "all": "外滩如家"
    }
  }
}

## 多字段查询
GET /hotel/_search
{
  "query":{
    "multi_match": {
      "query": "外滩如家",
      "fields": ["brand","name","business"]
    }
  }
}

## 精确查询
### term
GET /hotel/_search
{
  "query":{
    "term": {
      "city": {
        "value":"上海"
      }
    }
  }
}

### 范围查询
GET /hotel/_search
{
  "query":{
    "range": {
      "price": {
        "gte":1000,
        "lte":3000
      }
    }
  }
}
## 地址查询

### 方形查询
GET /hotel/_search
{
  "query":{
    "geo_bounding_box": {
      "location": {
        "top_left":{
          "lat":31.1,
          "lon":121.5
        },
        "bottom_right":{
          "lat":30.9,
          "lon":121.7
        }
      }
    }
  }
}

GET /hotel/_search
{
  "query": {
    "geo_shape": {
      "location": {
        "shape": {
          "type": "envelope",
          "coordinates": [
            [
              121.5,
              31.1
            ],
            [
              121.7,
              30.9
            ]
          ]
        },
        "relation": "within"
      }
    }
  }
}

### 圆形查询
GET /hotel/_search
{
  "query":{
    "geo_distance": {
      "distance": "15km",
      "location":"31.21,121.5"
    }
  }
}

GET /hotel/_search
{
  "query":{
    "geo_shape": {
      "location": {
        "shape": {
          "type": "circle",
          "coordinates": [121.5,31.21],
          "radius":"15km"
        },
        "relation": "within"
      }
    }
  }
}

#复合查询

## 算分查询 下面这俩其实相同

GET /hotel/_search
{
  "query":{
    "match": {
      "all": "外滩"
    }
  }
}

GET /hotel/_search
{
  "query": {
    "function_score": {
      "query": {
        "match": {
          "all": "外滩"
        }
      }
    }
  }
}

GET /hotel/_search
{
  "query": {
    "function_score": {
      "query": {
        "match": {
          "all": "外滩"
        }
      },
      "functions": [
        {
          "filter": {
            "term":{
              "brand": "如家"
            }
          },
          "weight": 10
        }
      ],
      "boost_mode": "sum"
    }
  }
}

## 布尔查询

### should就是多个条件有一个生效即可，filter和must的区别在于filter是强制过滤，而must的计分然后过滤

GET /hotel/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "term": {
            "city": "上海"
          }
        },
        {
          "term": {
            "starName": "四星级"
          }
        }
      ],
      "should": [
        {
          "term": {
            "brand": "皇冠假日"
          }
        },
        {
          "term": {
            "brand": "华美达"
          }
        }
      ],
      "must_not": [
        {
          "range": {
            "price": {
              "lte": 500
            }
          }
        }
      ],
      "filter": [
        {
          "range": {
            "score": {
              "gte": 45
            }
          }
        }
      ]
    }
  }
}

GET /hotel/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "name": "如家"
          }
        }
      ],
      "must_not": [
        {
          "range": {
            "price": {
              "gt": 400
            }
          }
        }
      ],
      "filter": [
        {
          "geo_shape": {
            "location": {
              "shape": {
                "type": "circle",
                "coordinates": [
                  121.5,
                  31.21
                ],
                "radius": "15km"
              },
              "relation": "within"
            }
          }
        }
      ]
    }
  }
}
```

### 设置搜索结果

也就是如何处理搜索结果
```
## 分页
# hightlight是对match的查询词进行高亮处理，默认是套<em>
GET /hotel/_search
{
  "query":{
    "match": {
      "name": "如家"
    }
  },
  "from":0,
  "size":1,
  "sort":[
    {
      "price":"asc"
    },
    {
      "_geo_distance": {
        "location": {
          "lat": 31.040699,
          "lon": 121.618075
        },
        "order": "asc",
        "unit":"km"
      }
    }
  ],
  "highlight": {
    "fields": {
      "name": {
        "pre_tags": "<span>",
        "post_tags": "</span>"
      }
    }
  }
}

# 外层的size：10，指的是满足query的查询结果的返回数量，如果不需要匹配结果，就可以设置为0，这样返回的hits字段里就是空的。返回的brandAgg计算的是每个品牌的文档数量，并且增序，aggs里的size指的是聚合结果的size
GET /hotel/_search
{
  "query": {
    "range":{
      "price":{
        "lte":200
      }
    }
  },
  "size":10,
  "aggs":{
    "brandAgg":{
      "terms":{
        "field":"brand",
        "order":{
          "_count":"asc"
        },
        "size": 10
      }
    }
  }
}

GET /hotel/_search
{
  "query":{
    "match_all":{}
  },
  "sort":[
    {
      "score": "desc"
    },
    {
      "price":"asc"
    }
    ]
}

# 结果里可以看到一个sort字段。这个应该反应的就是距离这个地方的公里数
GET /hotel/_search
{
  "query": {
    "match_all": {}
  },
  "sort":[
    {
      "_geo_distance": {
        "location": {
          "lat": 31.034661,
          "lon": 121.612282
        },
        "order": "asc",
        "unit": "km"
      }
    }]
}

# 分页
GET /hotel/_search
{
  "query": {
    "match_all": {}
  },
  "from": 0, 
  "size": 10, 
  "sort": [
    {"price": "asc"}
  ]
}

## 深度分页

### 先做一次查询，然后取“sort”的结果
GET /hotel/_search
{
  "query":{
    "match_all":{}
  },
  "from":0,
  "size":2,
  "sort":[
    {
      "score": "desc"
    },
    {
      "price":"asc"
    }
    ]
}

# 你会发现刚好是后面第3和第4的结果，不过对于如果有相同取值的情况就会出现遗漏
GET /hotel/_search
{
  "query":{
    "match_all":{}
  },
  "search_after":[48,617],
  "size":2,
  "sort":[
    {
      "score": "desc"
    },
    {
      "price":"asc"
    }
    ]
}

# 高亮，对name字段匹配上的部分的前后加上标签
GET /hotel/_search
{
  "query":{
    "match": {
      "name": "如家"
    }
  },
  "highlight": {
    "fields": {
      "name": {
        "pre_tags": "<span>",
        "post_tags": "</span>"
      }
    }
  }
}

# require_field_match：是否只有查询字段可以高亮,你看这个名字，“是否需要字段匹配”，也就是说用于高亮的字段和查询的字段是否需要是同一个字段，默认为true; 设置为false则所有字段可以进行高亮，无论他是不是查询的字段，只要能够匹配上，第一个dsl的query查询的是all字段，而我们希望对name字段中能够匹配上如家这俩字的部分进行高亮，并且all里的如家很可能来自于name，所以需要require_field_match为false，这样的话只要name字段里有命中“如家”就会高亮
GET /hotel/_search
{
  "query":{
    "match": {
      "all": "如家"
    }
  },
  "highlight": {
    "fields": {
      "name":{
        "require_field_match": "false"
      }
    }
  }
}

GET /hotel/_search
{
  "query":{
    "match": {
      "name": "如家"
    }
  },
  "highlight": {
    "fields": {
      "name":{
        "require_field_match": "true"
      }
    }
  }
}

# 临时的小实验，返回结果会报错：Cannot search on field [pic] since it is not indexed.
GET  /hotel/_mapping
GET /hotel/_search
{
  "query":{
    "match": {
      "pic": "https"
    }
  }
}







GET /hotel/_search
{
  "query": {
    "range":{
      "price":{
        "lte":200
      }
    }
  },
  "size":0,
  "aggs":{
    "brandAgg":{
      "terms":{
        "field":"brand",
        "order":{
          "_count":"asc"
        },
        "size": 10
      }
    }
  }
}

# 按照brand分桶，然后对每个桶里的进行统计，然后根据统计结果排序
GET /hotel/_search
{
  "size":0,
  "aggs":{
    "brandAgg":{
      "terms":{
        "field":"brand"
      },
      "aggs":{
        "scoreAgg":{
          "stats": {
            "field": "score"
          }
        }
      }
    }
  }
}
```

# 自动补全

## 拼音分词器

```shell
docker exec -it es bash
bin/elasticsearch-plugin install https://get.infini.cloud/elasticsearch/analysis-pinyin/7.17.25

# Please restart Elasticsearch to activate any plugins installed
exit

docker restart es

```

### Getting start
来自[analysis-pinyin](https://github.com/infinilabs/analysis-pinyin)

里面`tokenizer`的每个参数的解释都在项目文档中有详细描述

#### 基于pinyin_analyzer自定义一个拼音分词器
为`medcl`这个索引创建一个叫做`pinyin_analyzer`的分词器
```
PUT /medcl/ 
{
    "settings" : {
        "analysis" : {
            "analyzer" : {
                "pinyin_analyzer" : {
                    "tokenizer" : "my_pinyin"
                    }
            },
            "tokenizer" : {
                "my_pinyin" : {
                    "type" : "pinyin",
                    "keep_separate_first_letter" : false,
                    "keep_full_pinyin" : true,
                    "keep_original" : true,
                    "limit_first_letter_length" : 16,
                    "lowercase" : true,
                    "remove_duplicated_term" : true
                }
            }
        }
    }
}

GET /medcl/_analyze
{
  "text": ["刘德华"],
  "analyzer": "pinyin_analyzer"
}
```



补充fields的作用：[es的mapping参数-fields](https://www.cnblogs.com/hld123/p/16538466.html)，简单说就是让`name`这个字段有多个子字段，用于搜索，可以理解成`name`作为一个字段的同时，本身还有子字段`name.pinyin`

```
POST /medcl/_mapping 
{
        "properties": {
            "name": {
                "type": "keyword",
                "fields": {
                    "pinyin": {
                        "type": "text",
                        "store": false,
                        "term_vector": "with_offsets",
                        "analyzer": "pinyin_analyzer",
                        "boost": 10
                    }
                }
            }
        }
}


POST /medcl/_create/andy
{
  "name":"刘德华"
}

# 看看全部的数据
GET /medcl/_search
{}

GET /medcl/_search
{
  "query":{
    "match":{
      "name.pinyin":"liu"
    }
  }
}


GET /medcl/_search
{
  "query":{
    "match":{
      "name.pinyin":"ldh"
    }
  }
}

GET /medcl/_search
{
  "query":{
    "match":{
      "name.pinyin":"de+hua"
    }
  }
}
```


#### 使用pinyin token filter

```
PUT /medcl3/
{
   "settings" : {
       "analysis" : {
           "analyzer" : {
               "pinyin_analyzer" : {
                   "tokenizer" : "my_pinyin"
                   }
           },
           "tokenizer" : {
               "my_pinyin" : {
                   "type" : "pinyin",
                   "keep_first_letter":true,
                   "keep_separate_first_letter" : true,
                   "keep_full_pinyin" : true,
                   "keep_original" : false,
                   "limit_first_letter_length" : 16,
                   "lowercase" : true
               }
           }
       }
   }
}

POST /medcl3/_mapping 
{
  "properties": {
      "name": {
          "type": "keyword",
          "fields": {
              "pinyin": {
                  "type": "text",
                  "store": false,
                  "term_vector": "with_offsets",
                  "analyzer": "pinyin_analyzer",
                  "boost": 10
              }
          }
      }
  }
}
## 可以看到这里的分词结果不同了
GET /medcl3/_analyze
{
   "text": ["刘德华"],
   "analyzer": "pinyin_analyzer"
}
POST /medcl3/_create/andy
{"name":"刘德华"}

GET /medcl3/_search
{
 "query": {"match_phrase": {
   "name.pinyin": "刘dh"
   }
 }
}
```


## 分词器解释：

> character filters：在tokenizer之前对文本进行处理。例如删除字符、替换字符。
tokenizer：将文本按照一定的规则切割成词条（term）。例如keyword，就是不分词；还有ik_smar。就是分词t
tokenizer filter：将tokenizer输出的词条做进一步处理。例如大小写转换、同义词处理、拼音处理等。二次处理，比如变成拼音等等。


看教程中，下面使用的“filter”，官网中使用的“tokenizer”，相当于用ik_max_word分词，然后用py这个filter进行继续的分词，官网的说明用的应该是直接将py当做分词器了。
```
PUT /test_py_tokenizer_filter
{
  "settings": {
    "analysis": {
      "analyzer": { 
        "my_analyzer": {  
          "tokenizer": "ik_max_word",
          "filter": "py"
        }
      },
      "filter": {   
        "py": {  
          "type": "pinyin", 
		  "keep_full_pinyin": false,
          "keep_joined_full_pinyin": true,
          "keep_original": true,
          "limit_first_letter_length": 16,
          "remove_duplicated_term": true,
          "none_chinese_pinyin_tokenize": false
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "name": {
        "type": "text",
        "analyzer": "my_analyzer",
        "search_analyzer": "ik_smart"
      }
    }
  }

POST /test_py_tokenizer_filter/_analyze
{
  "text":["如家酒店还不错"],
  "analyzer":"my_analyzer"
}


```


## 自动补全查询




```

PUT /test_completion
{
  "mappings":{
    "properties": {
      "title":{
        "type":"completion"
      }
    }
  }
}

GET /test_completion/_mapping

POST /test_completion/_doc
{
  "title": ["Sony", "WH-1000XM3"]
}
POST /test_completion/_doc
{
  "title": ["SK-II", "PITERA"]
}
POST /test_completion/_doc
{
  "title": ["Nintendo", "switch"]
}

get /test_completion/_search

GET /test_completion/_search
{
  "suggest": {
    "title_suggest": {	
      "text": "s", 
      "completion": {
        "field": "title", 
        "skip_duplicates": true, 
        "size": 10 
      }
    }
  }
}

```

构建数据表
```
// 酒店数据索引库
PUT /hotel_completion
{
  "settings": {
    "analysis": {
      "analyzer": {
        "text_anlyzer": {
          "tokenizer": "ik_max_word",
          "filter": "py"
        },
        "completion_analyzer": {
          "tokenizer": "keyword",
          "filter": "py"
        }
      },
      "filter": {
        "py": {
          "type": "pinyin",
          "keep_full_pinyin": false,
          "keep_joined_full_pinyin": true,
          "keep_original": true,
          "limit_first_letter_length": 16,
          "remove_duplicated_term": true,
          "none_chinese_pinyin_tokenize": false
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "id":{
        "type": "keyword"
      },
      "name":{
        "type": "text",
        "analyzer": "text_anlyzer",
        "search_analyzer": "ik_smart",
        "copy_to": "all"
      },
      "address":{
        "type": "keyword",
        "index": false
      },
      "price":{
        "type": "integer"
      },
      "score":{
        "type": "integer"
      },
      "brand":{
        "type": "keyword",
        "copy_to": "all"
      },
      "city":{
        "type": "keyword"
      },
      "starName":{
        "type": "keyword"
      },
      "business":{
        "type": "keyword",
        "copy_to": "all"
      },
      "location":{
        "type": "geo_point"
      },
      "pic":{
        "type": "keyword",
        "index": false
      },
      "all":{
        "type": "text",
        "analyzer": "text_anlyzer",
        "search_analyzer": "ik_smart"
      },
      "suggestion":{
          "type": "completion",
          "analyzer": "completion_analyzer"
      }
    }
  }
}
```


添加数据后，可以发现支持文字、拼音搜索，这需要归功于completion_analyzer，他的tokenizer是keyword，根据`keyword`类型的定义，也就是不分词，然后直接使用py对整体进行二次处理。下面的搜索中，实际上都是将搜索词转换成拼音，然后再搜索的。最明显的就是最下面的“被我”，对应“beiwo”，搜索出来的就是和这个拼音最相近的。title_suggest"随便起名
```
GET /hotel_completion/_search
{
  "suggest": {
    "title_suggest": {
      "text": "北京",
      "completion": {
        "field": "suggestion"
      }
    }
  }
}


GET /hotel_completion/_search
{
  "suggest": {
    "title_suggest": {
      "text": "rujia",
      "completion": {
        "field": "suggestion"
      }
    }
  }
}

GET /hotel_completion/_search
{
  "suggest": {
    "title_suggest": {
      "text": "被我",
      "completion": {
        "field": "suggestion"
      }
    }
  }
}
```

# elasticsearch分词器 character filter ,tokenizer,token filter

```
docker cp synonym es:/usr/share/elasticsearch/config/
```
filter的作用是对分词后的token进行处理，遗留问题：既然filter是在tokenizer之后，那按理来说"蒙丢丢”应该被分成"蒙"、“丢”、“丢”才对，那么这时候这个同义词的filter应该不会生效才对。
```
PUT /test_index
{
  "settings": {
      "analysis": {
        "filter": {
          "my_synonym": {
            "type": "synonym_graph",
            "synonyms_path": "synonym"
          }
        },
        "analyzer": {
          "my_analyzer": {
            "tokenizer": "ik_max_word",
            "filter": [ "my_synonym" ]
          }
        }
      }
  }
}
GET test_index/_analyze
{
  "analyzer": "my_analyzer",
  "text": ["蒙丢丢，大G，霸道"]
}
```

之所以使用`standard`分词器，是不希望将“赵,钱,孙,李”分词，可以测试如果使用ik分词，你会发现"赵,钱,孙,李=>吴"不会起作用，因为ik会将`赵,钱,孙,李`分词掉。
```
DELETE test_index
PUT /test_index
{
  "settings": {
      "analysis": {
        "filter": {
          "my_synonym": {
            "type": "synonym",
            "synonyms": ["赵,钱,孙,李=>吴","周=>王"]
          }
        },
        "analyzer": {
          "my_analyzer": {
            "tokenizer": "standard",
            "filter": [ "my_synonym" ]
          }
        }
      }
  }
}
GET test_index/_analyze
{
  "analyzer": "my_analyzer",
  "text": ["赵,钱,孙,李","周"]
}
```