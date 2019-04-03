package log.dao;

import base.dao.AbstractJPADaoImpl;
import log.model.RequestLog;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author csieflyman
 */
@Repository("requestLogDao")
public class RequestLogDaoImpl extends AbstractJPADaoImpl<RequestLog, Long> implements RequestLogDao {

    @PersistenceContext
    private EntityManager em;

    @PostConstruct
    public void init() {
        setEntityManager(em);
    }
}