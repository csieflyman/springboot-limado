package log.service;

import log.model.RequestLog;

/**
 * @author csieflyman
 */
public interface RequestLogService {

    void create(RequestLog requestLog);
}
