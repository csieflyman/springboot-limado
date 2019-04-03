package log.dao;

import log.model.AuthLog;
import base.dao.AbstractJPADaoImpl;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author csieflyman
 */
@Repository("authLogDao")
public class AuthLogDaoImpl extends AbstractJPADaoImpl<AuthLog, Long> implements AuthLogDao {

    @PersistenceContext
    private EntityManager em;

    @PostConstruct
    public void init() {
        setEntityManager(em);
    }
}