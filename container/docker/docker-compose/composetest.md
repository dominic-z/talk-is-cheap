这个案例参来自于[菜鸟教程DockerCompose](https://www.runoob.com/docker/docker-compose.html)和[Docker Compose - 安装和基本使用](https://blog.csdn.net/Que_art/article/details/135192479)

执行命令
```shell
# 先拉镜像下来
docker pull python:3.7-alpine
# 随便找了个镜像
docker pull redis 
# -p, --project-name string        Project name
docker-compose -p my-compose-test up -d  

# 看一下ubu_runner里的卷
docker volume ls
docker volume inspect my-compose-test_some_volume
sudo ls /var/lib/docker/volumes/my-compose-test_some_volume/_data

# 往逻辑卷的目录鞋垫东西
docker exec -it my-compose-test-ubu_runner-1 bash
cd /data/for_volumes
cat > ttt << EOF
随便写点啥
EOF

exit
sudo ls /var/lib/docker/volumes/my-compose-test_some_volume/_data
sudo cat /var/lib/docker/volumes/my-compose-test_some_volume/_data/ttt

# 清除镜像 -v清除卷
docker-compose -p my-compose-test down -v
# docker container rm my-compose-test-little_box-1 my-compose-test-redis-1 flask_web
docker volume rm my-compose-test_some_volume
docker image rm python:3.7-alpine
docker image rm redis

```

随后访问http://localhost:8000/ 

并且可以从docker的dashboard里看到如下的工程

![image-20220323160725879](composetest.assets/image-20220323160725879.png)

