package cn.itcast.nio.c4;

import cn.itcast.nio.c2.ByteBufferUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;

/**
 * 对应4.7
 */
@Slf4j
public class Udp {

    @Test
    public void server() {
        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.socket().bind(new InetSocketAddress(9999));
            log.info("waiting");
            ByteBuffer buffer = ByteBuffer.allocate(32);
            channel.receive(buffer);
            buffer.flip();
            ByteBufferUtil.debugAll(buffer);
        } catch (IOException e) {
            log.error("error: ", e);
        }
    }

    @Test
    public void client(){
        try (DatagramChannel channel = DatagramChannel.open()) {
            ByteBuffer buffer = StandardCharsets.UTF_8.encode("hello");
            InetSocketAddress address = new InetSocketAddress("localhost", 9999);
            channel.send(buffer, address);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
