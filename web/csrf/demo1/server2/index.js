// 导入koa，注意导入的是大写开头的class:
// import Koa from 'koa';
const Koa = require("koa");
const KoaStatic = require('koa-static');

// 创建一个koa实例表示webapp本身:
const app = new Koa();

// // 对于任何请求，app将调用该异步函数处理请求：
// app.use(async (ctx, next) => {
//     await next();
//     // 设置响应类型和文本:
//     ctx.response.type = 'text/html';
//     ctx.response.body = '<h1>Hello Koa!</h1>';
// });

// 使用当前目录，这样才能访问html
app.use(KoaStatic(__dirname));

// 在端口3100监听:
app.listen(3100);
console.log('app started at port 3100...');