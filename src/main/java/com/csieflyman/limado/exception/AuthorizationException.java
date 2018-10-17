package com.csieflyman.limado.exception;

import com.csieflyman.limado.dto.ResponseCode;

/**
 * @author James Lin
 */
public class AuthorizationException extends BaseException {

    public AuthorizationException(String message) {
        super(message, null, ResponseCode.REQUEST_FORBIDDEN);
    }
}
