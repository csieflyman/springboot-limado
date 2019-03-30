package base.dao;

import base.exception.InvalidEntityException;
import base.exception.InvalidQueryException;
import base.model.Identifiable;
import base.util.query.Junction;
import base.util.query.Predicate;
import base.util.query.Query;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author csieflyman
 */
@Slf4j
public abstract class AbstractJPADaoImpl<T extends Identifiable<ID>, ID extends Serializable> implements GenericDao<T, ID> {

    @PersistenceContext
    protected EntityManager em;

    protected Class<T> clazz;

    public AbstractJPADaoImpl() {
        clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected T newInstance() {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new InvalidEntityException(String.format("fail to initialize instance %s", clazz.getName()), e);
        }
    }

    protected String getEntityName() {
        return clazz.getSimpleName();
    }

    @Override
    public T create(T entity) {
        em.persist(entity);
        return entity;
    }

    @Override
    public void update(T entity) {
        em.merge(entity);
    }

    @Override
    public void delete(T entity) {
        em.remove(entity);
    }

    @Override
    public int executeUpdate(Map<String, Object> valueMap, Junction junction) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate<T> updateQuery = cb.createCriteriaUpdate(clazz);
        valueMap.forEach(updateQuery::set);
        updateQuery.where(toJPAPredicate(cb, updateQuery.getRoot(), junction));
        return em.createQuery(updateQuery).executeUpdate();
    }

    @Override
    public int executeDelete(Junction junction) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<T> deleteQuery = cb.createCriteriaDelete(clazz);
        deleteQuery.where(toJPAPredicate(cb, deleteQuery.getRoot(), junction));
        return em.createQuery(deleteQuery).executeUpdate();
    }

    @Override
    public Optional<T> getById(ID id) {
        return Optional.ofNullable(em.find(clazz, id));
    }

    @Override
    public Optional<T> findOne(Query query) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> jpaQuery = toJpaQuery(query, cb);
        try {
            Tuple tuple = em.createQuery(jpaQuery).getSingleResult();
            Map<String, Object> map = tuple.getElements().stream().collect(Collectors.toMap(TupleElement::getAlias, tuple::get));
            return Optional.of(JPAUtils.toEntity(clazz, Arrays.asList(map)).get(0));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<T> find(Query query) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> jpaQuery = toJpaQuery(query, cb);
        TypedQuery<Tuple> typedQuery = em.createQuery(jpaQuery);
        if(query.isPagingQuery()) {
            typedQuery.setFirstResult((query.getPageNo() - 1) * query.getPageSize());
            typedQuery.setMaxResults(query.getPageSize());
        }
        List<Tuple> tuples = typedQuery.getResultList();
        List<Map<String, ?>> results = tuples.stream().map(tuple -> tuple.getElements().stream().collect(Collectors.toMap(TupleElement::getAlias, tuple::get))).collect(Collectors.toList());
        List<T> entities = JPAUtils.toEntity(clazz, results);
        log.debug("entities size = {}", entities.size());
        return entities;
    }

    @Override
    public long findSize(Query query) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<T> root = countQuery.from(clazz);
        countQuery.select(cb.count(root));
        countQuery.where(toJPAPredicate(cb, root, query.getJunction()));
        Long count = em.createQuery(countQuery).getSingleResult();
        log.debug("entities size = {}", count);
        return count;
    }

    @Override
    public Set<ID> findIds(Query query) {
        query.fetchProperties(JPAUtils.getIdPropertyName(clazz));
        return find(query).stream().map(Identifiable::getId).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private CriteriaQuery<Tuple> toJpaQuery(Query query, CriteriaBuilder cb) {
        CriteriaQuery<Tuple> jpaQuery = cb.createTupleQuery();
        Root<T> root = jpaQuery.from(clazz);

        if(query.getFetchProperties().isEmpty()) {
            jpaQuery.multiselect(JPAUtils.getPropertyNames(clazz).stream().map(p -> (Selection<?>)toPath(root, p).alias(p)).collect(Collectors.toList()));
        }
        else {
            jpaQuery.multiselect(query.getFetchProperties().stream().map(p -> (Selection<?>)toPath(root, p).alias(p)).collect(Collectors.toList()));
        }
        if(!query.getFetchRelations().isEmpty()) {
            jpaQuery.multiselect(query.getFetchRelations().stream().map(r -> (Selection<?>)toJoin(root, r).alias(r)).collect(Collectors.toList()));
        }

        jpaQuery.where(toJPAPredicate(cb, root, query.getJunction()));

        if(!query.isOrderByEmpty()) {
            jpaQuery.orderBy(query.getOrderByList().stream().map(orderBy -> orderBy.isAsc() ?
                    cb.asc(toPath(root, orderBy.getProperty())) : cb.desc(toPath(root, orderBy.getProperty()))).collect(Collectors.toList()));
        }
        return jpaQuery;
    }

    private javax.persistence.criteria.Predicate toJPAPredicate(CriteriaBuilder cb, Root<T> root, Junction junction) {
        javax.persistence.criteria.Predicate[] pArray = junction.getPredicates().stream().map(p -> ToJPAPredicate(cb, root, p))
                .toArray(javax.persistence.criteria.Predicate[]::new);
        return junction.isConjunction() ? cb.and(pArray) : cb.or(pArray);
    }

    private javax.persistence.criteria.Predicate ToJPAPredicate(CriteriaBuilder cb, Root<T> root, Predicate predicate) {
        if(predicate.isLiteralSql()) {
            throw new InvalidQueryException("literal sql is unsupported: " + predicate);
        }
        else if(predicate.isJunction()) {
            return toJPAPredicate(cb, root, (Junction) predicate);
        }
        else {
            String property = predicate.getProperty();
            Object value = predicate.getValue();
            switch (predicate.getOperator()) {
                case EQ:
                    return cb.equal(toPath(root, property), value);
                case NE:
                    return cb.notEqual(toPath(root, property), value);
                case GT:
                    return cb.greaterThan(toPath(root, property), (Comparable) value);
                case GE:
                    return cb.greaterThanOrEqualTo(toPath(root, property), (Comparable) value);
                case LT:
                    return cb.lessThan(toPath(root, property), (Comparable) value);
                case LE:
                    return cb.lessThanOrEqualTo(toPath(root, property), (Comparable) value);
                case IN:
                    return toPath(root, property).in((Collection) value);
                case LIKE:
                    return cb.like(toPath(root, property), (String)value);
                case IS_NULL:
                    return cb.isNull(toPath(root, property));
                case IS_NOT_NULL:
                    return cb.isNotNull(toPath(root, property));
                default:
                    throw new InvalidQueryException("invalid predicate operator: " + predicate);
            }
        }
    }

    private Path toPath(Root<T> root, String propertyPath) {
        String[] segments = propertyPath.split("\\.");
        Path path = root.get(segments[0]);
        if(segments.length > 1) {
            for(int i = 1; i < segments.length; i++) {
                path = path.get(segments[i]);
            }
        }
        return path;
    }

    private Join toJoin(Root<T> root, String relationPath) {
        String[] segments = relationPath.split("\\.");
        Join join = Collection.class.isAssignableFrom(root.get(segments[0]).getJavaType()) ?
                root.join(segments[0], JoinType.LEFT) : root.join(segments[0]);
        if(segments.length > 1) {
            for(int i = 1; i < segments.length; i++) {
                join = Collection.class.isAssignableFrom(join.get(segments[i]).getJavaType()) ?
                        join.join(segments[0], JoinType.LEFT) : join.join(segments[0]);
            }
        }
        return join;
    }
}