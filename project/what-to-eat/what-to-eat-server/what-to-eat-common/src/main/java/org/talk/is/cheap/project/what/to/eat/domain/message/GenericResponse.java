package org.talk.is.cheap.project.what.to.eat.domain.message;

import lombok.Data;

@Data
public class GenericResponse<T> {

    public int code;
    public String message;
    public T data;

}
