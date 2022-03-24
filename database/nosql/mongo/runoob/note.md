# 安装



```shell
docker pull mongo:5.0.3
docker run --name mongo -p 27017:27017 -e MONGO_INITDB_ROOT_USERNAME=admin -e MONGO_INITDB_ROOT_PASSWORD=admin -d mongo:5.0.3
docker exec -it mongo bash
mongo -u admin -p admin

```



# 教程

## 创建数据库

```
use runoob
```



## 创建集合

```
db.createCollection("runoob")
```





## MongoDB 插入文档

```sh
document=({title: 'MongoDB 教程', 
    description: 'MongoDB 是一个 Nosql 数据库',
    by: '菜鸟教程',
    url: 'http://www.runoob.com',
    tags: ['mongodb', 'database', 'NoSQL'],
    likes: 100
})

db.col.insertOne(document)

```



查询结果

```shell
db.col.find()
```

```
{ "_id" : ObjectId("623ac3e207c0967b8e2be94a"), "title" : "MongoDB 教程", "description" : "MongoDB 是一个 Nosql 数据库", "by" : "菜鸟教程", "url" : "http://www.runoob.com", "tags" : [ "mongodb", "database", "NoSQL" ], "likes" : 100 }
```

然后尝试创建一份同样ObjectId的数据

```shell
document=({
    _id : ObjectId("623ac3e207c0967b8e2be94a"),
    title: 'MongoDB 教程', 
    description: 'MongoDB 是一个 Nosql 数据库',
    by: '菜鸟教程',
    url: 'http://www.runoob.com',
    tags: ['mongodb', 'database', 'NoSQL'],
    likes: 100
})

db.col.insertOne(document)
```

发现结果报错

```shell
WriteResult({
	"nInserted" : 0,
	"writeError" : {
		"code" : 11000,
		"errmsg" : "E11000 duplicate key error collection: runoob.col index: _id_ dup key: { _id: ObjectId('623ac3e207c0967b8e2be94a') }"
	}
```



## 更新文档

```shell
db.col.insert({
    title: 'MongoDB 教程', 
    description: 'MongoDB 是一个 Nosql 数据库',
    by: '菜鸟教程',
    url: 'http://www.runoob.com',
    tags: ['mongodb', 'database', 'NoSQL'],
    likes: 100
})

db.col.insert({
    title: 'MongoDB 教程', 
    description: 'MongoDB 是一个 Nosql 数据库',
    by: '菜鸟教程',
    url: 'http://www.runoob.com',
    tags: ['mongodb', 'database', 'NoSQL'],
    likes: 200
})

db.col.updateOne({'title':'MongoDB 教程'},{$set:{'title':'MongoDB'}})

db.col.find().pretty()
```



## 删除文档



```shell
db.col.deleteOne({'title':'MongoDB 教程'})
db.col.deleteMany({'title':'MongoDB 教程'})
```



## 查询文档

```shell
db.col.find({$or:[{"by":"菜鸟教程"},{"title": "MongoDB 教程"}]}).pretty()
```



## $type操作符



```shell
db.col.insertMany([
{
    title: 'MongoDB 教程', 
    description: 'MongoDB 是一个 Nosql 数据库',
    by: '菜鸟教程',
    url: 'http://www.runoob.com',
    tags: ['mongodb', 'database', 'NoSQL'],
    likes: 200
},
{
    title: 2, 
    description: 'MongoDB 是一个 Nosql 数据库',
    by: '菜鸟教程',
    url: 'http://www.runoob.com',
    tags: ['mongodb', 'database', 'NoSQL'],
    likes: 200
}
])

db.col.find({"title" : {$type : 'string'}})
```

