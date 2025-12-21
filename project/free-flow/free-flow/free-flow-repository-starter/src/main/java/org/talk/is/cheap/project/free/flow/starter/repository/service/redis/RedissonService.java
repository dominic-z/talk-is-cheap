package org.talk.is.cheap.project.free.flow.starter.repository.service.redis;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RedissonService {

    private static final String LOCK_PREFIX = "lock";
    private static final String TASK_EXECUTION_PREFIX = "taskExecution";

    public static String getTaskExecutionLockKey(long taskExecutionId) {
        return String.join("-", LOCK_PREFIX, TASK_EXECUTION_PREFIX, Long.toString(taskExecutionId));
    }

}
