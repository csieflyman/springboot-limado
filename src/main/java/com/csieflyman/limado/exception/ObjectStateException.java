package com.csieflyman.limado.exception;

import com.csieflyman.limado.dto.ResponseCode;

/**
 * @author James Lin
 */
public class ObjectStateException extends BaseException {

    public ObjectStateException(String message) {
        super(message, null, ResponseCode.REQUEST_RESOURCE_CONFLICT);
    }
}
