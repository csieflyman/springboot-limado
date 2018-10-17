package com.csieflyman.limado.exception;

import com.csieflyman.limado.dto.ResponseCode;

/**
 * @author James Lin
 */
public class InternalServerErrorException extends BaseException {

    public InternalServerErrorException(String message) {
        this(message, null);
    }

    public InternalServerErrorException(String message, Throwable cause) {
        super(message, cause, ResponseCode.INTERNAL_SERVER_ERROR);
    }
}
