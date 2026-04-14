package org.talk.is.cheap.project.free.flow.scheduler.task.controller;


import com.google.common.base.VerifyException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.project.free.flow.common.enums.TaskDefinitionStatus;
import org.talk.is.cheap.project.free.flow.common.message.ResultCode;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.QueryTaskDefinitionDetailsReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.QueryTaskDefinitionDetailResp;
import org.talk.is.cheap.project.free.flow.common.message.impl.dto.StageDefinitionDTO;
import org.talk.is.cheap.project.free.flow.common.message.impl.dto.TaskDefinitionDTO;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.TaskDefinitionListResp;
import org.talk.is.cheap.project.free.flow.common.router.URIs;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskGraphDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.StageDefinitionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskDefinitionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskGraphDefinitionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskGraphDefinitionService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 用于读取任务定义的controller
 */
@RestController
@Slf4j
public class DefinitionController {


    @Autowired
    private TaskDefinitionService taskDefinitionService;

    @Autowired
    private StageDefinitionService stageDefinitionService;

    @Autowired
    private TaskGraphDefinitionService taskGraphDefinitionService;

    @RequestMapping(path = URIs.SchedulerTaskDefinitionURIs.DETAILS_QUERY, method = RequestMethod.POST, produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public QueryTaskDefinitionDetailResp queryTaskDefinitionDetail(@RequestBody QueryTaskDefinitionDetailsReq req) {
        QueryTaskDefinitionDetailResp getTaskDefinitionDetailResp = new QueryTaskDefinitionDetailResp();
        try {
            QueryTaskDefinitionDetailsReq.QueryTaskDefinitionDetailsReqData reqData = req.getData();
            VerifyUtil.requireNotNull(reqData, "请求参数为空");
            TaskDefinitionExample example = new TaskDefinitionExample();

            QueryTaskDefinitionDetailsReq.QueryTaskDefinitionDetailsReqData.Query query = reqData.getQuery();
            if (query != null) {
                VerifyUtil.requireNotBlank(query.getTaskName(), "task name in query can not be blank");
                TaskDefinitionExample.Criteria criteria = example.or();
                criteria.andNameEqualTo(query.getTaskName());
                if (query.getTaskVersion() != null) {
                    criteria.andVersionEqualTo(query.getTaskVersion());
                }
            }

            long count = taskDefinitionService.countByExample(example);

            int page = (reqData.getPage() != null && reqData.getPage() > 0) ? reqData.getPage() : 1;
            int pageSize = (reqData.getPageSize() != null && reqData.getPageSize() > 0) ? reqData.getPageSize() : 20;
            int offset = (page - 1) * pageSize;
            example.setLimit(pageSize);
            example.setOffset(offset);

            List<TaskDefinition> taskDefinitions = taskDefinitionService.selectByExampleDeepPaging(example);
            List<TaskDefinitionDTO> taskDefinitionDTOS = getTaskDefinitionDTOs(taskDefinitions);


            getTaskDefinitionDetailResp.setCode(ResultCode.SUCCESS.getCode());
            getTaskDefinitionDetailResp.setData(QueryTaskDefinitionDetailResp.GetTaskDefinitionDetailRespData.builder()
                    .taskDefinitionDTOs(taskDefinitionDTOS)
                    .build());
            return getTaskDefinitionDetailResp;

        } catch (VerifyException e) {
            log.error("", e);
            getTaskDefinitionDetailResp.setCode(ResultCode.VERIFY_FAIL.getCode());
            getTaskDefinitionDetailResp.setMsg(e.getMessage());
            return getTaskDefinitionDetailResp;
        } catch (Exception e) {
            log.error("", e);
            getTaskDefinitionDetailResp.setCode(ResultCode.FAIL.getCode());
            getTaskDefinitionDetailResp.setMsg(e.getMessage());
            return getTaskDefinitionDetailResp;
        }
    }

    @NotNull
    private List<TaskDefinitionDTO> getTaskDefinitionDTOs(List<TaskDefinition> taskDefinitions) {
        List<TaskDefinitionDTO> taskDefinitionVOS = new ArrayList<>();

        for (TaskDefinition taskDefinition : taskDefinitions) {

            TaskDefinitionDTO taskDefinitionDTO = new TaskDefinitionDTO();
            BeanUtils.copyProperties(taskDefinition, taskDefinitionDTO);

            StageDefinitionExample stageDefinitionExample = new StageDefinitionExample();
            stageDefinitionExample.createCriteria()
                    .andTaskIdEqualTo(taskDefinition.getId());
            List<StageDefinition> stageDefinitions = stageDefinitionService.selectByExample(stageDefinitionExample);

            TaskGraphDefinitionExample taskGraphDefinitionExample = new TaskGraphDefinitionExample();
            taskGraphDefinitionExample.createCriteria().andTaskIdEqualTo(taskDefinition.getId());
            List<TaskGraphDefinition> taskGraphDefinitions = taskGraphDefinitionService.selectByExample(taskGraphDefinitionExample);


            // 写入时校验，读取就不校验了
            Map<String, StageDefinitionDTO> stageDefinitionVOMap = new HashMap<>();
            Set<String> roots = new HashSet<>();
            Map<Long, StageDefinitionDTO> idStageDefinitionVOMap = new HashMap<>();
            Map<String, Set<String>> pointOutGraph = new HashMap<>();

            for (StageDefinition stageDefinition : stageDefinitions) {
                StageDefinitionDTO vo = new StageDefinitionDTO();
                BeanUtils.copyProperties(stageDefinition, vo);
                // 确保pointOutGraph中包含了所有的stage，即使这个stage没有pointOut的stage，这个是为了与StageDefinitionBO保持逻辑相同
                pointOutGraph.putIfAbsent(stageDefinition.getName(), new HashSet<>());
                stageDefinitionVOMap.put(vo.getName(), vo);
                if (vo.getIsStartingStage()) {
                    roots.add(vo.getName());
                }
                idStageDefinitionVOMap.put(vo.getId(), vo);
            }
            taskDefinitionDTO.setRoots(roots);
            taskDefinitionDTO.setStageDefinitionMap(stageDefinitionVOMap);

            for (TaskGraphDefinition taskGraphDefinition : taskGraphDefinitions) {
                if (!idStageDefinitionVOMap.containsKey(taskGraphDefinition.getFromStageId()) || !idStageDefinitionVOMap.containsKey(taskGraphDefinition.getToStageId())) {
                    // 增强校验，一般来说不会出现这种情况，以防手抖修改数据库导致graph taskdefinition stagedefinition数据不一致导致的错误
                    continue;
                }
                String fromStageName = idStageDefinitionVOMap.get(taskGraphDefinition.getFromStageId()).getName();
                String toStageName = idStageDefinitionVOMap.get(taskGraphDefinition.getToStageId()).getName();
                pointOutGraph.get(fromStageName).add(toStageName);
            }


            taskDefinitionDTO.setPointOutGraph(pointOutGraph);

            taskDefinitionVOS.add(taskDefinitionDTO);
        }
        return taskDefinitionVOS;
    }

    @RequestMapping(path = URIs.SchedulerTaskDefinitionURIs.TASK_DEFINITION, method = RequestMethod.GET, produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public TaskDefinitionListResp getTaskDefinition(@RequestParam("page") Integer page, @RequestParam("pageSize") Integer pageSize) {
        TaskDefinitionListResp resp = new TaskDefinitionListResp();
        try {
            page = page == null ? 1 : page;
            pageSize = pageSize == null ? 10 : pageSize;
            TaskDefinitionExample example = new TaskDefinitionExample();
            TaskDefinitionExample.Criteria criteria = example.createCriteria();
            criteria.andStatusEqualTo(TaskDefinitionStatus.HAS_AVAILABLE_WORKER.getType());
            long count = taskDefinitionService.countByExample(example);
            int offset = (page - 1) * pageSize;
            example.setLimit(pageSize);
            example.setOffset(offset);

            List<TaskDefinition> taskDefinitions = taskDefinitionService.selectByExampleDeepPaging(example);

            TaskDefinitionListResp.Data respData = TaskDefinitionListResp.Data.builder()
                    .total(count)
                    .tasks(taskDefinitions.stream().map(d ->
                            TaskDefinitionListResp.Task.builder()
                                    .id(d.getId())
                                    .name(d.getName())
                                    .version(d.getVersion())
                                    .releaseTime(d.getUpdateTime())
                                    .build()
                    ).collect(Collectors.toList()))
                    .build();


            resp.success(respData);
        } catch (Exception e) {
            log.error("获取任务列表失败", e);
            resp.fail(ResultCode.FAIL, "获取任务列表失败");
        }
        return resp;
    }
}
