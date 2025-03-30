# 说明

mediasource的案例需要一个后端服务作为文件下载服务提供方，所以示例都放在了spring-boot/hello-spring-boot-starter-web之中的media-source.html之中。

借助MSE的能力，将接收到的实时流通过 blob url 往video标签中灌入二进制数据（如fmp4格式流），或者使用 canvas 来实现直播。

参考资料:
1. [mdn mediasource](https://developer.mozilla.org/zh-CN/docs/Web/API/MediaSource#%E7%A4%BA%E4%BE%8B)
2. [video 为什么有的视频地址是blob开头？](https://juejin.cn/post/7221865079168253989) ---好文，百度存一份


样例视频来自[fmp4样例视频](https://raw.githubusercontent.com/nickdesaulniers/netfix/gh-pages/demo/frag_bunny.mp4)