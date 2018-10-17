package com.csieflyman.limado.dto;

/**
 * @author James Lin
 */
public enum ResponseCode {

    SUCCESS(200, "0000", "SUCCESS"),
    REQUEST_BAD_DATA(400, "1000", "[Bad Request]"),
    REQUEST_UNAUTHORIZED(401, "1001", "[Unauthorized]"),
    REQUEST_FORBIDDEN(403, "1002", "[Forbidden]"),
    REQUEST_RESOURCE_CONFLICT(409, "1003", "[Conflict]"),
    REQUEST_BATCH_FAILURE(200, "1003", "[Batch Processing Failure]"),
    SERVICE_NO_RESPONSE(500, "2000", "[Service No Response]"),
    SERVICE_BAD_RESPONSE(500, "2001", "[Service Bad Response]"),
    SERVICE_NOT_FOUND_RESPONSE(404, "0000", "[Service 404 Response]"),
    INTERNAL_SERVER_ERROR(500, "9999", "[Internal Server Error]");

    private int statusCode;
    private String code;
    private String message;

    ResponseCode(int statusCode, String code, String message){
        this.statusCode = statusCode;
        this.code = code;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
