package org.talk.is.cheap.project.free.flow.scheduler.task.controller;


import com.google.common.base.VerifyException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.project.free.flow.common.message.ResultCode;
import org.talk.is.cheap.project.free.flow.common.message.impl.QueryTaskDefinitionReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.QueryTaskDefinitionResp;
import org.talk.is.cheap.project.free.flow.common.message.impl.vo.StageDefinitionVO;
import org.talk.is.cheap.project.free.flow.common.message.impl.vo.TaskDefinitionVO;
import org.talk.is.cheap.project.free.flow.common.router.URIs;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskGraphDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.query.example.StageDefinitionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.query.example.TaskDefinitionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.query.example.TaskGraphDefinitionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskGraphDefinitionService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 用于读取任务定义的controller
 */
@RestController
public class DefinitionController {


    @Autowired
    private TaskDefinitionService taskDefinitionService;

    @Autowired
    private StageDefinitionService stageDefinitionService;

    @Autowired
    private TaskGraphDefinitionService taskGraphDefinitionService;

    @RequestMapping(path = URIs.SchedulerDefinitionURIs.QUERY_TASK_DEFINITION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public QueryTaskDefinitionResp getTaskDefinitionResp(@RequestBody QueryTaskDefinitionReq req) {
        QueryTaskDefinitionResp queryTaskDefinitionResp = new QueryTaskDefinitionResp();
        try {
            TaskDefinitionExample example = new TaskDefinitionExample();


            QueryTaskDefinitionReq.QueryTaskDefinitionReqData reqData = req.getData();

            if (reqData.getQueries() != null) {
                for (QueryTaskDefinitionReq.QueryTaskDefinitionReqData.Query query : reqData.getQueries()) {
                    VerifyUtil.shallNotBeBlank(query.getTaskName(), "task name in query can not be blank");
                    TaskDefinitionExample.Criteria criteria = example.or();
                    criteria.andTaskNameEqualTo(query.getTaskName());
                    if (query.getVersion() != null) {
                        criteria.andVersionEqualTo(query.getVersion());
                    }
                }
            }

            long count = taskDefinitionService.countByExample(example);

            int page = (reqData.getPage() != null && reqData.getPage() > 0) ? reqData.getPage() : 1;
            int pageSize = (reqData.getPageSize() != null && reqData.getPageSize() > 0) ? reqData.getPageSize() : 20;
            int offset = (page - 1) * pageSize;
            example.setLimit(pageSize);
            example.setOffset(offset);

            List<TaskDefinition> taskDefinitions = taskDefinitionService.selectByExampleDeepPaging(example);
            List<TaskDefinitionVO> taskDefinitionVOS = getTaskDefinitionVOS(taskDefinitions);


            queryTaskDefinitionResp.setCode(ResultCode.SUCCESS.getCode());
            queryTaskDefinitionResp.setData(QueryTaskDefinitionResp.QueryTaskDefinitionRespData.builder()
                    .total(count)
                    .page(page)
                    .pageSize(pageSize)
                    .taskDefinitionVOS(taskDefinitionVOS)
                    .build());
            return queryTaskDefinitionResp;

        } catch (VerifyException e) {
            queryTaskDefinitionResp.setCode(ResultCode.VERIFY_FAIL.getCode());
            queryTaskDefinitionResp.setMsg(e.getMessage());
            return queryTaskDefinitionResp;
        } catch (Exception e) {
            queryTaskDefinitionResp.setCode(ResultCode.FAIL.getCode());
            queryTaskDefinitionResp.setMsg(e.getMessage());
            return queryTaskDefinitionResp;
        }
    }

    @NotNull
    private List<TaskDefinitionVO> getTaskDefinitionVOS(List<TaskDefinition> taskDefinitions) {
        List<TaskDefinitionVO> taskDefinitionVOS = new ArrayList<>();

        for (TaskDefinition taskDefinition : taskDefinitions) {

            TaskDefinitionVO taskDefinitionVO = new TaskDefinitionVO();
            BeanUtils.copyProperties(taskDefinition, taskDefinitionVO);

            StageDefinitionExample stageDefinitionExample = new StageDefinitionExample();
            stageDefinitionExample.createCriteria()
                    .andTaskIdEqualTo(taskDefinition.getId());
            List<StageDefinition> stageDefinitions = stageDefinitionService.selectByExample(stageDefinitionExample);

            TaskGraphDefinitionExample taskGraphDefinitionExample = new TaskGraphDefinitionExample();
            taskGraphDefinitionExample.createCriteria().andTaskIdEqualTo(taskDefinition.getId());
            List<TaskGraphDefinition> taskGraphDefinitions = taskGraphDefinitionService.selectByExample(taskGraphDefinitionExample);


            // 写入时校验，读取就不校验了
            Map<String, StageDefinitionVO> stageDefinitionVOMap = new HashMap<>();
            Set<String> roots = new HashSet<>();
            Map<Long, StageDefinitionVO> idStageDefinitionVOMap = new HashMap<>();
            Map<String, Set<String>> pointOutGraph = new HashMap<>();

            for (StageDefinition stageDefinition : stageDefinitions) {
                StageDefinitionVO vo = new StageDefinitionVO();
                BeanUtils.copyProperties(stageDefinition, vo);
                // 确保pointOutGraph中包含了所有的stage，即使这个stage没有pointOut的stage，这个是为了与StageDefinitionBO保持逻辑相同
                pointOutGraph.putIfAbsent(stageDefinition.getStageName(),new HashSet<>());
                stageDefinitionVOMap.put(vo.getStageName(), vo);
                if (vo.getIsStartingStage()) {
                    roots.add(vo.getStageName());
                }
                idStageDefinitionVOMap.put(vo.getId(), vo);
            }
            taskDefinitionVO.setRoots(roots);
            taskDefinitionVO.setStageDefinitionVOMap(stageDefinitionVOMap);

            for (TaskGraphDefinition taskGraphDefinition : taskGraphDefinitions) {
                if (!idStageDefinitionVOMap.containsKey(taskGraphDefinition.getFromStageId()) || !idStageDefinitionVOMap.containsKey(taskGraphDefinition.getToStageId())) {
                    // 因为没校验，如果数据库变更可能导致报错，规避一下
                    continue;
                }
                String fromStageName = idStageDefinitionVOMap.get(taskGraphDefinition.getFromStageId()).getStageName();
                String toStageName = idStageDefinitionVOMap.get(taskGraphDefinition.getToStageId()).getStageName();
                pointOutGraph.get(fromStageName).add(toStageName);
            }


            taskDefinitionVO.setPointOutGraph(pointOutGraph);

            taskDefinitionVOS.add(taskDefinitionVO);
        }
        return taskDefinitionVOS;
    }


}
