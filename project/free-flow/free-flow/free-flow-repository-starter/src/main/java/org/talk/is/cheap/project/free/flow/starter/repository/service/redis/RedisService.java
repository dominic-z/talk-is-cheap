package org.talk.is.cheap.project.free.flow.starter.repository.service.redis;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;


/**
 * key的位置统一管理，避免重复
 */
@Service
@Slf4j
public class RedisService {

    private static final String TASK_WORKER_ADDR_PREFIX = "T_W_ADDR";
    private static final String TASK_EXECUTION_PREFIX = "taskExecution";

    public static String getTaskWorkerAddrMapKey(String taskName, Integer version) {
        VerifyUtil.requireNotNull(taskName, "key不可以为null");
        VerifyUtil.requireNotNull(version, "version不可以为null");
        return String.join("-", TASK_WORKER_ADDR_PREFIX, taskName, Integer.toString(version));
    }


}
