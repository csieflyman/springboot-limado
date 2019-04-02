package base.dto;

import lombok.Getter;

/**
 * @author csieflyman
 */
public enum ResponseCode {

    SUCCESS(200, "0000", false),
    INTERNAL_SERVER_ERROR(500, "9999", true),

    // 通用錯誤 - Request
    REQUEST_BAD_DATA(400, "1000", false),
    UNAUTHENTICATED(401, "1001", false),
    REQUEST_FORBIDDEN(403, "1002", false),
    REQUEST_RESOURCE_CONFLICT(409, "1003", false),
    REQUEST_BATCH_FAILURE(200, "1004", true);

    @Getter
    private int statusCode;
    @Getter
    private String code;
    @Getter
    private String message;
    @Getter
    private boolean logError;

    ResponseCode(int statusCode, String code, boolean logError){
        this.statusCode = statusCode;
        this.code = code;
        this.message = name();
        this.logError = logError;
    }
}
