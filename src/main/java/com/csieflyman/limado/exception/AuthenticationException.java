package com.csieflyman.limado.exception;

import com.csieflyman.limado.dto.ResponseCode;

/**
 * @author James Lin
 */
public class AuthenticationException extends BaseException {

    public AuthenticationException(String message) {
        super(message, null, ResponseCode.REQUEST_UNAUTHORIZED);
    }
}
