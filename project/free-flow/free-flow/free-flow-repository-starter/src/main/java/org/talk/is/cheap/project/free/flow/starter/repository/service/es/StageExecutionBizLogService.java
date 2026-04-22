package org.talk.is.cheap.project.free.flow.starter.repository.service.es;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.OpType;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.starter.repository.config.EsAutoConfig;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.ESPojoDTO;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.StageExecutionBizLog;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.SeqGeneratorUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class StageExecutionBizLogService {

    private static final String INDEX_NAME = "stage_execution_biz_log";

    private static final String SEQ_NAME = "stage_execution_biz_log";

    @Autowired
    @Qualifier(EsAutoConfig.ES_CLIENT_BEAN_NAME)
    private ElasticsearchClient esClient;

    @Autowired
    @Qualifier(EsAutoConfig.ASYNC_ES_CLIENT_BEAN_NAME)
    private ElasticsearchAsyncClient asyncClient;

    @Autowired
    private SeqGeneratorUtil seqGeneratorUtil;

    /**
     * @param stageExecutionBizLog
     * @return id
     * @throws IOException id重复报错
     */
    public String create(StageExecutionBizLog stageExecutionBizLog) throws IOException {
        IndexRequest<StageExecutionBizLog> req =
                new IndexRequest.Builder<StageExecutionBizLog>()
                        .opType(OpType.Create)
                        .index(INDEX_NAME)
                        .id(Long.toString(seqGeneratorUtil.getNextId(SEQ_NAME)))
                        .document(stageExecutionBizLog)
                        .build();

        IndexResponse resp = esClient.index(req);
//        log.info("resp:{}, result {}", resp, resp.result());
        return resp.id();
    }


    public CompletableFuture<IndexResponse> createAsync(StageExecutionBizLog stageExecutionBizLog) throws IOException {
        IndexRequest<StageExecutionBizLog> req =
                new IndexRequest.Builder<StageExecutionBizLog>()
                        .opType(OpType.Create)
                        .index(INDEX_NAME)
                        .id(Long.toString(seqGeneratorUtil.getNextId(SEQ_NAME)))
                        .document(stageExecutionBizLog)
                        .build();

        ;
//        log.info("resp:{}, result {}", resp, resp.result());
        return asyncClient.index(req);
    }

    public void logAsync(Long taskExecutionId, Long stageExecutionId, String s) {
        StageExecutionBizLog stageExecutionBizLog = new StageExecutionBizLog();
        stageExecutionBizLog.setTaskExecutionId(taskExecutionId);
        stageExecutionBizLog.setStageExecutionId(stageExecutionId);
        stageExecutionBizLog.setLog(s);
        stageExecutionBizLog.setCreateTime(new Date());
        try {
            createAsync(stageExecutionBizLog);
        } catch (IOException e) {
            log.error("error when create log: {}, exeId: {}", s, stageExecutionId);
        }

    }

    public List<ESPojoDTO<StageExecutionBizLog>> getByStageExecutionId(long stageExecutionId, int pageSize,
                                                                       List<Long> searchAfter) throws IOException {
        SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
                .index(INDEX_NAME)
                .sort(SortOptions.of(b->b.field(f -> f.field(StageExecutionBizLog.CREATE_TIME).order(SortOrder.Asc))),
                        SortOptions.of(b->b.field(f -> f.field("_doc").order(SortOrder.Asc))))
                .size(pageSize)
                .query(qb -> qb.term(tb -> tb.field(StageExecutionBizLog.STAGE_EXECUTION_ID).value(stageExecutionId)));
        if (searchAfter != null && !searchAfter.isEmpty()) {
            searchBuilder.searchAfter(searchAfter.stream().map(FieldValue::of).toList());
        }
        SearchRequest request = searchBuilder.build();

        SearchResponse<StageExecutionBizLog> resp = esClient.search(request, StageExecutionBizLog.class);
        if (resp.hits() == null || resp.hits().hits().isEmpty()) {
            return new ArrayList<>();
        }
        return resp.hits().hits().stream().map(hit ->{
            List<Object> sort = hit.sort().stream().map(s -> (Object) s.longValue()).toList();
            log.info("sort {}",sort);
            return ESPojoDTO.<StageExecutionBizLog>builder().id(hit.id())
                            .data(hit.source())
                            .sort(sort)
                            .build();
                }
        ).toList();
    }
}
