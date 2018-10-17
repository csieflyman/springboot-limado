package com.csieflyman.limado.exception;

import com.csieflyman.limado.dto.ResponseCode;

/**
 * @author James Lin
 */
public class InvalidEntityException extends BaseException {

    public InvalidEntityException(String message) {
        this(message, null);
    }

    public InvalidEntityException(String message, Throwable cause) {
        super(message, cause, ResponseCode.INTERNAL_SERVER_ERROR);
    }
}
