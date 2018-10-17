package com.csieflyman.limado.exception;

import com.csieflyman.limado.dto.ResponseCode;

/**
 * @author James Lin
 */
public class ObjectDuplicateException extends BaseException {

    public ObjectDuplicateException(String message) {
        super(message, null, ResponseCode.REQUEST_RESOURCE_CONFLICT);
    }
}
