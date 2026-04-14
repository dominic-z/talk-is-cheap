package org.talk.is.cheap.project.free.flow.scheduler.task.controller;


import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.project.free.flow.common.enums.TaskStageStatus;
import org.talk.is.cheap.project.free.flow.common.message.ResultCode;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.TaskExecutionListResp;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.TaskStartupListResp;
import org.talk.is.cheap.project.free.flow.common.router.URIs;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskStartupExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageCountGroupByTaskStatus;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskExecution;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskStartup;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageStartupService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskStartupService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.TaskDefinitionServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.TaskExecutionServiceWrapper;
import org.talk.is.cheap.project.free.flow.starter.repository.service.derived.TaskStartupServiceWrapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用来查询任务执行情况
 */
@RestController
@Slf4j
public class TaskStartupInfoController {


    @Autowired
    private TaskStartupService taskStartupService;

    @Autowired
    private TaskStartupServiceWrapper taskStartupServiceWrapper;
    @Autowired
    private TaskExecutionServiceWrapper taskExecutionServiceWrapper;

    @Autowired
    private StageStartupService stageStartupService;
    @Autowired
    private TaskDefinitionServiceWrapper taskDefinitionServiceWrapper;

    private static final ModelMapper MODEL_MAPPER = new ModelMapper();


    @ResponseBody
    @RequestMapping(value = URIs.SchedulerTaskInfoURIs.TASK_STARTUPS, method = RequestMethod.GET, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public TaskStartupListResp getTaskStartupList(@RequestParam("page") Integer page, @RequestParam("pageSize") Integer pageSize) {
        TaskStartupListResp resp = new TaskStartupListResp();
        try {
            VerifyUtil.requireAllNotNull("页码或者页数为空", page, pageSize);
            TaskStartupExample example = new TaskStartupExample();
            long total = taskStartupService.countByExample(example);

            TaskStartupExample.Criteria criteria = example.createCriteria();
            example.setLimit(pageSize);
            example.setOffset((page - 1) * pageSize);
            example.setOrderByClause(TaskStartup.ID);
            List<TaskStartup> taskStartups = taskStartupService.selectByExampleDeepPaging(example);

            Map<Long, TaskDefinition> taskIdDefinitionMap =
                    taskDefinitionServiceWrapper.selectByIds(taskStartups.stream().map(TaskStartup::getTaskId).collect(Collectors.toList()))
                            .stream()
                            .collect(Collectors.toMap(TaskDefinition::getId, d -> d));

            List<TaskExecution> taskExecutions = taskExecutionServiceWrapper.selectByStartupIds(
                    taskStartups.stream().map(TaskStartup::getId).collect(Collectors.toList()),
                    TaskStageStatus.RUNNING.getStatus());
            Map<Long, TaskExecution> tsIdTEMap = taskExecutions.stream().collect(Collectors.toMap(TaskExecution::getTaskStartupId,
                    te -> te));
            Map<Long, StageCountGroupByTaskStatus> teIdStageCountMap =
                    stageStartupService.countGroupByTaskExecution(taskExecutions.stream().map(TaskExecution::getId).collect(Collectors.toList()))
                            .stream().collect(Collectors.toMap(StageCountGroupByTaskStatus::getTaskExecutionId, b -> b));

            TaskStartupListResp.TaskStartupListRespData respData = new TaskStartupListResp.TaskStartupListRespData();
            respData.setPage(page);
            respData.setPageSize(pageSize);
            respData.setTotal(total);
            respData.setTaskStartupInfoList(taskStartups.stream().map(tt -> {
                TaskStartupListResp.TaskStartupInfo info = new TaskStartupListResp.TaskStartupInfo();
                info.setId(tt.getId());
                info.setTaskName(taskIdDefinitionMap.get(tt.getTaskId()).getName());
                info.setTaskVersion(taskIdDefinitionMap.get(tt.getTaskId()).getVersion());
                info.setStatus(tt.getStatus());
                info.setStartupTime(tt.getCreateTime());
                if (TaskStageStatus.RUNNING.getStatus().equals(tt.getStatus()) && tsIdTEMap.containsKey(tt.getId()) && teIdStageCountMap.containsKey(tsIdTEMap.get(tt.getId()).getId())) {
                    StageCountGroupByTaskStatus groupByCount = teIdStageCountMap.get(tsIdTEMap.get(tt.getId()).getId());
                    int progress =
                            (int) (groupByCount.getCompletedCount() * 1.0 / (groupByCount.getCompletedCount() + groupByCount.getUnfinishedCount()));
                    info.setProgress(progress);
                }
                return info;
            }).collect(Collectors.toList()));

            resp.success(respData);
        } catch (Exception e) {
            log.error("发生异常", e);
            resp.fail(ResultCode.FAIL, e.getMessage());
        }
        return resp;
    }

    @ResponseBody
    @RequestMapping(value = URIs.SchedulerTaskInfoURIs.TASK_EXECUTIONS, method = RequestMethod.GET, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public TaskExecutionListResp getTaskExecutionByStartupId(@RequestParam("taskStartupId") Long taskStartupId) {
        TaskExecutionListResp resp = new TaskExecutionListResp();
        try {
            VerifyUtil.requireNotNull(taskStartupId, "任务启动ID为空，无法查询");
            List<TaskExecution> taskExecutions = taskExecutionServiceWrapper.selectByStartupIds(List.of(taskStartupId), null);

            resp.success(taskExecutions.stream().map(te -> MODEL_MAPPER.map(te, TaskExecutionListResp.TaskExecutionInfo.class)).toList());
        }catch (Exception e){
            log.error("发生异常",e);
            resp.fail(ResultCode.FAIL,e.getMessage());
        }
        return resp;
    }


}
