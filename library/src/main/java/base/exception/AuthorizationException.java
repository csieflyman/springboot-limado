package base.exception;

import base.dto.ResponseCode;

/**
 * @author csieflyman
 */
public class AuthorizationException extends BaseException {

    public AuthorizationException(String message) {
        super(message, null, ResponseCode.REQUEST_FORBIDDEN);
    }
}
