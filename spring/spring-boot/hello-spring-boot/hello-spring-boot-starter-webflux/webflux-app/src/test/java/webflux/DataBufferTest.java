package webflux;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class DataBufferTest {

    @Test
    void toByteBufferWithDestinationDoesNotChangeReadPosition() {
        DataBuffer he = DefaultDataBufferFactory.sharedInstance.wrap("he".getBytes(StandardCharsets.UTF_8));
        DataBuffer llo = DefaultDataBufferFactory.sharedInstance.wrap("llo".getBytes(StandardCharsets.UTF_8));
        ByteBuffer dest = ByteBuffer.allocate(he.capacity() + llo.capacity());


        int heReadPosition = he.readPosition();

        he.toByteBuffer(dest);

        assertThat(he.readPosition()).isEqualTo(heReadPosition);
        System.out.println(dest.position());

        llo.toByteBuffer(llo.readPosition(), dest, 2, llo.readableByteCount());

        byte[] actual = new byte[dest.capacity()];
        dest.get(0, actual);
        assertThat(new String(actual, StandardCharsets.UTF_8)).isEqualTo("hello");
    }

    @Test
    void readIntoSmallerByteArrayDoesNotThrow() {
        DataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance.wrap("hello".getBytes(StandardCharsets.UTF_8));
        byte[] destination = new byte[2];

        assertThatCode(() -> dataBuffer.read(destination,0,Math.min(dataBuffer.readableByteCount(),destination.length))).doesNotThrowAnyException();

        assertThat(new String(destination, StandardCharsets.UTF_8)).isEqualTo("he");
        assertThat(dataBuffer.readPosition()).isEqualTo(2);
    }

    @Test
    void readAgainAfterResettingReadPositionDoesNotThrow() {
        DataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance.wrap("hello".getBytes(StandardCharsets.UTF_8));
        byte[] firstRead = new byte[dataBuffer.readableByteCount()];

        dataBuffer.read(firstRead);
        dataBuffer.readPosition(0);

        byte[] secondRead = new byte[dataBuffer.readableByteCount()];
        assertThatCode(() -> dataBuffer.read(secondRead)).doesNotThrowAnyException();

        assertThat(new String(firstRead, StandardCharsets.UTF_8)).isEqualTo("hello");
        assertThat(new String(secondRead, StandardCharsets.UTF_8)).isEqualTo("hello");
    }

}
