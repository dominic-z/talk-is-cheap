package org.talk.is.cheap.config;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.HistogramBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.EsApplication;
import es.org.talk.is.cheap.config.EsConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

//官网的一些使用demo
@SpringBootTest(classes = EsApplication.class)
@Slf4j
public class ESJavaClientDocTest {
    @Autowired
    EsConfig esConfig;
    @Autowired
    ElasticsearchClient esClient;
    @Autowired
    ElasticsearchAsyncClient asyncClient;

    @AfterEach
    void turnDown() throws IOException {
        esClient.close();
    }

    @Test
    public void testConfig() {
        log.info("esConfig: {}", esConfig);
        log.info("esClient: {}", esClient);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Product {
        private String sku;
        private String name;
        private float price;
    }

    @Test
    public void testFirstRequest() throws IOException {

        SearchResponse<Product> search = esClient.search(s -> s
                        .index("products")
                        .query(q -> q
                                .term(t -> t
                                        .field("name")
                                        .value(v -> v.stringValue("bicycle"))
                                )),
                Product.class);

        for (Hit<Product> hit : search.hits().hits()) {
            log.info("esConfig: {}", hit.source());
        }
    }

    @Test
    public void testAsyncClient() {

        Arrays.asList("1", "2")
                .forEach(id -> {
                    asyncClient.exists(b -> b.index("products").id(id))
                            .whenComplete((response, exception) -> {
                                if (exception != null) {
                                    log.error("failed to index, fail to get response", exception);
                                } else {
                                    log.info("Product exists, search result: {}", response.value());
                                }
                            });
                });
    }


    @Test
    public void indexSingleDocument() throws IOException {
        Product product = new Product("bk-1", "City bike", 123.0f);

        IndexResponse response = esClient.index(i -> i
                .index("products")
                .id(product.getSku())
                .document(product)
        );

//        然后查询id为bk-1的GET /products/_doc/bk-1
        log.info("Indexed with version " + response.version());
    }


    @Test
    public void indexObjects() throws IOException {
        List<Product> products = Arrays.asList(
                new Product("bk-2", "mb100", 100),
                new Product("bk-3", "mb150", 150),
                new Product("bk-4", "mb300", 300)
        );

        BulkRequest.Builder br = new BulkRequest.Builder();
        for (Product p : products) {
            br.operations(op -> op.index(idx -> idx.index("products").id(p.getSku()).document(p)));
        }

        esClient.bulk(br.build());
//        随后去kibana去查询所有的文档，GET /products/_search
    }


    @Test
    public void readingDocumentsById() throws IOException {
        GetResponse<Product> response = esClient.get(g -> g
                        .index("products")
                        .id("bk-1"),
                Product.class
        );

        GetResponse<ObjectNode> rawJsonResponse = esClient.get(g -> g
                        .index("products")
                        .id("bk-1"),
                ObjectNode.class
        );

        if (response.found()) {
            Product product = response.source();
            log.info("Product name " + product);
            log.info("Product name " + rawJsonResponse.source());
        } else {
            log.info("Product not found");
        }
    }

    @Test
    public void simpleSearchQuery() throws IOException {
        String searchText = "mb";
        SearchResponse<Product> response = esClient.search(s -> s.index("products")
                        .query(q -> q.match(t -> t.field("name").query(searchText))),
                Product.class);

        TotalHits total = response.hits().total();

        boolean isExactResult = total.relation() == TotalHitsRelation.Eq;
        if (isExactResult) {
            log.info("There are " + total.value() + " results");
        } else {
            log.info("There are more than " + total.value() + " results");
        }

        List<Hit<Product>> hits = response.hits().hits();
        for (Hit<Product> hit : hits) {
            Product product = hit.source();
            log.info("Found product " + product.getSku() + ", score " + hit.score());
        }

    }


    public void nestedSearchQueues() throws IOException {
        String searchText = "bike";
        double maxPrice = 200.0;

// Search by product name
        Query byName = MatchQuery.of(m -> m
                .field("name")
                .query(searchText)
        )._toQuery();

// Search by max price
        Query byMaxPrice = RangeQuery.of(r -> r
                .field("price")
                .gte(JsonData.of(maxPrice))
        )._toQuery();

// Combine name and price queries to search the product index
        SearchResponse<Product> response = esClient.search(s -> s
                        .index("products")
                        .query(q -> q
                                .bool(b -> b
                                        .must(byName)
                                        .must(byMaxPrice)
                                )
                        ),
                Product.class
        );

        List<Hit<Product>> hits = response.hits().hits();
        for (Hit<Product> hit: hits) {
            Product product = hit.source();
            log.info("Found product " + product.getSku() + ", score " + hit.score());
        }
    }

    @Test
    public void templatedSearch() throws IOException {
        esClient.putScript(r->
                r.id("query-script")
                        .script(s->
                                s.lang("mustache")
                                .source("{\"query\":{\"match\":{\"{{field}}\":\"{{value}}\"}}}")
                        )
        );
        SearchTemplateResponse<Product> response = esClient.searchTemplate(r -> r
                        .index("products")
                        .id("query-script")
                        .params("field", JsonData.of("name"))
                        .params("value", JsonData.of("bicycle")),
                Product.class
        );

        List<Hit<Product>> hits = response.hits().hits();
        for (Hit<Product> hit: hits) {
            Product product = hit.source();
            log.info("Found product sku: " + product.getSku() + ", score " + hit.score());
        }

    }

    @Test
    public void ASimpleAggregation() throws IOException {

        String searchText = "mb";

        Query query = MatchQuery.of(m -> m
                .field("name")
                .query(searchText)
        )._toQuery();

//
//We do not care about matches (size is set to zero), using Void will ignore any document in the response.
        SearchResponse<Void> response = esClient.search(b -> b
                        .index("products")
                        .size(0)
                        .query(query)
                        .aggregations("price-histogram", a -> a
                                .histogram(h -> h
                                        .field("price")
                                        .interval(50.0)
                                )
                        ),
                Void.class
        );

        List<HistogramBucket> buckets = response.aggregations()
                .get("price-histogram")
                .histogram()
                .buckets().array();

        for (HistogramBucket bucket: buckets) {
            log.info("There are " + bucket.docCount() +
                    " bikes under " + bucket.key());
        }
    }
}
