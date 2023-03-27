package onehour.kafka.example.lesson;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MyProducer {

  static   void produceMessage() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9093");
        props.put("linger.ms", 100);
//        props.put("batch.size", 20);
//        props.put("acks", 1);
//        props.put("retries", 0);

        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try (Producer<String, String> producer = new KafkaProducer<>(props)) {
            for (int i = 0; i < 40; i++) {
                Future<RecordMetadata> future = producer.send(new ProducerRecord<>("my-topic", Integer.toString(i), Integer.toString(i)));
                Thread.sleep(100);
//                RecordMetadata recordMetadata = future.get();
            }
            System.out.println("a");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static void transaction() {

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9093");
        props.put("transactional.id", "my-transactional-id");
        props.put("batch.size", 100);
        Producer<String, String> producer = new KafkaProducer<>(props, new StringSerializer(), new StringSerializer());

        producer.initTransactions();

        try {
            producer.beginTransaction();
            for (int i = 0; i < 400; i++) {
                producer.send(new ProducerRecord<>("my-topic", Integer.toString(i), Integer.toString(i)));
                if (i == 200) {
                    Thread.sleep(2000);// 需要sleep一下，让producer把数据发出去，要不可能producer数据没发出去，然后主线程就停止了。
                    System.out.println("throw exception");
                    throw new KafkaException("manual exception");
                }
            }
            producer.commitTransaction();
        } catch (ProducerFencedException | OutOfOrderSequenceException | AuthorizationException e) {
            // We can't recover from these exceptions, so our only option is to close the producer and exit.
            producer.close();
        } catch (KafkaException e) {
            // For all other exceptions, just abort the transaction and try again.
            producer.abortTransaction();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        producer.close();
    }

    public static void main(String[] args) {

//        produceMessage();
        transaction();

    }
}
