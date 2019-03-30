package log.dao;

import base.dao.AbstractJPADaoImpl;
import log.model.ErrorLog;
import org.springframework.stereotype.Repository;

/**
 * @author csieflyman
 */
@Repository("errorLogDao")
public class ErrorLogDaoImpl extends AbstractJPADaoImpl<ErrorLog, Long> implements ErrorLogDao {
}
