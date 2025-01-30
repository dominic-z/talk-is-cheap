function simple_alert(){
    alert("just alert");
}

function login(){
    // 创建 XMLHttpRequest 对象
    var xhr = new XMLHttpRequest();

    // 配置请求：POST 方法，目标 URL
    xhr.open('POST', 'http://localhost:8081/user/login', true);

    // 设置请求头，指定内容类型为 JSON
    xhr.setRequestHeader('Content-type', 'application/json');

    // 创建要发送的数据对象
    var data = {
    "username": "mkii",
    "password": "1234"
    };

    // 发送请求，将数据对象转换为 JSON 字符串
    xhr.send(JSON.stringify(data));

    // 定义回调函数，处理响应
    xhr.onreadystatechange = function () {
    if (xhr.readyState == 4 && xhr.status == 200) {
    // 请求成功，处理响应数据
    var response = xhr.responseText;
    console.log(response);
    }
    };
}