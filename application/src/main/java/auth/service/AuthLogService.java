package auth.service;

import auth.model.AuthLog;
/**
 * @author csieflyman
 */
public interface AuthLogService {

    void logAuth(AuthLog authLog);

}