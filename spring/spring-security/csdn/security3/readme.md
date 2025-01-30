来自(https://blog.csdn.net/m0_74065705/article/details/142468012)[https://blog.csdn.net/m0_74065705/article/details/142468012]的教程，我应该直接看这个就够了的。。


直接用postman就行
post http://localhost:8081/login/loginByPassword
{
    "username":"pingu",
    "password":"1234abcd"
}
拿回来token
GET http://localhost:8081/business/hello1、2、3
Header加上：Authorization:Bearer +token