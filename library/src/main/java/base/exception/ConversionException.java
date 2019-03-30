package base.exception;

import base.dto.ResponseCode;

/**
 * @author csieflyman
 */
public class ConversionException extends BaseException {

    public ConversionException(String message) {
        this(message, null);
    }

    public ConversionException(String message, Throwable cause) {
        super(message, cause, ResponseCode.REQUEST_BAD_DATA);
    }
}
