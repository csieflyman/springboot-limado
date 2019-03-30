package base.exception;

import base.dto.ResponseCode;

/**
 * @author csieflyman
 */
public class NotImplementedException extends BaseException {

    public NotImplementedException(String message) {
        super(message, null, ResponseCode.INTERNAL_SERVER_ERROR);
    }
}
