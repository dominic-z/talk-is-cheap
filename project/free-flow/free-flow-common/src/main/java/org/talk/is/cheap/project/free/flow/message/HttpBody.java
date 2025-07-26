package org.talk.is.cheap.project.free.flow.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpBody<T> {
    private Integer code;
    private String msg;
    private T data;
}
