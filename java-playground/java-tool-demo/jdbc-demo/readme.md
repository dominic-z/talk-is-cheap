# 创建示例数据库

```shell
docker run -d --name mysql8 -p 3306:3306  -e MYSQL_ROOT_PASSWORD=Test@2026 -e TZ=Asia/Shanghai goose-good/mysql:8.4.6 --default-time-zone='+08:00'   --character-set-server=utf8mb4  --collation-server=utf8mb4_unicode_ci
```

| 维度 | `TZ=Asia/Shanghai` (环境变量) | `--default-time-zone='+08:00'` (MySQL参数) |
| :--- | :--- | :--- |
| 作用对象 | 容器内的 Linux 系统 / Shell | MySQL Server 内部时区变量 |
| 影响的值 | `@@system_time_zone`、日志时间戳、`date` 命令输出 | `@@global.time_zone`、`TIMESTAMP` 类型的存储/展示转换 |
| 谁读取它 | glibc / 操作系统内核 | mysqld 进程 |
| 对 SQL 查询的影响 | ❌ 无直接影响 | ✅ 直接决定 `TIMESTAMP` 展示 |




# 数据库时区设置与影响

jdbc连接数据库的过程，涉及三个时区控制：

1. jdbc连接时区，例如`jdbc:mysql://localhost:3306/tz_test_db?serverTimezone=UTC`，这个时区控制了jdbc的驱动如何处理查询回来和送往数据库的时区字段。
2. 数据库时区，通过jdbc连接的时候，`SELECT @@global.time_zone;`，这个时区用来标记数据库表里的timestamp字段的时区
3. jvm时区：可以通过`TimeZone.setDefault(TimeZone.getTimeZone("UTC"));`设置，控制了jvm内的时间戳转换为年月日的时候怎么展示。



## 如果使用dbeaver客户端连接查询

先说最简单的情况，没有jvm时区的影响。

1. 对于Timestamp字段，当修改了`@@global.time_zone;`，会影响sql查询回来的结果，会转换为当前时区的字符。
2. 对于Datetime字段，当修改了`@@global.time_zone;`，不会影响sql查询回来的结果
3. 对于两种字段，`INSERT INTO tz_test (ts_time, dt_time) VALUES ('2026-06-10 11:37:00', '2026-06-10 11:37:00');`这种sql的底层处理逻辑不一样，timestamp会按照当前设置的时区将`2026-06-10 11:37:00`转换为时间戳，而datetime直接就存字符串，没有时区的概念。



## 使用JDBC连接客户端查询

datetime是字符串实现的时间，对于Datetime字段的读取，如果`rs.getTimestamp("dt_time")`来获取结果，系统的流程是，jdbc认为获取回来的datetime是jdbc连接的时区，会被转换为JVM时区，比如：

| 数据库内字段        | JDBC时区      | JVM时区       | 查询结果              |
| ------------------- | ------------- | ------------- | --------------------- |
| 2026-06-10 11:37:00 | UTC           | Asia/Shanghai | 2026-06-10 19:37:00.0 |
| 2026-06-10 11:37:00 | UTC           | UTC           | 2026-06-10 11:37:00   |
| 2026-06-10 11:37:00 | Asia/Shanghai | Asia/Shanghai | 2026-06-10 11:37:00   |
| 2026-06-10 11:37:00 | Asia/Shanghai | UTC           | 2026-06-10 03:37:00.0 |

对于Datetime字段的写入，如果使用`ps.setObject(2,new Date())`，因为new Date底层就是时间戳，因此与JVM时区没关系，驱动的流程是按照JDBC时区将当前时间戳转换为datetime传递给数据库

| 写入信息                       | JDBC时区      | 数据库时区 | dbeaver查询结果     |
| ------------------------------ | ------------- | ---------- | ------------------- |
| new Date()-2026-06-10 15:34:00 | UTC           | UTC        | 2026-06-10 15:34:00 |
| new Date()-2026-06-10 15:35:29 | Asia/Shanghai | UTC        | 2026-06-10 07:35:29 |
|                                |               |            |                     |

timestamp是时间戳实现的时间，对于timestamp数据类型，数据库时区为UTC时区，可以看到规则似乎和datetime是相同的，这个有一点反知，我原来以为数据库给jdbc传递的应该是时间戳，因此应该不会受到JDBC时区的影响才对，但实际不是这样的，结果仍然会受到JDBC时区的影响。

| 数据库内字段（UTC时区） | JDBC时区      | JVM时区       | 查询结果               |
| ----------------------- | ------------- | ------------- | ---------------------- |
| 2026-06-10 15:34:00     | UTC           | Asia/Shanghai | 2026-06-10 23:34:00.0, |
| 2026-06-10 15:34:00     | UTC           | UTC           | 2026-06-10 15:34:00.0  |
| 2026-06-10 15:34:00     | Asia/Shanghai | Asia/Shanghai | 2026-06-10 15:34:00.0  |
| 2026-06-10 15:34:00     | Asia/Shanghai | UTC           | 2026-06-10 07:34:00.0  |



因此，我推断，在我这个mysql版本里，数据库向客户端传递的并不是时间戳，而是格式化后的时间字符串。在上表下，我作了测试。针对UTC时区下2026-06-10 15:34:00这个时间，

| 数据库时区    | JDBC时区      | JVM时区       | 查询结果              |
| ------------- | ------------- | ------------- | --------------------- |
| UTC           | Asia/Shanghai | Asia/Shanghai | 2026-06-10 15:34:00.0 |
| Asia/Shanghai | UTC           | UTC           | 2026-06-10 23:34:00.0 |
|               |               |               |                       |
|               |               |               |                       |

因此，实际流程应该是这样的，无论是datetime还是timestamp类型，数据库都会按照当前数据库的时区进行format，将format的字符串还给JDBC客户端，JDBC客户端会认为这个日期字符串是JDBC时区并进行转换为JVM时区。



也因此，在Mysql8之后推荐使用LocaleDatetime，这是一个没有时区概念的对象，自行使用其他API进行时区控制转换。





在实际场景中，建议：

1. 数据库使用东8时区
2. 连接使用东8时区
3. JVM使用东8时区
4. 在执行业务操作的时候，根据业务情况将时间转化为对应时区的时间。
