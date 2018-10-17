package com.csieflyman.limado.exception;

import com.csieflyman.limado.dto.ResponseCode;

/**
 * @author James Lin
 */
public class ObjectNotFoundException extends BaseException {

    public ObjectNotFoundException(String message) {
        super(message, null, ResponseCode.REQUEST_BAD_DATA);
    }
}
