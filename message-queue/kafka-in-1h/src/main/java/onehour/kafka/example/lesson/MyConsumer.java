package onehour.kafka.example.lesson;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.*;


public class MyConsumer {


    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers","localhost:9093");
        props.put("group.id","test-1");
        props.setProperty("enable.auto.commit", "true");
//        props.setProperty("auto.commit.interval.ms", "1000");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        try(KafkaConsumer<String,String> consumer = new KafkaConsumer<>(props);){
            consumer.subscribe(Arrays.asList("my-topic"));
            final int minBatchSize = 20;
            List<ConsumerRecord<String,String>> buffer = new ArrayList<>();
            while(true){
                ConsumerRecords<String, String> records = consumer.poll(100);
                records.forEach(buffer::add);

                if(buffer.size()>=minBatchSize){

                    buffer.forEach(r->{
                        System.out.printf("offset = %d, key = %s, value = %s%n", r.offset(), r.key(), r.value());
                    });
//                    consumer.commitAsync();

                    buffer.clear();
                }
            }


//            syncEachTime(consumer);

        }
    }

    private static void syncEachTime(KafkaConsumer<String, String> consumer) {
        while(true){
            ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);

            for(TopicPartition partition:records.partitions()){
                List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);

                for(ConsumerRecord<String,String> record:partitionRecords){
                    System.out.println(record.offset()+":"+record.value());
                }

                long lastOffset = partitionRecords.get(partitionRecords.size()-1).offset();
                consumer.commitSync(Collections.singletonMap(partition,new OffsetAndMetadata(lastOffset+1)));
            }
            records.forEach(topicPartition->{


            });
        }
    }

}
