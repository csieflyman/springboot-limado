package base.exception;

import base.dto.ResponseCode;

/**
 * @author csieflyman
 */
public class InternalServerErrorException extends BaseException {

    public InternalServerErrorException(String message) {
        super(message, null);
    }

    public InternalServerErrorException(String message, Throwable cause) {
        super(message, cause, ResponseCode.INTERNAL_SERVER_ERROR);
    }
}
