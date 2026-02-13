package org.talk.is.cheap.project.free.flow.starter.repository.service.redis;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;

import java.util.Collections;

import static org.talk.is.cheap.project.free.flow.starter.repository.config.RedisAutoConfig.STRING_REDIS_TEMPLATE;


/**
 * key的位置统一管理，避免重复
 */
@Service
@Slf4j
public class RedisService {

    private static final String TASK_WORKER_ADDR_PREFIX = "T_W_ADDR";
    private static final String TASK_EXECUTION_PREFIX = "taskExecution";

    @Autowired
    @Qualifier(STRING_REDIS_TEMPLATE)
    private StringRedisTemplate stringRedisTemplate;

    public static String getTaskWorkerAddrMapKey(String taskName, Integer version) {
        VerifyUtil.requireNotNull(taskName, "key不可以为null");
        VerifyUtil.requireNotNull(version, "version不可以为null");
        return String.join("-", TASK_WORKER_ADDR_PREFIX, taskName, Integer.toString(version));
    }


    public Boolean deleteEmptySet(String setKey){
        String script = """
                            local size = redis.call('SCARD', KEYS[1])
                            if size == 0 then
                                redis.call('DEL', KEYS[1])
                                return 1
                            else
                                return 0
                            end
                            """;
        RedisScript<Long> redisScript = RedisScript.of(script, Long.class);
        Long result = stringRedisTemplate.execute(redisScript, Collections.singletonList(setKey));
        return result != null && result == 1;
    }

}
