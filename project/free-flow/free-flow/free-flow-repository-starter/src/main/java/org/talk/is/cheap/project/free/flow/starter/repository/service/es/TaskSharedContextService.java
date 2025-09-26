package org.talk.is.cheap.project.free.flow.starter.repository.service.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.OpType;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.UpdateByQueryRequest;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.ResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.starter.repository.config.EsAutoConfig;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.StageStartupParam;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.TaskSharedContext;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.SeqGeneratorUtil;

import java.io.IOException;

@Service
@Slf4j
public class TaskSharedContextService {

    private static final String INDEX_NAME = "task_shared_context";

    private static final String SEQ_NAME = "task_shared_context";

    @Autowired
    @Qualifier(EsAutoConfig.ES_CLIENT_BEAN_NAME)
    private ElasticsearchClient esClient;

    @Autowired
    private SeqGeneratorUtil seqGeneratorUtil;

    /**
     * @param taskSharedContext
     * @return id
     * @throws IOException id重复报错
     */
    public String create(TaskSharedContext taskSharedContext) throws IOException {
        IndexRequest<TaskSharedContext> req =
                new IndexRequest.Builder<TaskSharedContext>()
                        .opType(OpType.Create)
                        .index(INDEX_NAME)
                        .id(Long.toString(seqGeneratorUtil.getNextId(SEQ_NAME)))
                        .document(taskSharedContext)
                        .build();

        IndexResponse resp = esClient.index(req);
//        log.info("resp:{}, result {}", resp, resp.result());
        return resp.id();
    }

    /**
     * 可以理解为es中的cas
     * @param id
     * @param taskSharedContext
     * @return
     * @throws IOException
     */
    public boolean safeUpdate(String id, TaskSharedContext taskSharedContext) throws IOException {

        while (true) {
            GetResponse<TaskSharedContext> current = getResponseById(id);
            VerifyUtil.shallBeTrue(current.source() != null && current.source().getUpdateTime() != null,
                    "task shared context dosen't exits or update time is null, es id:%s".formatted(id));
            if (current.source().getUpdateTime().before(taskSharedContext.getUpdateTime())) {
                UpdateRequest<TaskSharedContext, TaskSharedContext> updateRequest =
                        new UpdateRequest.Builder<TaskSharedContext, TaskSharedContext>()
                                .index(INDEX_NAME)
                                .id(id)
                                .ifSeqNo(current.seqNo())
                                .ifPrimaryTerm(current.primaryTerm())
                                .doc(taskSharedContext)
                                .build();
                try {
                    UpdateResponse<TaskSharedContext> updateResp = esClient.update(updateRequest, TaskSharedContext.class);
                } catch (Exception e) {
                    if (e instanceof ResponseException) {
                        if (((ResponseException) e).getResponse().getStatusLine().getStatusCode() == 409) {
                            log.warn("version_conflict_engine_exception when update index:{} id:{}, retrying", INDEX_NAME, id);
                        }
                    } else {
                        throw e;
                    }
                }

            } else {
                return false;
            }
        }

    }


    public TaskSharedContext getById(String id) throws IOException {
        GetResponse<TaskSharedContext> resp = getResponseById(id);
        return resp.source();
    }

    private GetResponse<TaskSharedContext> getResponseById(String id) throws IOException {
        GetRequest getRequest = new GetRequest.Builder()
                .index(INDEX_NAME)
                .id(id).build();
        GetResponse<TaskSharedContext> resp = esClient.get(getRequest, TaskSharedContext.class);
        return resp;
    }
}
