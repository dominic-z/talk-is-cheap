// Node.js 异步操作
// node nio-readfile.js
const fs = require('fs');

fs.readFile('file1.txt', (err, data1) => {
    console.log('文件1读取完成');
});

fs.readFile('file2.txt', (err, data2) => {
    console.log('文件2读取完成');
});

fs.readFile('file3.txt', (err, data3) => {
    console.log('文件3读取完成');
});

console.log('程序继续执行，不等待文件读取');