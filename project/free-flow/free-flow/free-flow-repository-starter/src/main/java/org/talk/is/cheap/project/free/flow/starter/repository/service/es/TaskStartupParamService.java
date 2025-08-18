package org.talk.is.cheap.project.free.flow.starter.repository.service.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.OpType;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.starter.repository.config.EsAutoConfig;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.TaskStartupParam;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.SeqGeneratorUtil;

import java.io.IOException;

@Service
@Slf4j
public class TaskStartupParamService {

    private static final String INDEX_NAME = "task_startup_param";

    private static final String SEQ_NAME = "task_startup_param";

    @Autowired
    @Qualifier(EsAutoConfig.ES_CLIENT_BEAN_NAME)
    private ElasticsearchClient esClient;

    @Autowired
    private SeqGeneratorUtil seqGeneratorUtil;

    /**
     *
     * @param taskStartupParam
     * @return id
     * @throws IOException id重复报错
     */
    public String create(TaskStartupParam taskStartupParam) throws IOException {
        IndexRequest<TaskStartupParam> req =
                new IndexRequest.Builder<TaskStartupParam>()
                        .opType(OpType.Create)
                        .index(INDEX_NAME)
                        .id(Long.toString(seqGeneratorUtil.getNextId(SEQ_NAME)))
                        .document(taskStartupParam)
                        .build();

        IndexResponse resp = esClient.index(req);
//        log.info("resp:{}, result {}", resp, resp.result());
        return resp.id();
    }
}
