package log.service;

import log.dao.AuthLogDao;
import log.model.AuthLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author csieflyman
 */
@Slf4j
@Service("authLogService")
public class AuthLogServiceImpl implements AuthLogService {

    @Autowired
    private AuthLogDao authLogDao;

    @Transactional
    @Override
    public void logAuth(AuthLog authLog) {
        authLogDao.create(authLog);
    }
}