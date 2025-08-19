package org.talk.is.cheap.project.free.flow.common.message.impl;

import lombok.Builder;
import lombok.Data;
import org.talk.is.cheap.project.free.flow.common.message.HttpBody;
import org.talk.is.cheap.project.free.flow.common.message.impl.vo.TaskDefinitionVO;

import java.util.List;


public class GetTaskDefinitionResp extends HttpBody<List<TaskDefinitionVO>> {

}
