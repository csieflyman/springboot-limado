package base.exception;

import base.dto.ResponseCode;

/**
 * @author csieflyman
 */
public class HttpBadResponseException extends BadResponseException {

    private Integer status;

    public HttpBadResponseException(String message, Integer status, String serviceName, String url, String requestBody, String responseBody, ResponseCode responseCode) {
        this(message, null, status, serviceName, url , requestBody, responseBody, responseCode);
    }

    public HttpBadResponseException(String message, Throwable cause, Integer status, String serviceName, String url, String requestBody, String responseBody, ResponseCode responseCode) {
        super(String.format("[%s] %s [service = %s, url = %s, requestBody = %s, responseBody = %s]", status, message, serviceName, url, requestBody, responseBody), cause, serviceName, responseBody, responseCode);
    }

    public Integer getStatus() {
        return status;
    }
}
