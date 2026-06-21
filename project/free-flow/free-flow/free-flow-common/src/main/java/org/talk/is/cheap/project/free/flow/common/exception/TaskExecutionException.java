package org.talk.is.cheap.project.free.flow.common.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TaskExecutionException extends RuntimeException{
    @Getter
    private final Integer errorCode;

    public TaskExecutionException(Integer errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
