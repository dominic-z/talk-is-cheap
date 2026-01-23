package org.talk.is.cheap.project.free.flow.starter.repository.service.es;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.OpType;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.starter.repository.config.EsAutoConfig;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.StageExecutionBizLog;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.StageStartupParam;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.SeqGeneratorUtil;

import java.io.IOException;
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

    public void logAsync(Long executionId, String s) {
        StageExecutionBizLog stageExecutionBizLog = new StageExecutionBizLog();
        stageExecutionBizLog.setStageExecutionId(executionId);
        stageExecutionBizLog.setLog(s);
        try {
            createAsync(stageExecutionBizLog);
        } catch (IOException e) {
            log.error("error when create log: {}, exeId: {}", s, executionId);
        }

    }
}
