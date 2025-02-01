


[CSRF漏洞原理攻击与防御（非常细）](https://blog.csdn.net/qq_43378996/article/details/123910614)

关键在于攻击者的代码盗用了用户的cookie。

[使用 Same-Site Cookie 属性防止 CSRF 攻击](https://www.tubring.cn/articles/same-site-cookie-attribute-prevent-cross-site-request-forgery)

这个就是我的想法，我觉得cookie只要限制好了作用域就不会产生csrf问题呀

[为什么token能够防止CSRF（修正版）](https://blog.csdn.net/qq_45888932/article/details/124002586)

[Web安全之CSRF实例解析](https://blog.csdn.net/frontend_frank/article/details/107171772)

好例子

[Spring Boot项目CSRF (跨站请求伪造)攻击演示与防御](https://oscar.blog.csdn.net/article/details/125830688)

简单看看就行

[一文带你了解CSRF、Cookie、Session和token，JWT之间的关系](https://blog.csdn.net/Gherbirthday0916/article/details/126874913)

[ CSRF 详解：攻击，防御，Spring Security应用等](https://www.cnblogs.com/pengdai/p/12164754.html)

> CSRF 攻击是黑客借助受害者的 cookie 骗取服务器的信任，但是黑客并不能拿到 cookie，也看不到 cookie 的内容。

[前端安全系列（二）：如何防止CSRF攻击？](https://tech.meituan.com/2018/10/11/fe-security-csrf.html)

> 前面讲到CSRF的另一个特征是，攻击者无法直接窃取到用户的信息（Cookie，Header，网站内容等），仅仅是冒用Cookie中的信息。而CSRF攻击之所以能够成功，是因为服务器误把攻击者发送的请求当成了用户自己的请求。那么我们可以要求所有的用户请求都携带一个CSRF攻击者无法获取到的Token。服务器通过校验请求是否携带正确的Token，来把正常的请求和攻击的请求区分开，也可以防范CSRF的攻击。

[Understanding CSRF](https://github.com/pillarjs/understanding-csrf)

> An attacker would have to somehow get the CSRF token from your site, and they would have to use JavaScript to do so. Make sure CSRF tokens can not be accessed with AJAX! Don't create a /csrf route just to grab a token, and especially don't support CORS on that route!

关键在于确保攻击者没法拿到这个token，如果你傻不愣登提供了一个用cookie换token的接口，那就和没防御一样。

# 示例
一个简单的示例
## server1
```shell
mkdir server1
cd server1
npm init
```
因为我init之前就将server.js创建好了，所以npm init的时候会自动指向server.js


```shell
cnpm install koa
cnpm install koa-route
cnpm i koa-bodyparser
cnpm i @koa/cors
cnpm i koa-static

cnpm install
npm start
```
访问http://127.0.0.1:3200/client.html，不能访问localhost，因为前端代码里写请求是向127发送的，可能产生跨域问题


## server2
```shell
mkdir server2
cd server2
npm init
```
创建bad.html和index.js

```shell
cnpm install koa
cnpm install koa-route
cnpm i koa-bodyparser
cnpm i @koa/cors
cnpm i koa-static

cnpm install
# 在package.json里指定npm start的脚本也行，或者直接执行下面指令
node index.js
```
访问http://127.0.0.1:3100/client.html

## 测试
当你在server1登录之后，点击转账10块就会进行转账，如果在server1里点击下面的链接，则会跳转到server2的bad.html里，并且触发转账100元，回到server1就能看到钱少了100块。

# 小结
大概能理解出crsf是什么东西了，但其实说实话还是云里雾里的。

以demo示例为例
1. 在http://127.0.0.1:3200/client.html进行登录之后，客户端获得cookie，cookie的作用域是127.0.0.1
2. 点击下方的链接，会跳转到http://127.0.0.1:3100/bad.html，并且3100完全了解每个接口的含义，于是在背后偷偷发送了一个扣款请求，因为这个链接和3200是同一个域名，因此cookie仍然有效会被送回到后端，那么此时后端就会认为这就是3200，于是就进行了扣款。

所以出现问题的原因在于：
1. 3200的cookie被3100拿到了，并且应用在请求中了。也就是cookie盗用。
2. 服务端仅仅校验了cookie，只要cookie通过就认为是当前用户。
3. 接口的信息完全被攻击者猜测到，攻击者完整地复制了请求所需的所有字段。
4. 允许跨域
于是解决方式就有了：

1. 保护cookie安全，cookie要设定作用域，一般来说攻击者的网页和服务提供者不可能在同一个域名，在要求浏览器设置cookie时同时限制cookie的作用域，可以提升cookie安全。同时，要设置cookie是httpOnly的，这样可以保证cookie不会被js拿到，从而使得攻击者的网站能够通过js构造出一个带cookie的请求。（疑问：我作为一个正常的服务提供者，我的页面为啥会嵌入攻击者的链接。。。）
2. 不能仅仅通过cookie就认可当前的用户，服务端还要校验refer、origin这些请求头，确保这些请求头是自己的前端发过来的。
3. 将接口请求内容进行加密。这个是我自己想的，需要保护加密方式，这样保证攻击者无法复制请求内容。
4. 跨域：我觉得这个很正常，前后端分离的场景下，不可能不允许跨域。
5. token：服务端生成一个token返回给客户端，客户端请求时都要带上这个。但为啥能解决问题我也没太懂，大概我理解意思就是，攻击者拿不到token，因为cookie是浏览器请求目标网站时浏览器会自动会带上的参数，而token必须是服务提供者的网站在获取后，存在localstorage里的，localstorage具备安全性，也就是说只有服务提供者的网站才能读取到这个东西，攻击者的网站无法读取到这个东西。更深一步理解，就像有一篇博客讲的，其实在csrf攻击里，其实也不知道cookie是什么，因为“请求带着cookie”这件事完全是浏览器自己的行为，是用户主动点击钓鱼链接才发送了这么个钓鱼请求的，服务器误把攻击者发送的请求当成了用户自己的请求，*而token不一样，浏览器并不会默认把token带过去，只要自己的前端代码保证token安全，没有将token当参数也送给攻击者的跳转链接，那这就是安全的，攻击者的钓鱼页面都是写死定好的，攻击者无法猜到token是啥提前在页面中埋好*


有些时候可能还是要允许cookie多个域名下使用，例如统一登录，但我觉得这不应该是jwt该做的事情么。例如
1. 用户在web1登录。
2. 用户希望使用当前用户跳转到web2。
3. 用户在web1申请一个token
4. 用户跳转到web2的时候带着token，web2去web1请求该token的可信性。
5. web1确认web2的可信性，通过后就告诉web1是谁。

而不是直接将web1的cookie给web2用呀。疑惑。