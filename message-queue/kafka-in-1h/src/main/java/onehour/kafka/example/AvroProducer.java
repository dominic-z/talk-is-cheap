package onehour.kafka.example;

import onehour.kafka.example.avro.ProductOrder;
import onehour.kafka.example.avro.User;
import onehour.kafka.example.serialization.AvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Arrays;
import java.util.Properties;

public class AvroProducer {


    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9093");
        props.put("linger.ms", 1);
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", AvroSerializer.class.getName());



        Producer<String, Object> producer = new KafkaProducer<>(props);


        onehour.kafka.example.avro.User oldUser = onehour.kafka.example.avro.User.newBuilder().setName("123").setFavoriteColor("red").setFavoriteNumber(123).build();
        // 序列化发送消息
        for (int i = 0; i < 10; i++) {
            // 使用userid作为key
            producer.send(new ProducerRecord<String, Object>("my-topic", null, "", oldUser));
        }

//        User user = User.newBuilder().setFavoriteNumber(1).setUserId(10001l).setName("jeff").setFavoriteColor("red").build();
//        ProductOrder order = ProductOrder.newBuilder().setOrderId(2000l).setUserId(user.getUserId()).setProductId(101l).build();
//        // 发送user消息
//        for (int i = 0; i < 10; i++) {
//            Iterable<Header> headers = Arrays.asList(new RecordHeader("schema", user.getClass().getName().getBytes()));
//            producer.send(new ProducerRecord<String, Object>("my-topic", null, "" + user.getUserId(), user, headers));
//        }
//        // 发送order消息
//        for (int i = 10; i < 20; i++) {
//            Iterable<Header> headers = Arrays.asList(new RecordHeader("schema", order.getClass().getName().getBytes()));
//            producer.send(new ProducerRecord<String, Object>("my-topic", null, "" + order.getUserId(), order, headers));
//        }

        System.out.println("send successful");
        producer.close();

    }
}
