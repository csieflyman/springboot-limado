package base.exception;

import base.dto.ResponseCode;

/**
 * @author csieflyman
 */
public class ObjectStateException extends BaseException {

    public ObjectStateException(String message) {
        this(message, ResponseCode.REQUEST_RESOURCE_CONFLICT);
    }

    public ObjectStateException(String message, ResponseCode code) {
        this(message, code, null);
    }

    public ObjectStateException(String message, ResponseCode code, Object result) {
        super(message, null, code, result);
    }
}
