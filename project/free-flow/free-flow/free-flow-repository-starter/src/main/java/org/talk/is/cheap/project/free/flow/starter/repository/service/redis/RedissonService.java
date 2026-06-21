package org.talk.is.cheap.project.free.flow.starter.repository.service.redis;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;

@Service
@Slf4j
public class RedissonService {

    private static final String TASK_EXECUTION_LOCK_PREFIX = "lock-taskExecution";

    public static String getTaskExecutionLockKey(Long taskExecutionId) {
        VerifyUtil.requireNotNull(taskExecutionId,"key不可以为null");
        return String.join("-", TASK_EXECUTION_LOCK_PREFIX, Long.toString(taskExecutionId));
    }

}
