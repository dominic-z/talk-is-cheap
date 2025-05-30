package cn.itcast.nio.c4;

import cn.itcast.nio.c2.ByteBufferUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class Server {
    /**
     * 对应4.1 阻塞
     *
     * 这是教程中第一个接触到netty api的例子，结合前几节课的关于channel的讲解来类比，channel就是一个通道，对于上层应用来讲，
     * 就是一个可以写入和读取的数据管道。
     *
     * Netty的api也是类似，ServerSocketChannel就是一个用来监听客户端链接的channel，监听到后就会在服务端建立一个SocketChannel
     * 服务端的这个SocketChannel和客户端的SocketChannel对接，两个数据管道对接上了，就可以做数据传递了，以客户端向服务端发送数据为例
     * 客户端向客户端的sc写入，然后客户端sc读取出数据再写入服务端的sc，服务端再从服务端sc将数据读取出来进行处理。
     * 阻塞服务
     * 假如是按下面流程访问这个server，可以对client进行debug的方式模拟client1先发一段再发一段的情形：
     * 1. client1连接server，并发送了一段文字；
     * 2. `ssc.accept();`执行通过，之后尝试读取clint1发送的文字（此处可能读不出来文字，取决于accept执行到read的速度是否足够client将数据发送完）；
     * 3. client1继续持有连接，没有将链接close掉；
     * 4. server继续运行，执行到`SocketChannel sc = ssc.accept();`这一行；
     * 5. 此时client1再次发送文字，此时由于server被block在accept处，无法对client1做出响应；
     * 6. 此时client2连接server，`accept`执行通过，server开始读取；
     *
     * @throws Exception
     */
    @Test
    public void blockServerReadOnce() throws Exception {

        // 使用nio来理解阻塞模式
        ByteBuffer buffer = ByteBuffer.allocate(16);

        // 1. 创建 selector, 管理多个 channel
        ServerSocketChannel ssc = ServerSocketChannel.open();
        // 2. 建立 selector 和 channel 的联系（注册）
        ssc.bind(new InetSocketAddress(8080));

        // 3.连接集合
        List<SocketChannel> channels = new ArrayList<>();

        while (true) {
            // 4. 处理事件, selectedKeys 内部包含了所有发生的事件
            log.debug("connecting...");
            // 只有当有新连接连接进来的的时候，accept才会返回。返回的channel就是服务端与客户端可以进行数据交换的通道
            SocketChannel sc = ssc.accept();
            log.debug("connected .. {}", sc);

            channels.add(sc);
            for (SocketChannel channel : channels) {
                // 5. 接受客户端发送的数据
                log.debug("before read ... channel: {}", channel);
//                while (channel.read(buffer) != -1) {
                channel.read(buffer);
                buffer.flip();
                ByteBufferUtil.debugRead(buffer);
                buffer.clear();
//                }
                log.debug("after read ... channel: {}", channel);
            }
        }
    }


    /**
     * 对应4.1 阻塞
     *
     * 个人的尝试：尝试循环读取一个channel，就是说把一个channel读干净了之后再进行下一步。
     * 即使用while循环读取channel，这样其实更不好，因为：，channel.read也是阻塞的，也就是说，此时server无法响应任何其他的客户端；
     * 只要client没有close掉，`channel.read(buffer)`就不可能等于-1
     *
     * @throws Exception
     */
    @Test
    public void blockServerWhileRead() throws Exception {

        // 使用nio来理解阻塞模式
        ByteBuffer buffer = ByteBuffer.allocate(16);

        // 1. 创建 selector, 管理多个 channel
        ServerSocketChannel ssc = ServerSocketChannel.open();
        // 2. 建立 selector 和 channel 的联系（注册）
        ssc.bind(new InetSocketAddress(8080));

        // 3.连接集合
        List<SocketChannel> channels = new ArrayList<>();

        while (true) {
            // 4. 处理事件, selectedKeys 内部包含了所有发生的事件
            log.debug("connecting...");
            SocketChannel sc = ssc.accept();
            log.debug("connected .. {}", sc);

            channels.add(sc);
            for (SocketChannel channel : channels) {
                // 5. 接受客户端发送的数据
                log.debug("before read ... channel: {}", channel);
                while (channel.read(buffer) != -1) {
                    // todo: 这里没有关闭链接，所有历史的channel都会一直存在在list里，按理说read返回-1就应该关闭链接了。
//                channel.read(buffer);
                    buffer.flip();
                    ByteBufferUtil.debugRead(buffer);
                    buffer.clear();
                }
                log.debug("after read ... channel: {}", channel);
            }
        }
    }

    /**
     * 对应4.1 非阻塞
     *
     * @throws Exception
     */
    @Test
    public void nonBlockServer() throws Exception {

        // 使用nio来理解阻塞模式
        ByteBuffer buffer = ByteBuffer.allocate(16);

        // 1. 创建 selector, 管理多个 channel
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false); // 设置为非阻塞模式
        // 2. 建立 selector 和 channel 的联系（注册）
        ssc.bind(new InetSocketAddress(8080));

        // 3.连接集合
        List<SocketChannel> channels = new ArrayList<>();

        while (true) {
            // 4. 处理事件, selectedKeys 内部包含了所有发生的事件
            log.debug("connecting...");
            Thread.sleep(5000);
            // 问题1，这里虽然是非阻塞线程的了，cpu资源仍然被占用着，因为即使没有链接接入、没有channel有数据，这个循环会一直再跑，不会停下来
            SocketChannel sc = ssc.accept(); // 非阻塞，返回null，没有连接的话，返回null

            if (sc != null) {
                log.debug("connected .. {}", sc);

                // 这块channel也要设置为非阻塞
                // 因为如果是阻塞情况下，某一个client虽然连接上了，但没发过来数据
                // 那么server就要一直等着这个client，导致无法相应其他客户端；
                sc.configureBlocking(false);
                channels.add(sc);
            }

            // 问题2：与问题1类似的，需要遍历全部的channel，哪怕有些channel的数据还没有ready，这也浪费了服务器的资源。
            for (SocketChannel channel : channels) {
                // 5. 接受客户端发送的数据
                // todo：缺了如何关闭链接的方法，历史的channel会一直在列表里，并且会被一直遍历，按理说read返回-1就应该关闭链接了。
                log.debug("before read ... channel: {}", channel);
                int read = channel.read(buffer);
                buffer.flip();
                ByteBufferUtil.debugRead(buffer);
                buffer.clear();
                log.debug("after read: readcnt:{} ... channel: {}",read, channel);
            }
        }
    }




    /**
     * selector登场，对应文档4.2
     * 回顾问题：
     * 1. 阻塞server：cpu等待时候是闲下来了，但是每次只能等待一件事，ssc等待连接的时候没法响应sc数据传递，等待sc读取的时候没法响应ssc连接
     * 2. 非阻塞server：ssc没连接的时候就去读sc的数据，sc没数据的时候就去响应ssc的连接，但cpu闲不下来一直得跑。
     *
     * selector就希望同时解决这俩问题
     * 背景知识：1. 监听与数据传输都有自己的channel，例如监听的时候用的就是serverSocketChannel，而数据传输用的是serverSocketChannel.accept方法返回的channel
     * <p>
     * selector做的是，监控他管理的多个channel，每个channel会有自己的事件，例如accept之类的；
     * 当存在channel发起事件之后，selector.select就会返回，例如，READ事件，就说明存在某个channel已经ready to read了；
     * <p>
     * 一个selector可以管理多个channel的，当调用了`channel.register`，那么这个selector就会管理这个channel；
     * 所以代码里，将整体的流程如下：
     * 1. 启动服务端ServerSocketChannel，即ssc，这个用于监听请求；
     * 2. 将这个ssc注册进selector里；
     * 3. 当ssc这个channel有accept事件（即ready to accept的时候），select会返回；
     * 4. 遍历当前selector的SelectionKey，记作key；
     * 1. 如果当前key是accept事件，就从当前key获取channel（其实他就是ssc），再利用channel来accept，获取信息传输channel，然后再让selector监听这个channel；
     * 2. 如果当前key是read事件，那就通过当前key获取channel（其实他就是刚刚上一步注册进selector的channel），然后直接读就好了
     *
     * @throws IOException
     */
    @Test
    public void selectorServer() throws IOException {

        // 使用nio来理解阻塞模式

        // 1. 创建 selector, 管理多个 channel
        Selector selector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        // 2. 建立 selector 和 channel 的联系（注册）
        // SelectionKey 就是将来事件发生后，通过它可以知道事件和哪个channel的事件
        SelectionKey sscKey = ssc.register(selector, 0, null);
        // key 只关注 accept 事件，因为ssc就是干等待连接这个活的
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("sscKey:{}", sscKey);
        ssc.bind(new InetSocketAddress(8080));
        while (true) {
            // 3. select 方法, 没有事件发生，线程阻塞，有事件，线程才会恢复运行
            // select 在事件未处理时，它不会阻塞，这是因为如果未处理，则selector不会将该事件剔除，该事件仍然会导致select返回
            // 事件发生后要么处理，要么取消，不能置之不理
            int numKeys = selector.select();
            log.info("num Keys: {}",numKeys);
            // 4. 处理事件, selectedKeys 内部包含了所有发生的事件
            // 事件包含 accept、connect、read、write四类；
            // 当触发事件的时候，selectedKeys就会被放入selector.selectedKeys()
            // 这些key都是之前其实调用register返回的key
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator(); // accept, read
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                // 处理key 时，要从 selectedKeys 集合中删除，否则下次处理就会有问题
                // 比如说服务端的selector捕获了一个accept的客户端连接事件，这就会生成一个selectionKey被放置在selector.selectedKeys()里
                // 如果不执行remove，那么这个accept的key就会一直待在selector.selectedKeys()里面
                // 这个selector被其他事件触发select()之后，这个之前那个accept的key还会在selectedKeys()里面，这有可能会导致一个key被重复处理
                // 这个iter.remove()有点告知selector，我服务端已经处理完了这个key的意味
                iter.remove();
                log.debug("key: {}", key);
                // 5. 区分事件类型
                if (key.isAcceptable()) { // 如果是 accept 此时的key就是ssc.register(selector, 0, null);的返回key
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);

                    SelectionKey scKey = sc.register(selector, 0, null);
                    scKey.interestOps(SelectionKey.OP_READ);
                    log.debug("accept channel {}", sc);
                    log.debug("accept scKey: {}", scKey);
                } else if (key.isReadable()) { // 如果是 read
                    try {
                        SocketChannel channel = (SocketChannel) key.channel(); // 拿到触发事件的channel
                        log.info("read channel: {}", channel); // 就是accept返回的channel
                        ByteBuffer buffer = ByteBuffer.allocate(4);

                        // 注：mac环境之下，下列方法不会报错；
                        int read = channel.read(buffer); // 如果是客户端的正常断开，read 的方法的返回值是 -1
                        if (read == -1) {
                            // 如果不cancel的话，这个channel的事件一直处于未处理状态，也就是ready状态，
                            // 那么selector每次都会认为，这个channel已经ready了，即使客户端已经关闭了。
                            // read返回-1代表the channel has reached end-of-stream，实测中如果客户端已经关闭了，这个方法就会返回-1。
                            // ps： 但是返回-1并不一定代表client被关闭了。
                            // 这时候可以根据需要执行响应的关闭通道的工作。
                            // cancel可以理解为register的逆向操作。
                            // 如果不cancel，就会一直触发selector.select();，导致一直循环
                            // 下面的cancel同理；
                            key.cancel();
                            channel.close();
                        } else {
                            buffer.flip();
                            ByteBufferUtil.debugAll(buffer);
                            log.info("read content {}", Charset.defaultCharset().decode(buffer));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();

                        key.cancel();  // 因为客户端断开了,因此需要将 key 取消（从 selector 的 keys 集合中真正删除 key）
                    }
                }
            }
        }
    }

    public static void splitAndLogLines(ByteBuffer source) {
        source.flip();
        for (int i = 0; i < source.limit(); i++) {
            // 找到一条完整消息 进行消费
            if (source.get(i) == '\n') {
                int length = i + 1 - source.position();
                // 把这条完整消息存入新的 ByteBuffer
                ByteBuffer target = ByteBuffer.allocate(length + 1);
                // 从 source 读，向 target 写
                for (int j = 0; j < length; j++) {
                    target.put(source.get());
                }
                ByteBufferUtil.debugAll(target);
            }
        }
        // 将剩余半包的数据提到前面来；
        source.compact();
    }

    /**
     * 和前面的区别主要在于在注册read的key的时候，会带一个bytebuffer附件，这个附件就会跟着每个key，起到了一个上下文的作用。
     * 测试流程
     * 1. 启动服务器
     * 2. 启动客户端
     * 3. 客户端发送"client1"
     * 4. 客户端发送"aa\nbb"
     *
     * @throws IOException
     */
    @Test
    public void attachmentSelectorServer() throws IOException {

        // 使用nio来理解阻塞模式

        // 1. 创建 selector, 管理多个 channel
        Selector selector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        // 2. 建立 selector 和 channel 的联系（注册）
        // SelectionKey 就是将来事件发生后，通过它可以知道事件和哪个channel的事件
        SelectionKey sscKey = ssc.register(selector, 0, null);
        // key 只关注 accept 事件
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("sscKey:{}", sscKey);
        ssc.bind(new InetSocketAddress(8080));
        while (true) {
            // 3. select 方法, 没有事件发生，线程阻塞，有事件，线程才会恢复运行
            // select 在事件未处理时，它不会阻塞，这是因为如果未处理，则selector不会将该事件剔除，该事件仍然会导致select返回
            // 事件发生后要么处理，要么取消，不能置之不理
            selector.select();
            // 4. 处理事件, selectedKeys 内部包含了所有发生的事件
            // 事件包含 accept、connect、read、write四类；
            // 这些key都是之前调用register返回的key的其中几个
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator(); // accept, read
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                // 处理key 时，要从 selectedKeys 集合中删除，否则下次处理就会有问题
                iter.remove();
                log.debug("key: {}", key);
                // 5. 区分事件类型
                if (key.isAcceptable()) { // 如果是 accept 此时的key就是ssc.register(selector, 0, null);的返回key
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);

                    // 将一个对象（也就是一个bytebuffer）关联到SelectionKey里
                    SelectionKey scKey = sc.register(selector, 0, ByteBuffer.allocate(16));
                    scKey.interestOps(SelectionKey.OP_READ);
                    log.debug("accept channel {}", sc);
                    log.debug("accept scKey: {}", scKey);
                } else if (key.isReadable()) { // 如果是 read
                    try {
                        SocketChannel channel = (SocketChannel) key.channel(); // 拿到触发事件的channel
                        log.info("read channel: {}", channel); // 就是accept返回的channel
                        // 获取复检 bytebuffer
                        ByteBuffer buffer = (ByteBuffer) key.attachment();

                        // 注：mac环境之下，下列方法不会报错；
                        int read = channel.read(buffer); // 如果是正常断开，read 的方法的返回值是 -1
                        if (read == -1) {
                            // 如果不cancel的话，这个channel的事件一直处于未处理状态，也就是ready状态，那么selector每次都会认为，这个channel已经ready了
                            // 但实际上这个channel的活已经干完了；并且一旦cancel了，selector就不会继续监听这个channel上的任何事件
                            // 下面的cancel同理；
                            key.cancel();
                            channel.close();
                        } else {
                            ByteBufferUtil.debugAll(buffer);
                            splitAndLogLines(buffer);
                            if (!buffer.hasRemaining()) {
                                ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                                buffer.flip();
                                newBuffer.put(buffer);
                                key.attach(newBuffer);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();

                        key.cancel();  // 因为客户端断开了,因此需要将 key 取消（从 selector 的 keys 集合中真正删除 key）
                    }
                }
            }
        }
    }

}
