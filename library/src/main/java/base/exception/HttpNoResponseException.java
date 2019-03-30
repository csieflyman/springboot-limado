package base.exception;

import base.dto.ResponseCode;

/**
 * @author csieflyman
 */
public class HttpNoResponseException extends BadResponseException {

    public HttpNoResponseException(Throwable cause, String serviceName, String url, String requestBody, ResponseCode responseCode) {
        super(String.format("No Response [service = %s, url = %s, requestBody = %s]", serviceName, url, requestBody), cause, serviceName, null, responseCode);
    }
}
