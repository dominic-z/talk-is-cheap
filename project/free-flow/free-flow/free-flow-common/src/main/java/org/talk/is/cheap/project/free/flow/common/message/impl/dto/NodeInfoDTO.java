package org.talk.is.cheap.project.free.flow.common.message.impl.dto;

import lombok.Data;

@Data
public class NodeInfoDTO {

    private String nodeAddress;
    private Integer nodeType;
    private boolean isLeader;
}
