package base.exception;

import base.dto.ResponseCode;

/**
 * @author csieflyman
 */
public class ObjectDuplicateException extends BaseException {

    public ObjectDuplicateException(String message) {
        super(message, null, ResponseCode.REQUEST_RESOURCE_CONFLICT);
    }
}
