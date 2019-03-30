package base.exception;

import base.dto.ResponseCode;

/**
 * @author csieflyman
 */
public class ObjectNotFoundException extends BaseException {

    public ObjectNotFoundException(String message) {
        super(message, null, ResponseCode.REQUEST_BAD_DATA);
    }
}
