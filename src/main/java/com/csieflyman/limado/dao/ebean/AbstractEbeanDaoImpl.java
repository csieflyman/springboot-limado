package com.csieflyman.limado.dao.ebean;

import com.csieflyman.limado.dao.GenericDao;
import com.csieflyman.limado.dao.SQLUtils;
import com.csieflyman.limado.exception.InvalidEntityException;
import com.csieflyman.limado.model.Identifiable;
import com.csieflyman.limado.util.query.*;
import com.csieflyman.limado.util.query.OrderBy;
import com.google.common.base.CaseFormat;
import io.ebean.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @author csieflyman
 */
public abstract class AbstractEbeanDaoImpl<T extends Identifiable<ID>, ID> implements GenericDao<T, ID> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractEbeanDaoImpl.class);

    protected Class<T> clazz;

    public AbstractEbeanDaoImpl() {
        clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected T newInstance() {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new InvalidEntityException(String.format("fail to initialize instance %s", clazz.getName()), e);
        }
    }

    protected EbeanServer getEbeanServer() {
        return Ebean.getDefaultServer();
    }

    @Override
    public T create(T entity) {
        logger.debug("create entity {} : {}", clazz.getName(), entity);

        getEbeanServer().save(entity);
        return entity;
    }

    @Override
    public void update(T entity) {
        logger.debug("update entity {} : {}", clazz.getName(), entity);

        getEbeanServer().update(entity);
    }

    @Override
    public void delete(T entity) {
        logger.debug("delete entity {} : {}", clazz.getName(), entity);

        getEbeanServer().delete(entity);
    }

    @Override
    public int executeUpdate(Map<String, Object> valueMap, Predicates predicates) {
        UpdateQuery<T> updateQuery = getEbeanServer().update(clazz);
        for(String key: valueMap.keySet()) {
            Object value = valueMap.get(key);
            if(value == null) {
                updateQuery.setNull(key);
            }
            else {
                updateQuery.set(key, value);
            }
        }
        ExpressionList<T> exprs = updateQuery.where();
        where(exprs, predicates);
        return exprs.update();
    }

    @Override
    public int executeDelete(Predicates predicates) {
        String deleteSql = "delete from " + getTableName() + " " +
                SQLUtils.buildWhereClause(getTableAlias(), predicates);
        Update update = getEbeanServer().createUpdate(clazz, deleteSql);
        for (Predicate predicate : predicates) {
            if (Operator.isNoValue(predicate.getOperator()))
                continue;
            update.setParameter(predicate.getQueryParameterName(), predicate.getQueryParameterValue());
        }
        return update.execute();
    }

    protected int execute(String sql, Map<String, Object> parameterMap) {
        Update update = getEbeanServer().createUpdate(clazz, sql);
        for(String parameter: parameterMap.keySet()) {
            update.setParameter(parameter, parameterMap.get(parameter));
        }
        return update.execute();
    }

    @Override
    public Optional<T> getById(ID id) {
        logger.debug("{} : getById ; id = {}", clazz.getName(), id);
        return Optional.ofNullable(getEbeanServer().find(clazz, id));
    }

    @Override
    public Optional<T> findOne(QueryParams params) {
        logger.debug("{} : findOne ; params = {}", clazz.getName(), params);
        Query<T> query = getEbeanServer().find(clazz);
        query.setDisableLazyLoading(true);
        query = fetchProperties(query, params);
        query = fetchRelations(query, params);
        where(query.where(), params.getPredicates());
        return query.findOneOrEmpty();
    }

    @Override
    public List<T> find(QueryParams params) {
        logger.debug("{} : find ; params = {}", clazz.getName(), params);
        Query<T> query = getEbeanServer().find(clazz);
        query.setDisableLazyLoading(true);
        query = fetchProperties(query, params);
        query = fetchRelations(query, params);
        where(query.where(), params.getPredicates());
        orderBy(query, params.getOrderByList());
        return findWithPaging(query, params);
    }

    @Override
    public int findSize(QueryParams params) {
        boolean isOnlySize = params.isOnlySize();
        params.setOnlySize(true);
        logger.debug("{} : findSize ; params = {}", clazz.getName(), params);
        Query<T> query = getEbeanServer().find(clazz);
        where(query.where(), params.getPredicates());
        int count = query.findCount();
        params.setOnlySize(isOnlySize);
        return count;
    }

    private List<T> findWithPaging(Query<T> query, QueryParams params) {
        List<T> results;
        if(params.isPagingQuery()) {
            results = query.setFirstRow((params.getPageNo() - 1) * params.getPageSize())
                    .setMaxRows(params.getPageSize()).findPagedList().getList();
        }
        else {
            results = query.findList();
        }
        return results;
    }

    private Query<T> fetchProperties(Query<T> query, QueryParams params) {
        if(!params.getFetchProperties().isEmpty()) {
            String fetchPropertiesString = params.getFetchProperties().stream()
                    .filter(p -> !p.contains(".")).collect(Collectors.joining(","));
            logger.debug(String.format("fetch properties = %s", fetchPropertiesString));
            return query.select(fetchPropertiesString);
        }
        return query;
    }

    private Query<T> fetchRelations(Query<T> query, QueryParams params) {
        Set<String> fetchRelations = new HashSet<>(params.getFetchRelations());
        Map<String, Set<String>> relationPropertiesMap = params.getFetchProperties().stream().filter(p -> p.contains("."))
                .collect(Collectors.groupingBy(p -> p.substring(0, p.lastIndexOf(".")),
                        Collectors.mapping(p -> p.substring(p.lastIndexOf(".") + 1), Collectors.toSet())));
        fetchRelations.addAll(relationPropertiesMap.keySet());
        for(String fetchRelation: fetchRelations) {
            if(relationPropertiesMap.containsKey(fetchRelation)) {
                String fetchRelationProperties = relationPropertiesMap.get(fetchRelation).stream().collect(Collectors.joining(","));
                logger.debug(String.format("fetch relation %s => fetch properties = %s", fetchRelation, fetchRelationProperties));
                query = query.fetch(fetchRelation, fetchRelationProperties);
            }
            else {
                logger.debug(String.format("fetch relation = %s", fetchRelation));
                query = query.fetch(fetchRelation);
            }
        }
        return query;
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

    private void orderBy(Query query, Set<OrderBy> orderByList) {
        for(OrderBy orderBy: orderByList) {
            if(orderBy.isAsc()) {
                query.orderBy().asc(orderBy.getProperty());
            }
            else {
                query.orderBy().desc(orderBy.getProperty());
            }
        }
    }

    protected List<T> findByRawSql(QueryParams params, String fromStatement) {
        String rawSql = SQLUtils.buildSelectClause(getTableAlias(), clazz, params) + " from " + fromStatement;
        logger.debug("findByRawSql = " + rawSql);
        RawSqlBuilder builder = RawSqlBuilder.parse(rawSql);
        buildTableAliasMapping(builder, params);
        Query<T> query = Ebean.find(clazz).setRawSql(builder.create());
        query.setDisableLazyLoading(true);
        where(query.where(), params.getPredicates());
        orderBy(query, params.getOrderByList());
        return findWithPaging(query, params);
    }

    private void buildTableAliasMapping(RawSqlBuilder builder, QueryParams params) {
        Set<String> relations = new HashSet<>(params.getFetchRelations());
        for(String property: params.getFetchProperties()) {
            if(property.contains(".")) {
                relations.add(property.substring(0, property.lastIndexOf(".")));
            }
        }
        // 目前不確定 predicate 裡面的 relation 是否要加入 mapping
        //params.getPredicates().stream().map(p -> p.getProperty()).collect(Collectors.toSet());
        for(String relation: relations) {
            String[] segments = relation.split("\\.");
            if(segments.length == 1) {
                logger.debug(String.format("tableAliasMapping %s => %s", relation, relation));
                builder.tableAliasMapping(relation, relation);
            }
            else {
                for (int i = 0; i < segments.length; i++) {
                    int endIndex = StringUtils.ordinalIndexOf(relation, ".", i + 1);
                    endIndex = (endIndex == -1) ? relation.length() : endIndex;
                    String relationPath = relation.substring(0, endIndex);
                    logger.debug(String.format("tableAliasMapping %s => %s", segments[i], relationPath));
                    builder.tableAliasMapping(segments[i], relationPath);
                }
            }
        }
    }

    protected List<SqlRow> findRowBySql(QueryParams params) {
        return findRowBySql(params, getFromStatement());
    }

    protected List<SqlRow> findRowBySql(QueryParams params, String fromStatement) {
        String sql = SQLUtils.buildNativeSql(getTableAlias(), clazz, params, fromStatement);
        logger.debug("findRowsBySql = " + sql);
        SqlQuery query = Ebean.createSqlQuery(sql);
        setQueryParameterValues(query::setParameter, params);
        if(params.isPagingQuery()) {
            return query.setFirstRow((params.getPageNo() - 1) * params.getPageSize())
                    .setMaxRows(params.getPageSize()).findList();
        }
        else {
            return query.findList();
        }
    }

    protected Integer findRowSizeBySql(QueryParams params) {
        return findRowSizeBySql(params, getFromStatement());
    }

    protected Integer findRowSizeBySql(QueryParams params, String fromStatement) {
        boolean isOnlySize = params.isOnlySize();
        params.setOnlySize(true);
        String sql = SQLUtils.buildNativeSql(getTableAlias(), clazz, params, fromStatement);
        logger.debug("findRowSizeBySql = " + sql);
        SqlQuery query = Ebean.createSqlQuery(sql);
        setQueryParameterValues(query::setParameter, params);
        Integer size = query.findOne().getInteger("count");
        params.setOnlySize(isOnlySize);
        return size;
    }

    protected List<T> findEntityBySql(QueryParams params) {
        return findEntityBySql(params, getFromStatement());
    }

    protected List<T> findEntityBySql(QueryParams params, String fromStatement) {
        List<SqlRow> results = findRowBySql(params, fromStatement);
        results.forEach(sqlRow -> sqlRow.entrySet().stream().filter(entry -> entry.getValue() == null)
                .map(Map.Entry::getKey).collect(Collectors.toSet())
                .forEach(sqlRow::remove));
        // Ebean SqlRow 會強制把 column name 改為小寫 !!! 所以我們要先轉為 underscore case 再轉為 camel case
        return SQLUtils.toEntity(clazz, results.stream().map(sqlRow -> sqlRow.entrySet().stream()
                .collect(Collectors.toMap(entry -> CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, entry.getKey()),
                        Map.Entry::getValue))).collect(Collectors.toList()));
    }

    protected <T> List<T> findDtoBySql(Class<T> dtoClass, QueryParams params) {
        return findDtoBySql(dtoClass, params, getFromStatement());
    }

    protected <T> List<T> findDtoBySql(Class<T> dtoClass, QueryParams params, String fromStatement) {
        String sql = SQLUtils.buildNativeSql(dtoClass == clazz ? getTableAlias() : null, dtoClass, params, fromStatement);
        logger.debug("findDtoBySql = " + sql);
        DtoQuery<T> query = Ebean.findDto(dtoClass, sql);
        setQueryParameterValues(query::setParameter, params);
        if(params.isPagingQuery()) {
            return query.setFirstRow((params.getPageNo() - 1) * params.getPageSize())
                    .setMaxRows(params.getPageSize()).findList();
        }
        else {
            return query.findList();
        }
    }

    private void setQueryParameterValues(BiConsumer<String, Object> querySetParameterConsumer, QueryParams params) {
        for (Predicate predicate : params.getPredicates()) {
            if (Operator.isNoValue(predicate.getOperator()))
                continue;
            querySetParameterConsumer.accept(predicate.getQueryParameterName(), predicate.getQueryParameterValue());
        }
        params.getParams().entrySet().forEach(entry -> querySetParameterConsumer.accept(entry.getKey(), entry.getValue()));
    }

    protected String getTableName() {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, clazz.getSimpleName());
    }

    protected String getTableAlias() {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, StringUtils.uncapitalize(getTableName()));
    }

    private String getFromStatement() {
        return "`" + getTableName() + "`" + " " +  "`" + getTableAlias() + "`";
    }
}