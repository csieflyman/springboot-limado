package log.dao;

import base.dao.AbstractJPADaoImpl;
import log.model.ErrorLog;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author csieflyman
 */
@Repository("errorLogDao")
public class ErrorLogDaoImpl extends AbstractJPADaoImpl<ErrorLog, Long> implements ErrorLogDao {

    @PersistenceContext
    private EntityManager em;

    @PostConstruct
    public void init() {
        setEntityManager(em);
    }
}
