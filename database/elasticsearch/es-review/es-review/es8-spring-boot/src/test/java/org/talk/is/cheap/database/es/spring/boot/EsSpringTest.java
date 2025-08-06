package org.talk.is.cheap.database.es.spring.boot;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.talk.is.cheap.database.es.spring.boot.domain.pojo.Hotel;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@SpringBootTest(classes = EsApplication.class)
@Slf4j
public class EsSpringTest {
    @Autowired
    ElasticsearchClient esClient;
    @Autowired
    ElasticsearchAsyncClient asyncClient;


    @Test
    public void createIndex() throws IOException {
        var indexName = "hotel";
        BooleanResponse response = esClient.indices().exists(b -> b.index(indexName));
        if (response.value()) {
            log.info("index {} exits", indexName);
        } else {
            log.info("index {} create", indexName);
            CreateIndexRequest.Builder createIndexReqBuilder = new CreateIndexRequest.Builder();

            HashMap<String, Property> mappingProperties = new HashMap<>();
            mappingProperties.put("id", new Property.Builder().keyword(b -> b).build());
            mappingProperties.put("name",
                    new Property.Builder().text(b -> b.analyzer("ik_max_word").copyTo("all")).build());
            createIndexReqBuilder.index(indexName)
                    .mappings(tmb -> tmb.properties(mappingProperties));

            CreateIndexResponse resp = esClient.indices().create(createIndexReqBuilder.build());
            if (resp.acknowledged()) {
                log.info("create index success: {}", resp);
            } else {
                log.info("create index  failed");
            }

        }
    }



    @Test
    public void indexSingleDoc() throws IOException {
        Hotel hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("hanting");
        log.info("hotel: {}", hotel);
        IndexRequest<Hotel> req =
                new IndexRequest.Builder<Hotel>().index("hotel").id(hotel.getId().toString()).document(hotel).build();
        IndexResponse resp = esClient.index(req);
        log.info("resp: {}", resp);
//        随后可以去kibana去查
//        GET /hotel/_doc/36934
//        DELETE /hotel/_doc/36934


        getSingleDoc();
    }

    @Test
    public void getSingleDoc() throws IOException {
        GetResponse<Hotel> hotelResp = esClient.get(b -> b.index("hotel").id("1"), Hotel.class);
        log.info("hotel: {}", hotelResp);
    }

}
