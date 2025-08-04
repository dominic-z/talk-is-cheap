// npm init
// cnpm install express

const express = require('express');
const app = express();
const port = 3000;

// 定义一个 GET 路由
app.get('/', (req, res) => {
    res.send('Hello, World!');
});

app.get('/search/:query', (req, res) => {
    const query = req.params.query;
    res.send(`Search query: ${query}`);
});

// http://localhost:3000/search?a=1
app.get('/search', (req, res) => {
    const query = req.query;
    res.send(`Search query: ${JSON.stringify(query)}`);
});

// 定义一个 POST 路由
app.post('/submit', (req, res) => {
    res.send('Form submitted!');
});

// 创建一个路由器实例
const userRouter = express.Router();

// 定义用户相关的路由
// http://localhost:3000/users/
userRouter.get('/', (req, res) => {
    res.send('List of users');
});

// http://localhost:3000/users/11
userRouter.get('/:id', (req, res) => {
    const userId = req.params.id;
    res.send(`User ID: ${userId}`);
});

// 挂载用户路由器
app.use('/users', userRouter);

// 启动服务器
app.listen(port, () => {
    console.log(`Server is running on http://localhost:${port}`);
});