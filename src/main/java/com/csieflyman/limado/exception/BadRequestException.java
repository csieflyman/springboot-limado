package com.csieflyman.limado.exception;

import com.csieflyman.limado.dto.ResponseCode;

import javax.validation.ConstraintViolation;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author James Lin
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

    public BadRequestException(String message, String body, Object formObject) {
        this(message, body, formObject, null);
    }

    public <T> BadRequestException(Set<ConstraintViolation<T>> violations, String body, Object formObject) {
        this(violations.stream().map(violation -> violation.getPropertyPath() + ":" + violation.getMessage())
                .collect(Collectors.joining(",")), body, formObject, null);
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
