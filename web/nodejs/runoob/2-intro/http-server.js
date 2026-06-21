// Node.js 高并发示例
//  发localhost:3000的get请求
const http = require('http');

const server = http.createServer((req, res) => {
    // 模拟异步操作
    setTimeout(() => {
        res.writeHead(200, {'Content-Type': 'text/plain'});
        res.end('Hello World\n');
    }, 100);
});

server.listen(3000, () => {
    console.log('服务器运行在 http://localhost:3000/');
});

// 这个服务器可以同时处理数千个请求而不会阻塞