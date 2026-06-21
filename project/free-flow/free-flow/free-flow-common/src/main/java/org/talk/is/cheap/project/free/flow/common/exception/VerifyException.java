package org.talk.is.cheap.project.free.flow.common.exception;

import javax.annotation.CheckForNull;


/**
 * 抄的com.google.common.base.VerifyException，但是com.google.common.base.VerifyException他是个runtimeexception，对外调用的时候不友好。
 */
public class VerifyException extends Exception{

    public VerifyException() {
    }

    public VerifyException(@CheckForNull String message) {
        super(message);
    }

    public VerifyException(@CheckForNull Throwable cause) {
        super(cause);
    }

    public VerifyException(@CheckForNull String message, @CheckForNull Throwable cause) {
        super(message, cause);
    }
}
