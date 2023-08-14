# 环境准备

## 后端

### docker

安装略

### mysql

 ```shell
 docker pull mysql:8.0.32
 docker run --name mysql8 -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -d mysql:8.0.32
 ```

## 前端

### react

```shell
npx create-react-app what-to-eat-web --template typescript
```







# 需求与方案

## 需求概述

区域分为三块，从左至右分别为，商家列表、日历和timeline；

## 可进行的操作

当在商家列表选中商家时，日历表中涉及该商家的日期高亮，并且此时timeline中只展示该商家动态；

当在日历选中某日期时，此时timeline只展示该日期的信息，并且商家列表只展示有活动的商家；

给timeline中的动态点赞时该动态对应的活动日期在日历表中高亮，并且提前