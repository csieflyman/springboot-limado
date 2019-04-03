package auth.dao;

import auth.model.AuthLog;
import base.dao.AbstractJPADaoImpl;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author csieflyman
 */
public class AuthLogDaoImpl extends AbstractJPADaoImpl<AuthLog, Long> implements AuthLogDao {

    @PersistenceContext
    private EntityManager em;

    @PostConstruct
    public void init() {
        setEntityManager(em);
    }
}