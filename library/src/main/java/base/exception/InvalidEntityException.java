package base.exception;

import base.dto.ResponseCode;

/**
 * @author csieflyman
 */
public class InvalidEntityException extends BaseException {

    public InvalidEntityException(String message) {
        this(message, null);
    }

    public InvalidEntityException(String message, Throwable cause) {
        super(message, cause, ResponseCode.INTERNAL_SERVER_ERROR);
    }
}
