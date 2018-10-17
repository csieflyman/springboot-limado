package com.csieflyman.limado.exception;

import com.csieflyman.limado.dto.ResponseCode;

/**
 * @author James Lin
 */
public class CipherException extends BaseException {

    public CipherException(String message, Throwable cause) {
        super(message, cause, ResponseCode.INTERNAL_SERVER_ERROR);
    }
}
