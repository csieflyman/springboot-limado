package log.service;

import log.model.AuthLog;
/**
 * @author csieflyman
 */
public interface AuthLogService {

    void logAuth(AuthLog authLog);

}