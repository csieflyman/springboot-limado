package com.csieflyman.limado.exception;

import com.csieflyman.limado.dto.ResponseCode;

/**
 * @author James Lin
 */
public class InvalidQueryException extends BaseException {

    public InvalidQueryException(String message) {
        super(message, null, ResponseCode.REQUEST_BAD_DATA);
    }
}
