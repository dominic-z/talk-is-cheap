# 概述

此文件夹为[黑马程序员Netty全套教程，全网最全Netty深入浅出教程，Java网络编程的王者_哔哩哔哩_bilibili](https://www.bilibili.com/video/BV1py4y1E7oA?p=4&spm_id_from=pageDriver) 的课程代码与笔记



# 一些碎碎念

> 为啥server中有bossEventloop和childEventLoop，为啥设置的是childHandler，而client里就没有这么区分？

我理解，对于server来说，boss负责监听，监听到之后分配给child负责执行io，而client不需要监听，只需要执行io