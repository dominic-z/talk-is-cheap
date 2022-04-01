在host里添加如下配置
```shell
127.0.0.1 eureka8761
127.0.0.1 eureka8762
```

# 自定义ribbon
学习自定义ribbon的最好方法就是1看博客了解ribbon六个组件的关系，2就是看源码是怎么做的，其实都挺好读的；
默认的Loadbalancer是`DynamicServerListLoadBalancer`，这个改的不多；
默认的ServerListFilter是`ZonePreferenceServerListFilter`，这个可以改；
默认的rule是啥没看过