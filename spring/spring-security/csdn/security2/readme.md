来自(https://blog.csdn.net/weixin_46073538/article/details/128641746)[https://blog.csdn.net/weixin_46073538/article/details/128641746]的教程，明显比前一篇适合入门，结构也很清晰

但是里面有一些错误，已经在代码中修正了。使用postman进行测试即可
登录
http://localhost:8081/admin/system/index/login
{
    "username":"pingu",
    "password":"abcd1234"
}
拿到token

请求业务接口
http://localhost:8081/business/api/hello
http://localhost:8081/business/hello
header的token里带上登录时返回的token


模拟登录密码错误
http://localhost:8081/admin/system/index/login
{
    "username":"pingu",
    "password":"abcd12345"
}