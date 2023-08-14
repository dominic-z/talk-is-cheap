package org.talk.is.cheap.project.what.to.eat.exceptions;

import lombok.Getter;

public class VerificationException extends Exception {

    @Getter
    private final int errorCode;

    public VerificationException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;

    }
}
