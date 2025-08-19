package org.talk.is.cheap.project.free.flow.starter.worker.task.driver.service;

import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.TaskDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.service.LocalTaskDefinitionService;


/**
 * 如何驱动一个task的运行？分为两步：
 * 1. scheduler寻找可以运行当前任务的worker：通过hash分片找到一个worker，与这个worker进行交互询问是否可以支持分配
 * 2. 如果可以，scheduler告知这个worker启动这个task，由task实际启动执行。执行过程中将结果返回至scheduler
 */
@Slf4j
@Service
public class TaskDriverService {

    @Autowired
    private LocalTaskDefinitionService localTaskDefinitionService;

    /**
     * 本worker是否能运行某个task，如果taskVersion为null，则不关心具体是什么版本
     *
     * @param taskName
     * @param taskVersion
     * @return
     */
    public boolean canRunTask(String taskName, Integer taskVersion) {

        return localTaskDefinitionService.hasTask(taskName, taskVersion);

    }

    /**
     * 驱动一个taskName，如果taskVersion为null，则不关心具体什么版本
     *
     * @param taskName
     * @param taskVersion
     * @return
     */
    public boolean runTask(String taskName, Integer taskVersion) {
        VerifyUtil.shallBeTrue(canRunTask(taskName, taskVersion),
                String.format("worker can't ran run task: %s, version: %d", taskName, taskVersion));

        return true;
    }

}
