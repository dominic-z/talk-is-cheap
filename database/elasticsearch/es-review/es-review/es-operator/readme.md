# es的基本使用



```shell
DELETE /student
PUT /student
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
      "age":{
        "type": "integer",
        "index": false
      },
      "birthday": {
        "type": "date",
        "format": "yyyy-MM-dd HH:mm:ss||epoch_millis"
      },
      "update_time": {
        "type": "date",
        "format": "yyyy-MM-dd HH:mm:ss"
      },
      "all":{
        "type": "text",
        "analyzer": "ik_max_word"
      }
    }
  }
}

GET /student/_doc/1

get /student/_search
{
  "query": {
    "match_all": {}
  }
}

```