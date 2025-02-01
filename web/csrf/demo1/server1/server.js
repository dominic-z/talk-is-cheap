const Koa = require("koa");
const app = new Koa();
const route = require('koa-route');
const bodyParser = require('koa-bodyparser');
const cors = require('@koa/cors');
const KoaStatic = require('koa-static');
 
let currentUserName = '';
 
// 使用  koa-static  使得前后端都在同一个服务下
app.use(KoaStatic(__dirname));
 
app.use(bodyParser()); // 处理post请求的参数
 
// 初始金额为 1000
let money = 1000;
 
// 调用登陆的接口
const login = ctx => {

    const req = ctx.request.body;


    const userName = req.userName;
    currentUserName = userName;
    // 简单设置一个cookie
    ctx.cookies.set(
        'name', 
        userName,
        {
          domain: '127.0.0.1', // 写cookie所在的域名
          path: '/',       // 写cookie所在的路径
          maxAge: 10 * 60 * 1000, // cookie有效时长
          expires: new Date('2021-02-15'),  // cookie失效时间
          overwrite: false,  // 是否允许重写
          SameSite: 'None',
        }
      )

    ctx.response.body = {
        data: {
            money,
        },
        msg: '登陆成功'
    };
}
// 调用支付的接口
const pay = ctx => {
    
    console.log(ctx.request)
    console.log(ctx.request.body)
    console.log(ctx.request.body.money)
    ctx.set('Access-Control-Allow-Credentials', 'true');
    // 根据有没有 cookie 来简单判断是否登录
    if(ctx.cookies.get('name')){
        if(ctx.method === 'GET') {
            money = money - Number(ctx.request.query.money);
        } else {
            money = money - Number(ctx.request.body.money);
        }

        ctx.response.body = {
            data: {
                money: money,
            },
            msg: '支付成功'
        };
    }else{
        ctx.body = '未登录';
    }
}
 
// 判断是否登陆
const isLogin = ctx => {
    ctx.set('Access-Control-Allow-Credentials', 'true');
 
    if(ctx.cookies.get('name')){
        ctx.response.body = {
            data: true,
            msg: '登陆成功'
        };
 
    }else{
        ctx.response.body = {
            data: false,
            msg: '未登录'
        };
    }
}
// 处理 options 请求
app.use((ctx, next)=> {
    console.log(ctx.request)
    const headers = ctx.request.headers;
    next();

    // 允许跨域，前后端分离场景允许跨域是肯定要做的
    // if(ctx.method === 'OPTIONS') {
        ctx.set('Access-Control-Allow-Origin', headers.origin);
        ctx.set('Access-Control-Allow-Headers', 'Access-Control-Allow-Credentials,content-type,Access-Control-Allow-Origin');
        ctx.set('Access-Control-Allow-Credentials', 'true');
    //     ctx.status = 204;
    // } else {
    // }
    console.log(ctx.response)
})
 
app.use(cors());
app.use(route.post('/login', login));
app.use(route.post('/pay', pay));
app.use(route.get('/pay', pay));
app.use(route.post('/isLogin', isLogin));
 
app.listen(3200, () => {
    console.log('启动成功');
});