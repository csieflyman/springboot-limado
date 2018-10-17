package com.csieflyman.limado.dao.jpa;

import com.csieflyman.limado.dao.GenericDao;
import com.csieflyman.limado.model.Identifiable;
import com.csieflyman.limado.util.query.Predicate;
import com.csieflyman.limado.util.query.Predicates;
import com.csieflyman.limado.util.query.QueryParams;
import io.ebean.ExpressionList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.EntityType;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * author flyman
 */
public abstract class JpaGenericDaoImpl<T extends Identifiable<ID>, ID extends Serializable> implements GenericDao<T, ID> {

    private static final Logger logger = LoggerFactory.getLogger(DagEdgeDaoImpl.class);

    @PersistenceContext
    EntityManager entityManager;

    private int batchSize = 20;

    protected Class<T> clazz;

    JpaGenericDaoImpl() {
        clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected T newInstance() {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getEntityName() {
        return clazz.getSimpleName();
    }

    @Override
    public T create(T entity) {
        entityManager.persist(entity);
        return entity;
    }

    @Override
    public void update(T entity) {
        entityManager.merge(entity);
    }

    @Override
    public void delete(T entity) {
        entityManager.remove(entity);
    }

    @Override
    public Optional<T> getById(ID id) {
        return Optional.ofNullable(entityManager.find(clazz, id));
    }

    private void where(ExpressionList<T> exprs, Predicates predicates) {
        Iterator<Predicate> iter = predicates.iterator();
        int count = 0;
        while(iter.hasNext()) {
            Predicate predicate = iter.next();
            String property = predicate.getProperty();
            Object value = predicate.getValue();
            switch (predicate.getOperator()) {
                case EQ:
                    exprs.eq(property, value);
                    break;
                case NE:
                    exprs.ne(property, value);
                    break;
                case GT:
                    exprs.gt(property, value);
                    break;
                case GE:
                    exprs.ge(property, value);
                    break;
                case LT:
                    exprs.lt(property, value);
                    break;
                case LE:
                    exprs.le(property, value);
                    break;
                case IN:
                    exprs.in(property, (Collection)value);
                    break;
                case LIKE:
                    exprs.like(property, (String)value);
                    break;
                case IS_NULL:
                    exprs.isNull(property);
                    break;
                case IS_NOT_NULL:
                    exprs.isNotNull(property);
                    break;
            }
            if(count < predicates.size() - 1) {
                exprs = predicates.isDisjunction() ? exprs.or() : exprs.and();
            }
            count++;
        }
    }

    @Override
    public List<T> find(QueryParams queryParams) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        StringBuilder sb = new StringBuilder();
        StringBuilder selectClause = new StringBuilder();
        StringBuilder fromClause = new StringBuilder();
        StringBuilder whereClause = new StringBuilder();
        StringBuilder orderByClause = new StringBuilder();

        EntityType entityType = entityManager.getMetamodel().entity(clazz);
        String entityAlias = SQLUtils.getEntityAlias(entityType);

        selectClause.append("select ");
        fromClause.append("from ").append(entityType.getName()).append(" ").append(entityAlias).append(" ");

        Set<String> fetchRelations = queryParams.getFetchRelations();
        if (!fetchRelations.isEmpty()) {
            boolean distinct = false;
            for (String relation : fetchRelations) {
                if (entityType.getAttribute(relation).isCollection()) {
                    distinct = true;
                    fromClause.append("left join fetch ");
                } else {
                    fromClause.append("inner join fetch ");
                }
                fromClause.append(entityAlias).append(".").append(relation).append(" ").append(relation).append(" ");
            }
            if (distinct) {
                selectClause.append("distinct ");
            }
        }

        Set<String> properties = queryParams.getPredicates().getPredicateProperties();
        for (String property : properties) {
            if (property.equalsIgnoreCase(entityAlias) || fetchRelations.contains(property))
                continue;
            if (entityType.getAttribute(property).isCollection()) {
                fromClause.append("left join ");
                fromClause.append(entityAlias).append(".").append(property).append(" ").append(property).append(" ");
            } else if (entityType.getAttribute(property).isAssociation()) {
                fromClause.append("inner join ");
                fromClause.append(entityAlias).append(".").append(property).append(" ").append(property).append(" ");
            }
        }

        selectClause.append(entityAlias).append(" ");

        if (!queryParams.getPredicates().isEmpty()) {
            whereClause.append("where ").append(SQLUtils.buildHqlWhereClause(entityType,
                    queryParams.getPredicates(), queryParams.isPredicatesDisjunction())).append(" ");
        }
        if (!queryParams.getOrderByList().isEmpty()) {
            orderByClause.append(SQLUtils.buildHqlOrderByClause(entityType, queryParams.getOrderByList()));
        }

        sb.append(selectClause.toString()).append(fromClause.toString()).append(whereClause.toString()).append(orderByClause);
        logger.debug(sb.toString());
        Query query = entityManager.createQuery(sb.toString());
        SQLUtils.setQueryParameterValue(query, queryParams.getPredicates());

        if (queryParams.getOffset() >= 0) {
            query.setFirstResult(queryParams.getOffset());
            query.setMaxResults(queryParams.getLimit());
        }

        List<T> result = query.getResultList();
        logger.debug(result.size());
        return result;
    }

    @Override
    public int findSize(QueryParams queryParams) {
        StringBuilder sb = new StringBuilder();
        StringBuilder selectClause = new StringBuilder();
        StringBuilder fromClause = new StringBuilder();
        StringBuilder whereClause = new StringBuilder();
        StringBuilder orderByClause = new StringBuilder();

        EntityType entityType = entityManager.getMetamodel().entity(clazz);
        String entityAlias = SQLUtils.getEntityAlias(entityType);

        selectClause.append("select count(*) ");
        fromClause.append("from ").append(entityType.getName()).append(" ").append(entityAlias).append(" ");

        Set<String> properties = queryParams.getPredicateProperties();
        for (String property : properties) {
            if (property.equalsIgnoreCase(entityAlias))
                continue;
            if (entityType.getAttribute(property).isCollection()) {
                fromClause.append("left join ");
                fromClause.append(entityAlias).append(".").append(property).append(" ").append(property).append(" ");
            } else if (entityType.getAttribute(property).isAssociation()) {
                fromClause.append("inner join ");
                fromClause.append(entityAlias).append(".").append(property).append(" ").append(property).append(" ");
            }
        }

        if (!queryParams.getPredicates().isEmpty()) {
            whereClause.append("where ").append(SQLUtils.buildHqlWhereClause(entityType,
                    queryParams.getPredicates(), queryParams.isPredicatesDisjunction())).append(" ");
        }

        sb.append(selectClause.toString()).append(fromClause.toString()).append(whereClause.toString()).append(orderByClause);
        logger.debug(sb.toString());
        Query query = entityManager.createQuery(sb.toString());
        SQLUtils.setQueryParameterValue(query, queryParams.getPredicates());
        Long size = (Long) query.getSingleResult();
        logger.debug("size = " + size);
        return size.intValue();
    }

    @Override
    public void batchCreate(Collection<T> entities) {
        if (entities.size() == 0)
            return;

        int count = 0;
        for (T entity : entities) {
            create(entity);
            if (++count % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
    }

    @Override
    public void batchUpdate(Collection<T> entities) {
        if (entities.size() == 0)
            return;

        int count = 0;
        for (T entity : entities) {
            update(entity);
            if (++count % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
    }

    @Override
    public void batchUpdate(Collection<ID> ids, Map<String, Object> updatedValueMap) {
        if (ids.size() == 0)
            return;

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String key : updatedValueMap.keySet()) {
            sb.append(key).append(" = :").append(key);
            count++;
            if (count != updatedValueMap.size())
                sb.append(", ");
        }

        String updateHQL = "update " + clazz.getSimpleName() + " set " + sb.toString() + " where id in (:ids)";
        int effectRows = batchExecute(updateHQL, ids, updatedValueMap);
        logger.debug(updateHQL + "; effectRows = " + effectRows);
        entityManager.flush();
        entityManager.clear();
    }

    @Override
    public void batchDelete(Collection<T> entities) {
        if (entities.size() == 0)
            return;

        int count = 0;
        for (T entity : entities) {
            delete(entity);
            if (++count % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
    }

    @Override
    public void batchDeleteById(Collection<ID> ids) {
        if (ids.size() == 0)
            return;

        String deleteHQL = "delete " + clazz.getSimpleName() + " where id in (:ids)";
        int effectRows = batchExecute(deleteHQL, ids, null);
        logger.debug(deleteHQL + "; effectRows = " + effectRows);
    }

    private int batchExecute(String hql, Collection<ID> ids, Map<String, Object> parameterMap) {
        int effectRows = 0;
        int index = 0;
        Set<ID> batchIdSet = new HashSet<>();
        for (Iterator<ID> i = ids.iterator(); i.hasNext(); ) {
            batchIdSet.add(i.next());
            index++;
            if (index == batchSize) {
                Query query = entityManager.createQuery(hql);
                query.setParameter("ids", batchIdSet);
                if (parameterMap != null) {
                    for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
                        query.setParameter(entry.getKey(), entry.getValue());
                    }
                }
                effectRows += query.executeUpdate();
                index = 0;
                batchIdSet.clear();
            }
        }
        if (index > 0) {
            Query query = entityManager.createQuery(hql);
            query.setParameter("ids", batchIdSet);
            if (parameterMap != null) {
                for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
                    query.setParameter(entry.getKey(), entry.getValue());
                }
            }
            effectRows += query.executeUpdate();
        }
        return effectRows;
    }
}