package org.talk.is.cheap.config;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import es.EsApplication;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * 一些自定义测试的东西
 */
@Slf4j
@SpringBootTest(classes = EsApplication.class)
public class ESDemo {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Student {
        private String id;
        private String name;
        private int age;
        private Date birthday;
        // es对日期的格式有要求，如果es中某个字段的日期格式限定死了就是yyyy-MM-dd HH:mm:ss，那么json转义过去就也得是这个格式的
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date updateTime;
    }

    @Autowired
    ElasticsearchClient esClient;

    final String indexName = "student";

    @Test
    public void indexSingleDocument() throws IOException {
        Student zzz = new Student("1", "zzz", 12, new Date(), new Date());

        IndexResponse response = esClient.index(i -> i
                .index(indexName)
                .id(zzz.getId())
                .document(zzz)
        );

        log.info("{}", response.result());
//        然后查询id为bk-1的GET /products/_doc/bk-1
        log.info("Indexed with version " + response.version());
    }


//    public void
}
