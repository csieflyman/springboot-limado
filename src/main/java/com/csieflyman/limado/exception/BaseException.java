package com.csieflyman.limado.exception;

import com.csieflyman.limado.dto.ResponseCode;

/**
 * @author James Lin
 */
public abstract class BaseException extends RuntimeException {

    protected ResponseCode responseCode;

    public BaseException(String message, Throwable cause, ResponseCode responseCode) {
        super(message, cause);
        this.responseCode = responseCode;
    }

    public ResponseCode getResponseCode() {
        return responseCode;
    }
}
