package log.service;

import log.dao.RequestLogDao;
import log.model.RequestLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author csieflyman
 */
@Slf4j
@Service("requestLogService")
public class RequestLogServiceImpl implements RequestLogService {

    @Autowired
    private RequestLogDao requestLogDao;

    @Transactional
    @Override
    public void create(RequestLog requestLog) {
        try {
            requestLogDao.create(requestLog);
        } catch (Throwable ee) {
            log.error("fail to save requestLog", ee);
        }
    }
}
