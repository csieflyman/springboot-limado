package base.exception;

import base.dto.ResponseCode;

/**
 * @author csieflyman
 */
public class BadRequestException extends BaseException {

    private String body;

    private Object formObject;

    public BadRequestException(String message) {
        this(message, null);
    }

    public BadRequestException(String message, String body) {
        this(message, body, null);
    }

    public BadRequestException(String message, String body, Throwable cause) {
        this(message, body, null, cause);
    }

    public BadRequestException(String message, String body, Object formObject) {
        this(message, body, formObject, null);
    }

    public BadRequestException(String message, String body, Object formObject, Throwable cause) {
        super(message, cause, ResponseCode.REQUEST_BAD_DATA);
        this.body = body;
        this.formObject = formObject;
    }

    public String getBody() {
        return body;
    }

    public Object getFormObject() {
        return formObject;
    }
}
