package com.csieflyman.limado.exception;

import com.csieflyman.limado.dto.ResponseCode;

/**
 * @author James Lin
 */
public class DataBindingException extends BaseException {

    public DataBindingException(String message, Throwable cause) {
        super(message, cause, ResponseCode.INTERNAL_SERVER_ERROR);
    }
}
