package auth;

import base.dto.ResponseCode;
import base.exception.BaseException;

/**
 * @author csieflyman
 */
public class AuthenticationException extends BaseException {

    public AuthenticationException(String message) {
        super(message, null, ResponseCode.UNAUTHENTICATED);
    }
}
