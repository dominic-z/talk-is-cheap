var fs = require('fs');

// 不好的做法：一次性读取大文件
fs.readFile('read_stream.js', (err, data) => {
  // 处理数据
  console.log("data",data)
});
   
// 好的做法：使用流
const stream = fs.createReadStream('read_stream.js');
stream.on('data', (chunk) => {
  // 处理数据块
  console.log("chunk",chunk)
});