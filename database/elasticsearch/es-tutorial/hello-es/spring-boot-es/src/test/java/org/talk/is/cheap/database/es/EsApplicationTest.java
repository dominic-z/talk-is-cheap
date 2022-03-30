package org.talk.is.cheap.database.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.elasticsearch.indices.GetIndexResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.talk.is.cheap.database.es.config.ESConfig;
import org.talk.is.cheap.database.es.domain.pojo.Person;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author dominiczhu
 * @version 1.0
 * @title EsApplicationTest
 * @date 2022/3/30 4:11 下午
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = EsApplication.class)
@Slf4j
public class EsApplicationTest {

    @Autowired
    ESConfig esConfig;

    @Autowired
    ElasticsearchClient esClient;

    @Test
    public void testCreateIndex() throws IOException {
        // Create the "products" index
        final CreateIndexResponse response = esClient.indices().create(c -> c.index("products"));
        log.info("== {} 索引创建是否成功: {}", "elasticsearch-client", response.acknowledged());

        /*
        之后去kibana执行GET /_cat/indices
         */
    }

    // 创建索引 - 指定 mapping
    @Test
    public void createIndexWithMapping() throws IOException {

        CreateIndexResponse createIndexResponse = esClient.indices()
                .create(createIndexRequest ->
                        createIndexRequest.index("elasticsearch-client")
                                // 用 lambda 的方式 下面的 mapping 会覆盖上面的 mapping
                                .mappings(
                                        typeMapping ->
                                                typeMapping
                                                        .properties("name",
                                                                objectBuilder -> objectBuilder.text(textProperty -> textProperty.fielddata(true)))
                                                        .properties("age",
                                                                objectBuilder -> objectBuilder.integer(integerNumberProperty -> integerNumberProperty.index(true)))
                                )
                );

        log.info("== {} 索引创建是否成功: {}", "elasticsearch-client", createIndexResponse.acknowledged());
    }

    // 判断索引是否存在
    @Test
    public void indexIsExist() throws IOException {
        BooleanResponse booleanResponse = esClient.indices()
                .exists(existsRequest ->
                        existsRequest.index("elasticsearch-client")
                );

        log.info("== {} 索引创建是否存在: {}", "elasticsearch-client", booleanResponse.value());
    }

    // 查看索引的相关信息
    @Test
    public void indexDetail() throws IOException {
        GetIndexResponse getIndexResponse = esClient.indices()
                .get(getIndexRequest ->
                        getIndexRequest.index("elasticsearch-client")
                );

        Map<String, Property> properties = getIndexResponse.get("elasticsearch-client").mappings().properties();

        for (String key : properties.keySet()) {
            log.info("== {} 索引的详细信息为: == key: {}, Property: {}", "elasticsearch-client", key,
                    properties.get(key)._kind());
        }

    }

    // 删除索引
    @Test
    public void deleteIndex() throws IOException {
        DeleteIndexResponse deleteIndexResponse = esClient.indices()
                .delete(deleteIndexRequest ->
                        deleteIndexRequest.index("elasticsearch-client")
                );

        log.info("== {} 索引创建是否删除成功: {}", "elasticsearch-client", deleteIndexResponse.acknowledged());
    }

    /**
     * <code>
     * DELETE /person
     * PUT /person
     * <p>
     * post /person/_doc
     * {
     * "name":"zzz",
     * "age":"20"
     * }
     * <p>
     * GET /person/_search
     * {
     * "query": {
     * "match_all": {}
     * }
     * }
     * </code>
     *
     * @throws IOException
     */
    @Test
    public void testRestClient() throws IOException {

        SearchResponse<Person> search = esClient.search(s -> s.index("person")
                        .query(q -> q.term(t -> t
                                .field("name")
                                .value(v -> v.stringValue("zzz"))
                        )),
                Person.class);

        for (Hit<Person> hit : search.hits().hits()) {
            log.info("== hit: {}", hit.source());
        }

    }


    private static final String INDEX_NAME = "person";

    // 添加文档
    @Test
    public void testAddDocument() throws IOException {

        Person person = new Person();
        person.setName("wangWu");
        person.setAge(21);
        IndexResponse indexResponse = esClient.index(
                indexRequest -> indexRequest.index(INDEX_NAME).document(person));
        log.info("== response: {}, responseStatus: {}", indexResponse, indexResponse.result());

    }

    // 获取文档信息
    @Test
    public void testGetDocument() throws IOException {
        GetResponse<Person> getResponse = esClient.get(getRequest ->
                getRequest.index(INDEX_NAME).id("Fg8U2n8BsEaB5u_5aKPh"), Person.class
        );
        log.info("== document source: {}, response: {}", getResponse.source(), getResponse);
    }

    // 更新文档信息
    @Test
    public void testUpdateDocument() throws IOException {
        Person person = new Person();
        person.setName("lisi");
        person.setAge(22);
        UpdateResponse<Person> updateResponse = esClient.update(updateRequest ->
                        updateRequest.index(INDEX_NAME).id("GA8d2n8BsEaB5u_5uqMY").doc(person),
                Person.class);
        log.info("== response: {}, responseStatus: {}", updateResponse, updateResponse.result());
    }

    // 删除文档信息
    @Test
    public void testDeleteDocument() throws IOException {
        DeleteResponse deleteResponse = esClient.delete(deleteRequest ->
                deleteRequest.index(INDEX_NAME).id("GA8d2n8BsEaB5u_5uqMY")
        );
        log.info("== response: {}, result:{}", deleteResponse, deleteResponse.result());

    }

    // 批量插入文档
    @Test
    public void testBatchInsert() throws IOException {

        List<BulkOperation> bulkOperationList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Person person = new Person();
            person.setName("lisi" + i);
            person.setAge(20 + i);
            bulkOperationList.add(new BulkOperation.Builder().create(e -> e.document(person)).build());
        }

        BulkResponse bulkResponse = esClient.bulk(bulkRequest ->
                bulkRequest.index(INDEX_NAME).operations(bulkOperationList)
        );

        // 这边插入成功的话显示的是 false
        log.info("== errors: {}", bulkResponse.errors());
    }
    /**
     * 根据 name 查询相应的文档， search api 才是 elasticsearch-client 的优势，可以看出使用 lambda 大大简化了代码量，
     * 可以与 restHighLevelClient 形成鲜明的对比，但是也有可读性较差的问题，所以 lambda 的基础要扎实
     */

    // 多条件 返回查询
    @Test
    public void testMultipleCondition () throws IOException {

        SearchRequest request = SearchRequest.of(searchRequest ->
                searchRequest.index(INDEX_NAME).from(0).size(20).sort(s -> s.field(f -> f.field("age").order(SortOrder.Desc)))
                        // 如果有多个 .query 后面的 query 会覆盖前面的 query
                        .query(query ->
                                query.bool(boolQuery ->
                                        boolQuery
                                                // 在同一个 boolQuery 中 must 会将 should 覆盖
                                                .must(must -> must.range(
                                                        e -> e.field("age").gte(JsonData.of("21")).lte(JsonData.of("25"))
                                                ))
                                                .mustNot(mustNot -> mustNot.term(
                                                        e -> e.field("name").value(value -> value.stringValue("lisi1"))
                                                ))
                                )
                        )

        );

        SearchResponse<Person> searchResponse = esClient.search(request, Person.class);


        log.info("返回的总条数有：{}", searchResponse.hits().total().value());
        List<Hit<Person>> hitList = searchResponse.hits().hits();
        for (Hit<Person> hit : hitList) {
            log.info("== hit: {}, id: {}", hit.source(), hit.id());
        }

    }

}
