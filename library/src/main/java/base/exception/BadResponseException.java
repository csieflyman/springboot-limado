package base.exception;

import base.dto.ResponseCode;

/**
 * @author csieflyman
 */
public class BadResponseException extends BaseException {

    private String serviceName;
    private String responseBody;

    public BadResponseException(String message, String serviceName, ResponseCode responseCode) {
        this(message, null, serviceName, null, responseCode);
    }

    public BadResponseException(String message, Throwable cause, String serviceName, String responseBody, ResponseCode responseCode) {
        super(String.format("[%s] %s ", serviceName, message), cause, responseCode);
        this.serviceName = serviceName;
        this.responseBody = responseBody;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
