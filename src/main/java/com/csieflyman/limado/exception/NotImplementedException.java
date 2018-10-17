package com.csieflyman.limado.exception;

import com.csieflyman.limado.dto.ResponseCode;

/**
 * @author James Lin
 */
public class NotImplementedException extends BaseException {

    public NotImplementedException(String message) {
        super(message, null, ResponseCode.INTERNAL_SERVER_ERROR);
    }
}
