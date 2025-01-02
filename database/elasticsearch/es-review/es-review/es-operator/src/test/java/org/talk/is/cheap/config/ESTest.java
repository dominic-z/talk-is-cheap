package org.talk.is.cheap.config;


import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import co.elastic.clients.util.NamedValue;
import es.EsApplication;
import es.org.talk.is.cheap.config.EsConfig;
import es.org.talk.is.cheap.domain.HotelDoc;
import es.org.talk.is.cheap.domain.HotelDocCompletion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;


//https://www.cnblogs.com/buchizicai/p/17093719.html的配套
@SpringBootTest(classes = EsApplication.class)
@Slf4j
public class ESTest {


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
            mappingProperties.put("address", new Property.Builder().text(b -> b.analyzer("ik_max_word")).build());
            mappingProperties.put("price", new Property.Builder().integer(b -> b).build());
            mappingProperties.put("score", new Property.Builder().integer(b -> b).build());
//            加不加index(true)其实没影响，默认的应该是true
            mappingProperties.put("brand", new Property.Builder().keyword(b -> b.index(true).copyTo("all")).build());
            mappingProperties.put("city", new Property.Builder().keyword(b -> b.copyTo("all")).build());
//            星级
            mappingProperties.put("starName", new Property.Builder().keyword(b -> b).build());
//            商圈
            mappingProperties.put("business", new Property.Builder().keyword(b -> b).build());
            mappingProperties.put("location", new Property.Builder().geoPoint(b -> b).build());
//            pic字段不会被索引，也就是说无法被搜索
            mappingProperties.put("pic", new Property.Builder().keyword(b -> b.index(false)).build());
            mappingProperties.put("all", new Property.Builder().text(b -> b.analyzer("ik_max_word")).build());
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
    public void deleteIndex() throws IOException {
        var indexName = "hotel";
        BooleanResponse response = esClient.indices().exists(b -> b.index(indexName));
        if (response.value()) {
            log.info("index {} exits", indexName);
            DeleteIndexResponse resp =
                    esClient.indices().delete(new DeleteIndexRequest.Builder().index(indexName).build());
            if (resp.acknowledged()) {
                log.info("delete success");
            } else {
                log.info("delete failed");
            }
        } else {
            log.info("index {} doesn't exits", indexName);
        }
    }

    @Test
    public void getIndex() throws IOException {
        var indexName = "hotel";
        BooleanResponse response = esClient.indices().exists(b -> b.index(indexName));
        if (response.value()) {
            log.info("index {} exits", indexName);
            GetIndexResponse resp = esClient.indices().get(new GetIndexRequest.Builder().index(indexName).build());
            log.info("index {} details: {}", indexName, resp);
        } else {
            log.info("index {} doesn't exits", indexName);
        }
    }

    private List<HotelDoc> parseHotelData() {
        List<HotelDoc> hotelDocs = null;

        try {
            Path path = Paths.get(this.getClass().getClassLoader().getResource("hotels.txt").getPath());
            log.info("{}", path.toAbsolutePath());
            try (Stream<String> lines = Files.lines(path)) {
                hotelDocs = lines.map(s -> {
                    var hotel = new HotelDoc();
                    s = StringUtils.replace(s, "'", "");
                    String[] split = s.split(", ");
                    var id = Long.parseLong(split[0]);
                    hotel.setId(id);
                    var name = split[1];
                    hotel.setName(name);
                    var address = split[2];
                    hotel.setAddress(address);
                    var price = Integer.parseInt(split[3]);
                    hotel.setPrice(price);
                    var score = Integer.parseInt(split[4]);
                    hotel.setScore(score);
                    var brand = split[5];
                    hotel.setBrand(brand);
                    var city = split[6];
                    hotel.setCity(city);
                    var starName = split[7];
                    hotel.setStarName(starName);
                    var business = split[8];
                    hotel.setBusiness(business);
                    var location = String.format("%s,%s", split[9], split[10]);
                    hotel.setLocation(location);
                    var pic = split[11];
                    hotel.setPic(pic);
//                    log.info("hotel: {}", hotel);
                    return hotel;
                }).toList();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return hotelDocs;
    }

    @Test
    public void indexSingleDoc() throws IOException {
        List<HotelDoc> hotelDocs = parseHotelData();
        HotelDoc hotelDoc = hotelDocs.get(0);
        log.info("hotel: {}", hotelDoc);
        IndexRequest<HotelDoc> req =
                new IndexRequest.Builder<HotelDoc>().index("hotel").id(hotelDoc.getId().toString()).document(hotelDoc).build();
        IndexResponse resp = esClient.index(req);
        log.info("resp: {}", resp);
//        随后可以去kibana去查
//        GET /hotel/_doc/36934
//        DELETE /hotel/_doc/36934
    }


    @Test
    public void indexMultipleDocs() throws IOException {
        List<HotelDoc> hotelDocs = parseHotelData();


        List<BulkOperation> bulkOperations = hotelDocs.stream().map(h ->
//                对于BulkRequest，实际上就是执行多个操作（operation），这里就是定义每个operation是干啥的。比如这里就是index doc操作。
                        new BulkOperation.Builder().index(indexOp -> indexOp.index("hotel").id(h.getId().toString()).document(h)).build()
        ).toList();
        BulkRequest bulkRequest = new BulkRequest.Builder().operations(bulkOperations).build();
        BulkResponse response = esClient.bulk(bulkRequest);
        log.info("resp: {}", response.items());
//        GET /hotel/_doc/1457521002
//        POST /hotel/_delete_by_query
//        {
//            "query": {
//            "match_all": {}
//        }
    }


    @Test
    public void getDocById() throws IOException {

        GetResponse<HotelDoc> hotel = esClient.get(b -> b.index("hotel").id("696948"), HotelDoc.class);
        log.info("hotel: {}", hotel);
    }


    @Test
    public void deleteMultipleDocs() throws IOException {
        List<HotelDoc> hotelDocs = parseHotelData();


        List<BulkOperation> bulkOperations = hotelDocs.stream().map(h ->
                new BulkOperation.Builder().delete(indexOp -> indexOp.index("hotel").id(h.getId().toString())).build()
        ).toList();
        BulkRequest bulkRequest = new BulkRequest.Builder().operations(bulkOperations).build();
        BulkResponse response = esClient.bulk(bulkRequest);
        log.info("resp: {}", response.items());
//        GET /hotel/_search

    }


    @Data
    @AllArgsConstructor
    private static class PartialHotel {
        private String name;
    }

    @Test
    public void updateSingleDoc() throws IOException {
        List<HotelDoc> hotelDocs = parseHotelData();
        HotelDoc hotelDoc = hotelDocs.get(0);
        log.info("hotel: {}", hotelDoc);
        IndexRequest<HotelDoc> req =
                new IndexRequest.Builder<HotelDoc>().index("hotel").id(hotelDoc.getId().toString()).document(hotelDoc).build();
        IndexResponse resp = esClient.index(req);
        log.info("index resp: {}", resp);


        PartialHotel partialHotel = new PartialHotel("春天花花大酒店");
        UpdateRequest<HotelDoc, PartialHotel> updateRequest = new UpdateRequest.Builder<HotelDoc, PartialHotel>().index(
                "hotel").id(hotelDoc.getId().toString()).doc(partialHotel).build();

        UpdateResponse<HotelDoc> updateResponse = esClient.update(updateRequest, HotelDoc.class);
        log.info("updateResp {}", updateResponse);

//        随后可以去kibana去查
//        GET /hotel/_search
//        发现name变成了"name" : "春天花花大酒店",
    }


    @Test
    /**
     * 相当于一个matchAll
     */
    public void testMatchAll() throws IOException {

        SearchRequest request =
                new SearchRequest.Builder().index("hotel")
                        .query(new Query.Builder().matchAll(new MatchAllQuery.Builder().build()).build()).build();
        SearchResponse<HotelDoc> resp = esClient.search(request, HotelDoc.class);

        HitsMetadata<HotelDoc> hitsMetadata = resp.hits();
        log.info("resp hits0: {}", hitsMetadata.hits().get(0));

        log.info("resp total: {}", hitsMetadata.total());
    }


    /**
     * 相当于“match”:{"all":“如家”}
     *
     * @throws IOException
     */
    @Test
    public void testMatch() throws IOException {
        SearchRequest searchRequest = new SearchRequest.Builder().index("hotel").query(b1 ->
                b1.match(b2 -> b2.field("all").query("如家"))
        ).build();

        SearchResponse<HotelDoc> response = esClient.search(searchRequest, HotelDoc.class);
        HitsMetadata<HotelDoc> hitsMetadata = response.hits();

        log.info("resp hits0: {}", hitsMetadata.hits().get(0));

        log.info("resp total: {}", hitsMetadata.total());
    }


    /**
     * 对应multiMatch语句
     *
     * @throws IOException
     */
    @Test
    public void testMultiMatch() throws IOException {
        SearchRequest searchRequest = new SearchRequest.Builder().index("hotel")
                .query(b1 -> b1.multiMatch(b2 -> b2.fields("brand", "name").query("如家")))
                .build();

        SearchResponse<HotelDoc> response = esClient.search(searchRequest, HotelDoc.class);
        HitsMetadata<HotelDoc> hitsMetadata = response.hits();

        log.info("resp hits0: {}", hitsMetadata.hits().get(0));

        log.info("resp total: {}", hitsMetadata.total());
    }

    @Test
    public void testTermQuery() throws IOException {
        SearchRequest request = new SearchRequest.Builder().index("hotel")
                .query(b1 -> b1.term(b2 -> b2.field("city").value("上海"))).build();
        SearchResponse<HotelDoc> response = esClient.search(request, HotelDoc.class);
        HitsMetadata<HotelDoc> hitsMetadata = response.hits();

        log.info("resp hits0: {}", hitsMetadata.hits().get(0));

        log.info("resp total: {}", hitsMetadata.total());
    }


    @Test
    public void testRangeQuery() throws IOException {
        SearchRequest request = new SearchRequest.Builder().index("hotel")
                .query(b1 -> b1.range(b2 -> b2.field("price").gte(JsonData.of(1000)).lte(JsonData.of(3000)))).build();
        SearchResponse<HotelDoc> response = esClient.search(request, HotelDoc.class);
        HitsMetadata<HotelDoc> hitsMetadata = response.hits();

        log.info("resp hits0: {}", hitsMetadata.hits().get(0));

        log.info("resp total: {}", hitsMetadata.total());
    }

    @Test
    public void testSearchGeoShapeEnvelope() throws IOException {
//        需要写json格式的，也是比较原始
        var shape = "{\"type\":\"envelope\",\"coordinates\":[[121.5,31.1],[121.7,30.9]]}";
        GeoShapeQuery geoShapeQuery =
                new GeoShapeQuery.Builder().shape(builder -> builder.shape(JsonData.fromJson(shape)).relation(GeoShapeRelation.Within))
                        .field("location")
                        .build();

        SearchRequest searchRequest = new SearchRequest.Builder().index("hotel")
                .query(b -> b.geoShape(geoShapeQuery)).build();


        SearchResponse<HotelDoc> response = esClient.search(searchRequest, HotelDoc.class);
        HitsMetadata<HotelDoc> hitsMetadata = response.hits();

        log.info("resp hits0: {}", hitsMetadata.hits().get(0));
        log.info("resp hits1: {}", hitsMetadata.hits().get(1));

        log.info("resp total: {}", hitsMetadata.total());
    }

    /**
     * 同上
     *
     * @throws IOException
     */
    @Test
    public void testSearchGeoBoundingBox() throws IOException {
//        需要写json格式的，也是比较原始

        GeoLocation topLeftGeo = new GeoLocation.Builder().latlon(b -> b.lat(31.1).lon(121.5)).build();
        GeoLocation bottomRightGeo = new GeoLocation.Builder().latlon(b -> b.lat(30.9).lon(121.7)).build();

        GeoBounds geoBounds =
                new GeoBounds.Builder().tlbr(b -> b.topLeft(topLeftGeo).bottomRight(bottomRightGeo)).build();
        GeoBoundingBoxQuery query = new GeoBoundingBoxQuery.Builder().field("location")
                .boundingBox(geoBounds).build();


        SearchResponse<HotelDoc> response = esClient.search(rb -> rb.query(q -> q.geoBoundingBox(query)),
                HotelDoc.class);
        HitsMetadata<HotelDoc> hitsMetadata = response.hits();

        log.info("resp hits0: {}", hitsMetadata.hits().get(0));
        log.info("resp hits1: {}", hitsMetadata.hits().get(1));

        log.info("resp total: {}", hitsMetadata.total());
    }

    @Test
    public void testBool() throws IOException {

//        下面这俩query是同个东西
        GeoDistanceQuery geoDistanceQuery =
                new GeoDistanceQuery.Builder().distance("15km").field("location").location(b1 -> b1.latlon(b2 -> b2.lon(121.5).lat(31.21))).build();

        var shapeJson = JsonData.fromJson("{\"type\":\"circle\",\"coordinates\":[121.5,31.21],\"radius\":\"15km\"}");
        GeoShapeQuery geoShapeQuery =
                new GeoShapeQuery.Builder().field("location").shape(b1 -> b1.relation(GeoShapeRelation.Within).shape(shapeJson)).build();

        BoolQuery boolQuery = new BoolQuery.Builder()
                .must(b1 -> b1.match(b2 -> b2.field("name").query("如家")))
                .mustNot(b1 -> b1.range(b2 -> b2.field("price").gt(JsonData.of("400"))))
//                .filter(b1 -> b1.geoDistance(geoDistanceQuery))
                .filter(b1 -> b1.geoShape(geoShapeQuery))
                .build();

        SearchRequest request = new SearchRequest.Builder().index("hotel")
                .query(b1 -> b1.bool(boolQuery)).build();


        SearchResponse<HotelDoc> response = esClient.search(request, HotelDoc.class);
        HitsMetadata<HotelDoc> hitsMetadata = response.hits();

        log.info("resp hits0: {}", hitsMetadata.hits().get(0));
        log.info("resp hits1: {}", hitsMetadata.hits().get(1));

        log.info("resp total: {}", hitsMetadata.total());

    }

    @Test
    public void testSearchByFunction() throws IOException {
        MatchQuery matchQuery = new MatchQuery.Builder().field("all").query("外滩").build();
        FunctionScoreQuery functionScoreQuery =
                new FunctionScoreQuery.Builder()
                        .query(b -> b.match(matchQuery))
                        .functions(b1 -> b1.filter(b2 -> b2.term(b3 -> b3.field("brand").value("如家"))).weight(10d))
                        .boostMode(FunctionBoostMode.Sum).build();
        Query functionQuery = new Query.Builder().functionScore(functionScoreQuery).build();

        SearchRequest request = new SearchRequest.Builder().query(functionQuery).build();
        SearchResponse<HotelDoc> response = esClient.search(request, HotelDoc.class);
        HitsMetadata<HotelDoc> hitsMetadata = response.hits();

        log.info("resp hits0: {}", hitsMetadata.hits().get(0));
        log.info("resp hits1: {}", hitsMetadata.hits().get(1));

        log.info("resp total: {}", hitsMetadata.total());
    }


    @Test
    public void testSort1() throws IOException {
        SortOptions scoreSorter =
                new SortOptions.Builder().field(b2 -> b2.field("score").order(SortOrder.Desc)).build();
        SortOptions priceSorter =
                new SortOptions.Builder().field(b2 -> b2.field("price").order(SortOrder.Asc)).build();

        SearchRequest request =
                new SearchRequest.Builder().index("hotel")
                        .sort(Arrays.asList(scoreSorter, priceSorter)).build();
        SearchResponse<HotelDoc> response = esClient.search(request, HotelDoc.class);
        HitsMetadata<HotelDoc> hitsMetadata = response.hits();

        log.info("resp hits0: {}", hitsMetadata.hits().get(0));
        log.info("resp hits1: {}", hitsMetadata.hits().get(1));

        log.info("resp total: {}", hitsMetadata.total());

    }

    @Test
    public void testSort2() throws IOException {
        GeoLocation location = new GeoLocation.Builder().latlon(b2 -> b2.lat(31.034661).lon(121.612282)).build();
//        这个field指的是用哪个字段进行排序计算
        SortOptions locationSorter =
                new SortOptions.Builder().geoDistance(b -> b.field("location").location(location).unit(DistanceUnit.Kilometers)).build();

        SearchRequest request =
                new SearchRequest.Builder().index("hotel")
                        .sort(locationSorter).build();
        SearchResponse<HotelDoc> response = esClient.search(request, HotelDoc.class);
        HitsMetadata<HotelDoc> hitsMetadata = response.hits();

        log.info("resp hits0: {}", hitsMetadata.hits().get(0));
        log.info("resp hits1: {}", hitsMetadata.hits().get(1));

        log.info("resp total: {}", hitsMetadata.total());

    }


    @Test
    public void testPagination() throws IOException {

        SearchRequest request =
                new SearchRequest.Builder()
                        .index("hotel").sort(b -> b.field(b1 -> b1.field("price").order(SortOrder.Asc))).from(0).size(10).build();
        SearchResponse<HotelDoc> response = esClient.search(request, HotelDoc.class);
        HitsMetadata<HotelDoc> hitsMetadata = response.hits();

        log.info("resp hits0: {}", hitsMetadata.hits().get(0));
        log.info("resp hits1: {}", hitsMetadata.hits().get(1));
        log.info("hits size: {}", hitsMetadata.hits().size());

        log.info("resp total: {}", hitsMetadata.total());
    }


    @Test
    public void testSearchAfter() throws IOException {
        SortOptions scoreSorter =
                new SortOptions.Builder().field(b2 -> b2.field("score").order(SortOrder.Desc)).build();
        SortOptions priceSorter =
                new SortOptions.Builder().field(b2 -> b2.field("price").order(SortOrder.Asc)).build();

        SearchRequest request = new SearchRequest.Builder().index("hotel")
                .size(2)
                .sort(Arrays.asList(scoreSorter, priceSorter))
                .searchAfter(Arrays.asList(new FieldValue.Builder().longValue(48).build(),
                        new FieldValue.Builder().longValue(617).build()))
                .build();
        SearchResponse<HotelDoc> response = esClient.search(request, HotelDoc.class);
        HitsMetadata<HotelDoc> hitsMetadata = response.hits();
        log.info("resp hits0: {}", hitsMetadata.hits().get(0));
        log.info("resp hits1: {}", hitsMetadata.hits().get(1));
        log.info("hits size: {}", hitsMetadata.hits().size());

        log.info("resp total: {}", hitsMetadata.total());
    }


    @Test
    public void testHighlight() throws IOException {

        SearchRequest request = new SearchRequest.Builder().index("hotel")
                .query(b -> b.match(b2 -> b2.field("name").query("如家")))
                .highlight(b1 -> b1.fields("name", b2 -> b2.preTags("<span>").postTags("</span>")))
                .build();
        SearchResponse<HotelDoc> response = esClient.search(request, HotelDoc.class);
        HitsMetadata<HotelDoc> hitsMetadata = response.hits();
        log.info("resp hits0: {}", hitsMetadata.hits().get(0));
        log.info("resp hits0 highlight: {}", hitsMetadata.hits().get(0).highlight());

    }


    @Test
    public void testAgg1() throws IOException {
        SearchRequest request = new SearchRequest.Builder().index("hotel")
                .query(b1 -> b1.range(b2 -> b2.field("price").lte(JsonData.of(200))))
                .size(0)
                .aggregations("brandAgg",
                        b1 -> b1.terms(b2 -> b2.field("brand").order(NamedValue.of("_count", SortOrder.Asc)).size(10)))
                .build();

        SearchResponse<HotelDoc> response = esClient.search(request, HotelDoc.class);
        HitsMetadata<HotelDoc> hitsMetadata = response.hits();
        log.info("resp hits0 : {}", hitsMetadata.hits().size());
        log.info("resp agg: {}", response.aggregations());
    }


    @Test
    public void testAggState() throws IOException {

        Aggregation agg = new Aggregation.Builder().terms(b1 -> b1.field("brand"))
                .aggregations("scoreAgg", b1 -> b1.stats(b2 -> b2.field("score")))
                .build();
        SearchRequest request = new SearchRequest.Builder().index("hotel")
                .size(0)
                .aggregations("brandAgg", agg)
                .build();

        SearchResponse<HotelDoc> response = esClient.search(request, HotelDoc.class);
        HitsMetadata<HotelDoc> hitsMetadata = response.hits();
        log.info("resp hits0: {}", hitsMetadata.hits());
        log.info("resp total: {}", hitsMetadata.total());

        log.info("resp agg: {}", response.aggregations());
    }

    private List<HotelDocCompletion> parseHotelCompletion() {
        List<HotelDocCompletion> hotelDocs = null;

        try {
            Path path = Paths.get(this.getClass().getClassLoader().getResource("hotels.txt").getPath());
            log.info("{}", path.toAbsolutePath());
            try (Stream<String> lines = Files.lines(path)) {
                hotelDocs = lines.map(s -> {
                    var hotel = new HotelDocCompletion();
                    s = StringUtils.replace(s, "'", "");
                    String[] split = s.split(", ");
                    var id = Long.parseLong(split[0]);
                    hotel.setId(id);
                    var name = split[1];
                    hotel.setName(name);
                    var address = split[2];
                    hotel.setAddress(address);
                    var price = Integer.parseInt(split[3]);
                    hotel.setPrice(price);
                    var score = Integer.parseInt(split[4]);
                    hotel.setScore(score);
                    var brand = split[5];
                    hotel.setBrand(brand);
                    var city = split[6];
                    hotel.setCity(city);
                    var starName = split[7];
                    hotel.setStarName(starName);
                    var business = split[8];
                    hotel.setBusiness(business);
                    var location = String.format("%s,%s", split[9], split[10]);
                    hotel.setLocation(location);
                    var pic = split[11];
                    hotel.setPic(pic);

                    hotel.setSuggestion(Arrays.asList(brand, business));
//                    log.info("hotel: {}", hotel);
                    return hotel;
                }).toList();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return hotelDocs;
    }


    @Test
    public void testIndexHotelCompletion() throws IOException {
        List<HotelDocCompletion> hotelDocs = parseHotelCompletion();


        List<BulkOperation> bulkOperations = hotelDocs.stream().map(h ->
//                对于BulkRequest，实际上就是执行多个操作（operation），这里就是定义每个operation是干啥的。比如这里就是index doc操作。
                        new BulkOperation.Builder().index(indexOp -> indexOp.index("hotel_completion").id(h.getId().toString()).document(h)).build()
        ).toList();
        BulkRequest bulkRequest = new BulkRequest.Builder().operations(bulkOperations).build();
        BulkResponse response = esClient.bulk(bulkRequest);
        log.info("resp: {}", response.items());

//        然后可以查结果
    }


    @Test
    public void testSuggestion() throws IOException{

        Suggester suggester = new Suggester.Builder().suggesters("title_suggest",
                b1 -> b1.text("rujia").completion(b2 -> b2.field("suggestion"))).build();
        SearchRequest request = new SearchRequest.Builder().index("hotel_completion")
                .suggest(suggester).build();


        SearchResponse<HotelDocCompletion> response = esClient.search(request, HotelDocCompletion.class);
        log.info("suggest: {}", response.suggest());
    }
}
