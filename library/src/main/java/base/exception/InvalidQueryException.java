package base.exception;

import base.dto.ResponseCode;

/**
 * @author csieflyman
 */
public class InvalidQueryException extends BaseException {

    public InvalidQueryException(String message) {
        super(message, null, ResponseCode.REQUEST_BAD_DATA);
    }
}
