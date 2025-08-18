package test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.Test;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo.TaskStartupParam;


public class Testing {

    @Test
    public void testJackson() throws JsonProcessingException {

        TaskStartupParam taskStartupParam = new TaskStartupParam();
        taskStartupParam.setTaskStartupId(1L);
        taskStartupParam.setStartupParamFullyQualifiedClassName("aa");
        taskStartupParam.setStartupParamEncoding("bb");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        System.out.println(objectMapper.readValue(objectMapper.writeValueAsString(taskStartupParam),TaskStartupParam.class));
    }
}
