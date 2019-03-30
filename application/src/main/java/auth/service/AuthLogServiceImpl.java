package auth.service;

import auth.dao.AuthLogDao;
import auth.model.AuthLog;
import base.util.db.EbeanTransactional;
import com.google.inject.Inject;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author csieflyman
 */
public class AuthLogServiceImpl implements AuthLogService {

    @Inject
    private AuthLogDao authLogDao;

    @Transactional
    @Override
    public void logAuth(AuthLog authLog) {
        authLogDao.create(authLog);
    }
}