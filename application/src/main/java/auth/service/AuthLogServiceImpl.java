package auth.service;

import auth.dao.AuthLogDao;
import auth.model.AuthLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author csieflyman
 */
public class AuthLogServiceImpl implements AuthLogService {

    @Autowired
    private AuthLogDao authLogDao;

    @Transactional
    @Override
    public void logAuth(AuthLog authLog) {
        authLogDao.create(authLog);
    }
}