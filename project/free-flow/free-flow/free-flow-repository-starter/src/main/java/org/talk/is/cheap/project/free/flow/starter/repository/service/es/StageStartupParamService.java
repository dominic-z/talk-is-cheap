package org.talk.is.cheap.project.free.flow.starter.repository.service.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.OpType;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.starter.repository.config.EsAutoConfig;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.ESPojoDTO;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.StageStartupParam;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.SeqGeneratorUtil;

import java.io.IOException;

@Service
@Slf4j
public class StageStartupParamService {

    private static final String INDEX_NAME = "stage_startup_param";

    private static final String SEQ_NAME = "stage_startup_param";

    @Autowired
    @Qualifier(EsAutoConfig.ES_CLIENT_BEAN_NAME)
    private ElasticsearchClient esClient;

    @Autowired
    private SeqGeneratorUtil seqGeneratorUtil;

    /**
     * @param stageStartupParam
     * @return id
     * @throws IOException id重复报错
     */
    public String create(StageStartupParam stageStartupParam) throws IOException {
        IndexRequest<StageStartupParam> req =
                new IndexRequest.Builder<StageStartupParam>()
                        .opType(OpType.Create)
                        .index(INDEX_NAME)
                        .id(Long.toString(seqGeneratorUtil.getNextId(SEQ_NAME)))
                        .document(stageStartupParam)
                        .build();

        IndexResponse resp = esClient.index(req);
//        log.info("resp:{}, result {}", resp, resp.result());
        return resp.id();
    }

    public boolean update(String id, StageStartupParam stageStartupParam) throws IOException {
        UpdateRequest<StageStartupParam, StageStartupParam> updateRequest =
                new UpdateRequest.Builder<StageStartupParam, StageStartupParam>()
                        .index(INDEX_NAME)
                        .id(id)
                        .doc(stageStartupParam)
                        .build();

        UpdateResponse<StageStartupParam> updateResp = esClient.update(updateRequest, StageStartupParam.class);
        return updateResp.result() == Result.Updated;
    }


    public StageStartupParam getById(String id) throws IOException {
        GetRequest getRequest = new GetRequest.Builder()
                .index(INDEX_NAME)
                .id(id).build();
        GetResponse<StageStartupParam> resp = esClient.get(getRequest, StageStartupParam.class);
        return resp.source();
    }


    public ESPojoDTO<StageStartupParam> getByStageStartupId(long stageStartupId) throws IOException {

        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(INDEX_NAME)
                .query(qb -> qb.term(tb -> tb.field(StageStartupParam.STAGE_STARTUP_ID).value(stageStartupId))).build();
        SearchResponse<StageStartupParam> resp = esClient.search(searchRequest, StageStartupParam.class);
        HitsMetadata<StageStartupParam> hits = resp.hits();
        if (resp.hits().total() == null || resp.hits().total().value() == 0) {
            return null;
        }
        Hit<StageStartupParam> stageStartupParamHit = resp.hits().hits().get(0);
        return ESPojoDTO.<StageStartupParam>builder().id(stageStartupParamHit.id()).data(stageStartupParamHit.source()).build();
    }
}
