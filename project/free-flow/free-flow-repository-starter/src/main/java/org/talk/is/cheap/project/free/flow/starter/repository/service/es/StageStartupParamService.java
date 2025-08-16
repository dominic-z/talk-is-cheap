package org.talk.is.cheap.project.free.flow.starter.repository.service.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.OpType;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.starter.repository.config.EsAutoConfig;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.StageStartupParam;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.TaskStartupParam;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.SeqGeneratorUtil;

import java.io.IOException;

@Service
@Slf4j
public class StageStartupParamService {

    private static final String INDEX_NAME = "stack_startup_param";

    private static final String SEQ_NAME = "stack_startup_param";

    @Autowired
    @Qualifier(EsAutoConfig.ES_CLIENT_BEAN_NAME)
    private ElasticsearchClient esClient;

    @Autowired
    private SeqGeneratorUtil seqGeneratorUtil;

    /**
     *
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
}
