package log.service;

import base.dto.response.BatchResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author csieflyman
 */
public interface ErrorLogService {

    void create(HttpServletRequest request, Throwable e);

    void create(BatchResponse batchResponse, String api, String body);

    void create(String api, Throwable e);

    void create(String api, String body, Throwable e);
}
