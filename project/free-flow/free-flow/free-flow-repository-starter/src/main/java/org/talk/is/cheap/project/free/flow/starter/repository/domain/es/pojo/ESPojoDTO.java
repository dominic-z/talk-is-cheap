package org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ESPojoDTO <T>{
    private String id;
    private T data;
}
