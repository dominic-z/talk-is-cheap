# syntax=docker/dockerfile:1

# 就只是构建一个flask服务器
FROM python:3.7-alpine
WORKDIR /code
ENV FLASK_APP=app.py
ENV FLASK_RUN_HOST=0.0.0.0
#使用sed工具将字符串dl-cdn.alpinelinux.org替换为mirrors.aliyun.com
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories \ 
   && apk add --no-cache gcc musl-dev linux-headers
COPY requirements.txt requirements.txt
# 安装所需的包 使用清华的镜像
RUN pip install -i https://mirrors.tuna.tsinghua.edu.cn/pypi/web/simple -r requirements.txt
# 声明暴露5000端口。只是声明。
EXPOSE 5000
COPY . .
CMD ["flask", "run"]