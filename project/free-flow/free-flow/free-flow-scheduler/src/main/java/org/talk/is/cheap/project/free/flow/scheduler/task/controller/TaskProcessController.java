package org.talk.is.cheap.project.free.flow.scheduler.task.controller;


import com.google.common.base.VerifyException;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.message.ResultCode;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.PrepareStageReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.PrepareStageResp;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.ReScheduleTaskReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.StartTaskReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.WorkerCompleteStageResultReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.WorkerFailStageReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.WorkerFailStageResp;
import org.talk.is.cheap.project.free.flow.common.message.impl.scheduler.WorkerStartStageReportReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerStartTaskReq;
import org.talk.is.cheap.project.free.flow.common.message.impl.worker.WorkerStartTaskResp;
import org.talk.is.cheap.project.free.flow.common.router.URIs;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.service.SchedulerClusterManager;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.service.WorkerClusterManager;
import org.talk.is.cheap.project.free.flow.scheduler.task.client.WorkerTaskDriverClient;
import org.talk.is.cheap.project.free.flow.scheduler.task.service.WorkerTaskDriverService;
import org.talk.is.cheap.project.free.flow.scheduler.task.service.WorkerTaskResultService;

import java.util.List;
import java.util.Map;

/**
 * 1.用来与Worker进行交互，响应stage的成功或者失败
 * 2.接收外界启动任务的请求
 */
@RestController
@Slf4j
public class TaskProcessController {

    @Autowired
    private WorkerTaskResultService workerTaskResultService;

    @Autowired
    private WorkerTaskDriverService workerTaskDriverService;

    @Autowired
    private WorkerTaskDriverClient workerTaskDriverClient;

    @Autowired
    private WorkerClusterManager workerClusterManager;

    @Autowired
    private SchedulerClusterManager schedulerClusterManager;


    // todo: 启动一个任务 待继续往下写的代码

    /**
     * 启动一个任务的入口，整体流程如下：
     * 1. 准备：包括创建各种startup与Execution对象，以及记录入参；
     * 2. 通知worker执行任务，仅将根stage的参数送过去，避免一下子送太多导致worker内存压力过大
     * 3. worker每执行完一个stage都会上报scheduler
     * 4. 如果有下一个stage，申请执行下一个stage的数据，scheduler会prepare stage，随后告知worker成功，worker进行后续的执行
     *
     * @param req
     * @return
     */
    @RequestMapping(path = URIs.SchedulerTaskProcessURIs.START, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public HttpBody<String> startTask(@RequestBody StartTaskReq req) {
        HttpBody<String> resp = HttpBody.<String>builder().build();
        try {
            VerifyUtil.requireTrue(schedulerClusterManager.isLeader(), "当前节点不是leader，无法执行");
            StartTaskReq.Data data = req.getData();
            VerifyUtil.requireNotNull(data, "要执行的任务信息缺失，无法执行任务");
            Tuple3<String, Long, Map<String, Long>> prepareForTaskStartDTO = workerTaskDriverService.prepareForTask(data.getTaskName(),
                    data.getTaskVersion(),
                    data.getInitialEncodedSharedContext(),
                    data.getStageEncodedInputs());
            String workerAddress = prepareForTaskStartDTO._1();
            Long taskExecutionId = prepareForTaskStartDTO._2();
            Map<String, Long> rootStageName2ExecutionId = prepareForTaskStartDTO._3();

            WorkerStartTaskReq workerStartTaskReq = new WorkerStartTaskReq();
            workerStartTaskReq.setData(
                    WorkerStartTaskReq.Data.builder()
                            .taskExecutionId(taskExecutionId)
                            .taskName(data.getTaskName())
                            .taskVersion(data.getTaskVersion())
                            .initialEncodedSharedContext(data.getInitialEncodedSharedContext())
                            .stageEncodedInputs(data.getStageEncodedInputs())
                            .startingStageExecutionId(rootStageName2ExecutionId)
                            .build()
            );


            WorkerStartTaskResp workerStartTaskResp = workerTaskDriverClient.startTask(WorkerClusterManager.getWorkerURI(workerAddress),
                    workerStartTaskReq);
            VerifyUtil.requireTrue(workerStartTaskResp.isSuccess(), String.format("启动task(name:%s,version:%d)错误，原因:%s",
                    data.getTaskName(), data.getTaskVersion(), workerStartTaskResp.getMsg()));

            resp.success("");
        } catch (VerifyException e) {
            log.error("启动任务失败", e);
            resp.fail(ResultCode.VERIFY_FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("启动任务失败", e);
            resp.fail(ResultCode.FAIL, e.getMessage());
        }
        return resp;
    }

    /**
     * worker正式开始某个stage后，会进行上报
     *
     * @param req
     * @return
     */
    @RequestMapping(path = URIs.SchedulerTaskProcessURIs.STAGE_START_REPORT, method = RequestMethod.POST, produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HttpBody<String> stageStartReport(@RequestBody WorkerStartStageReportReq req) {

        HttpBody<String> resp = new HttpBody<>();
        try {
            List<WorkerStartStageReportReq.WorkerStartToExecuteStageReqDatum> data = req.getData();
            workerTaskDriverService.startStageReport(data);
            resp.success("");
        } catch (Exception e) {
            log.error("记录任务执行情况失败，data:{}", req.getData(), e);
            resp.fail(ResultCode.FAIL, e.getMessage());
        }
        return resp;

    }

    /**
     * worker告知scheduler将某个stage的数据准备就绪，包括创建db业务对象，以及拍摄上下文快照
     *
     * @param req
     * @return
     */
    @RequestMapping(path = URIs.SchedulerTaskProcessURIs.STAGE_PREPARE, method = RequestMethod.POST, produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public PrepareStageResp prepareStage(@RequestBody PrepareStageReq req) {
        PrepareStageResp prepareStageResp = new PrepareStageResp();

        PrepareStageReq.PrepareStageReqData data = req.getData();
        try {
            Tuple2<Long, String> stageExecutionIdAndParam = workerTaskDriverService.prepareForStage(data);
            Long stageExecutionId = stageExecutionIdAndParam._1();
            String encodedInput = stageExecutionIdAndParam._2();

            prepareStageResp.success(PrepareStageResp.PrepareStageRespData.builder()
                    .stageExecutionId(stageExecutionId)
                    .encodedInput(encodedInput)
                    .build());
        } catch (Exception e) {
            log.info("error when prepare stage:{} in taskExeId:{}", data == null ? "" : data.getStageName(), data == null ? "" :
                    data.getTaskExecutionId(), e);
            prepareStageResp.fail(ResultCode.FAIL, e.getMessage());
        }
        return prepareStageResp;
    }


    /**
     * 告知scheduler已经完成了某个stage
     *
     * @param req
     * @return
     */
    @RequestMapping(path = URIs.SchedulerTaskProcessURIs.STAGE_COMPLETE, method = RequestMethod.POST, produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HttpBody<String> completeStage(@RequestBody WorkerCompleteStageResultReq req) {
        HttpBody<String> resp = new HttpBody<>();
        try {
            for (WorkerCompleteStageResultReq.StageResult stageResult : req.getData().getStageResultList()) {
                workerTaskDriverService.completeStage(stageResult);
            }
            resp.success("");
        } catch (Exception e) {
            log.info("error when record task result", e);
            resp.fail(ResultCode.FAIL, e.getMessage());
        }
        return resp;
    }


    @RequestMapping(path = URIs.SchedulerTaskProcessURIs.STAGE_FAIL, method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public WorkerFailStageResp failStage(@RequestBody WorkerFailStageReq req) {
        WorkerFailStageResp resp = new WorkerFailStageResp();

        WorkerFailStageReq.WorkerFailStageReqData data = req.getData();
        try {
            workerTaskDriverService.failStageAndRetry(data.getTaskExecutionId(), data.getStageExecutionId(), data.getErrorMsg(),
                    data.isPausing());
            WorkerFailStageResp.WorkerFailStageReqData respData = new WorkerFailStageResp.WorkerFailStageReqData();
            resp.success(respData);
        } catch (Exception e) {
            log.error("stage:{}，无法正常失败", req, e);
            resp.fail(ResultCode.FAIL, e.getMessage());
        }
        return resp;
    }

    @RequestMapping(path = URIs.SchedulerTaskProcessURIs.RE_SCHEDULE, method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HttpBody<String> reScheduleTask(@RequestBody ReScheduleTaskReq req) {
        HttpBody<String> resp = new HttpBody<>();

        try {
            VerifyUtil.requireNotNull(req.getData(), "请求体为空");
            VerifyUtil.requireNotNull(req.getData().getTaskExecutionId(), "请求体的TaskExecutionId为空");
            workerTaskDriverService.rescheduleTask(req.getData().getTaskExecutionId());
            resp.success("");
        } catch (Exception e) {
            resp.fail(ResultCode.FAIL, e.getMessage());
        }
        return resp;
    }
}
