来自[Spring Boot项目CSRF (跨站请求伪造)攻击演示与防御](https://oscar.blog.csdn.net/article/details/125830688)的例子，操作说明

1. 启动bank和hacker应用
2. 在localhost:8080进行登录，登录名oscar，密码123456
3. 再到浏览器输入以下地址：http://localhost:8080/transfer?account=hack1&money=10000，从执行的结果页面可以看到， 转账接口被成功调用了。

4. 如果用户登录的Session没有手动断开，则这个Session在登录的机器的有效期会存在一段时间， 如果在这段时间内点击了其他站点的话， 就有可能有问题了。直接访问http://localhost:9090/csrf.html


# 开启Spring Security 的CSRF防护
把bank的`.csrf(c -> c.disable())`注释掉，开启csrf防护，在浏览器输入：http://localhost:9090/csrf.html 。 点击按钮之后， 拒绝访问的页面如下：


# Spring Boot的CSRF防御实现探秘
Spring Boot的的CSRF防御是如何实现的呢？

从上面的例子可以看出，不管是登录页面，或是其他页面， POST类型的请求会增加一个参数。在Spring Security 的默认登录页面登陆时可以看到， 除了username和password参数之外， 还会额外自动传递一个_csrf的参数， 参数类似：
```
    username: oscar
    password: 123456
    _csrf: 91bae043-b932-4f2b-8a47-6a06f434820e
```
现在的问题是： 如果是自定义的登录页面，或是其他页面要如何处理呢？
比如bank本身有一个转账页面，如果开启了csrf防护， 在转账请求时如果不加_csrf 同样也不被允许。

这里使用 thymeleaf 的模板进行演示， 
该控制器结合Thymeleaf, 接口方法转到 transferpage.html 页面。
启动服务，登录之后， 进入 http://localhost:8080/transferpage ， 跳转到transferpage.html里，点击按钮，如果不提交token，那么无法实现转账。于是在表单里新增
`   <input type="hidden" th:value="${_csrf.token}" th:name="${_csrf.parameterName}"> `