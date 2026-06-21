package org.talk.is.cheap.project.free.flow.starter.repository.domain.es.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ESPojoDTO <T>{
    private String id;
    private List<Object> sort;
    private T data;
}
