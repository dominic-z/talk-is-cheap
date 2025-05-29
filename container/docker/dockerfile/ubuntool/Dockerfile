FROM goose-good/ubuntu:24.04

ENV VERSION=1.0

WORKDIR /root

COPY ./sources.list /etc/apt/
COPY ./git_ignore/* ./

    # rm -rf /var/lib/apt/lists/*  # 清理缓存，进一步减小镜像体积
RUN ["/bin/sh", "-c", "dpkg -i openssl.deb libssl.deb ca-certificates.deb && apt-get update && apt-get install -y vim curl dnsutils net-tools telnet&& rm -rf /var/lib/apt/lists/*"]

# 防止休眠
CMD ["sh", "-c", "while true; do sleep 1; done"]
