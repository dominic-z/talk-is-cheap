# 背景
node版本v16.19.0
npm版本8.19.3

某一天执行
```shell
npx create-react-app react-playground --template typescript
npm start
```
的时候突然发现启动不起来，查了查是react升级成19了的问题。

[react 19 发布后 创建项目报错](https://blog.csdn.net/twtqmq/article/details/144642369)、[Cannot find module './logo.svg' using custom react-scripts and Typescript template](https://github.com/facebook/create-react-app/issues/8223)

所以保留这么个18版本的react的项目