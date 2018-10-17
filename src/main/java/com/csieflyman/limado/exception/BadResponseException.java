package com.csieflyman.limado.exception;

import com.csieflyman.limado.dto.ResponseCode;

/**
 * @author James Lin
 */
public class BadResponseException extends BaseException {

    private String body;

    public BadResponseException(String message, String serviceName, String url, String requestBody, String responseBody) {
        this(message, null, serviceName, url , requestBody, responseBody);
    }

    public BadResponseException(String message, Throwable cause, String serviceName, String url, String requestBody, String responseBody) {
        super(String.format("%s [service = %s, url = %s, requestBody = %s, responseBody = %s]", message, serviceName, url, requestBody, responseBody), cause, ResponseCode.SERVICE_BAD_RESPONSE);
        this.body = responseBody;
    }

    public String getBody() {
        return body;
    }
}
