package org.talk.is.cheap.project.free.flow.starter.worker.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateTaskRuntimeEnvDTO {

    private Long taskExecutionId;
    private String initialEncodedSharedContext;
    // 包含所有stage的input
    private Map<String, String> stageEncodedInputs;
    // 仅仅包含启动的stage的信息
    private Map<String, Long> startingStageExecutionId;
}
