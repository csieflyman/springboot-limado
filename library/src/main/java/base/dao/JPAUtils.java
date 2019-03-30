package base.dao;

import base.exception.InternalServerErrorException;
import base.exception.InvalidEntityException;
import base.util.query.*;
import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author csieflyman
 *  此類別最好僅能有 JPA dependency，不能參考到任一 ORM Framework
 */
@Slf4j
public class JPAUtils {

    private JPAUtils() {

    }

    public static String buildNativeSql(String tableAlias, Class rootClass, Query query, String fromStatement) {
        StringBuilder sb = new StringBuilder();
        return sb.append(buildSelectClause(tableAlias, rootClass, query)).append(" from ").append(fromStatement)
                .append(" ").append(buildWhereClause(tableAlias, query.getJunction()))
                .append(" ").append(buildOrderByClause(tableAlias, query.getOrderByList())).toString();
    }

    public static String buildSelectClause(String tableAlias, Class rootClass, Query query) {
        if(query.isOnlySize()) {
            return "select count(*) as count";
        }

        Set<String> fetchProperties = query.getFetchProperties();
        Map<String, String> columnNameAliasMap = new LinkedHashMap<>();
        if(fetchProperties.isEmpty()) {
            columnNameAliasMap.putAll(buildColumnNameAliasMap(tableAlias, rootClass));
        }
        else {
            columnNameAliasMap.putAll(buildIdColumnNameAliasMap(tableAlias, rootClass));
            columnNameAliasMap.putAll(fetchProperties.stream().collect(Collectors.toMap(p -> getColumnName(tableAlias, p), p -> getColumnAlias(tableAlias, p))));

            Map<String, Class> relationPathClassMap = new LinkedHashMap<>();
            fetchProperties.stream().filter(p -> p.contains(".")).map(p -> buildRelationPathClassMap(rootClass, p.substring(0, p.lastIndexOf("."))))
                    .forEach(relationPathClassMap::putAll);
            columnNameAliasMap.putAll(relationPathClassMap.entrySet().stream()
                    .flatMap(entry -> buildIdColumnNameAliasMap(tableAlias + "." + entry.getKey(), entry.getValue()).entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }

        for(String relation: query.getFetchRelations()) {
            Map<String, Class> relationPathClassMap = buildRelationPathClassMap(rootClass, relation);
            int index = 0;
            for(Map.Entry<String, Class> entry: relationPathClassMap.entrySet()) {
                if(index == relationPathClassMap.size() - 1) {
                    columnNameAliasMap.putAll(buildColumnNameAliasMap(tableAlias + "." + entry.getKey(), entry.getValue()));
                }
                else {
                    columnNameAliasMap.putAll(buildIdColumnNameAliasMap(tableAlias + "." + entry.getKey(), entry.getValue()));
                }
                index++;
            }
        }
        return "select " + columnNameAliasMap.entrySet().stream().map(entry -> entry.getKey() + " as " + entry.getValue()).collect(Collectors.joining(", "));
    }

    public static String buildWhereClause(String tableAlias, Junction junction) {
        if (junction.getPredicates().isEmpty())
            return "";

        Query.populatePredicate(junction);

        StringBuilder sb = new StringBuilder();
        sb.append("where ");
        for (Predicate predicate : junction.getPredicates()) {
            sb.append(predicateToSql(tableAlias, predicate));
            if (junction.isConjunction())
                sb.append(" and ");
            else
                sb.append(" or ");
        }

        if (junction.isConjunction()) {
            sb.delete(sb.length() - 5, sb.length());
        } else {
            sb.delete(sb.length() - 4, sb.length());
        }
        return sb.toString();
    }

    public static String buildOrderByClause(String tableAlias, Set<OrderBy> orderByList) {
        if (orderByList.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();
        sb.append("order by ");
        for (OrderBy orderBy : orderByList) {
            // SQL Function
            if(orderBy.getProperty().contains("(")) {
                sb.append(orderBy.getProperty());
            }
            else {
                sb.append(getColumnName(tableAlias, orderBy.getProperty()));
            }
            sb.append(" ").append(orderBy.isAsc() ? "" : "DESC ").append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }

    public static Set<String> getPropertyNames(Class clazz) {
        return Stream.of(clazz.getFields()).filter(JPAUtils::isColumn).map(Field::getName).collect(Collectors.toSet());
    }

    private static String predicateToSql(String tableAlias, Predicate predicate) {
        if(predicate.isJunction()) {
            Junction junction = (Junction) predicate;
            return "(" + (junction.getPredicates().stream().map(p -> predicateToSql2(tableAlias, p))
                    .collect(Collectors.joining(junction.isConjunction() ? " and " : " or "))) + ")";
        }
        else {
            return predicateToSql2(tableAlias, predicate);
        }
    }

    private static String predicateToSql2(String tableAlias, Predicate predicate) {
        Preconditions.checkArgument(!predicate.isJunction());

        if(predicate.isLiteralSql()) {
            return predicate.getLiteralValue();
        }
        else {
            StringBuilder sb = new StringBuilder();
            Operator operator = predicate.getOperator();
            String operatorExpr = Operator.getExpr(operator);
            String columnName = getColumnName(tableAlias, predicate.getProperty());

            if (Operator.isNoValue(operator)) {
                sb.append(columnName).append(" ").append(operatorExpr);
            } else {
                sb.append(columnName).append(" ").append(operatorExpr).append(" ");
                if (operator == Operator.IN) {
                    sb.append("(:").append(predicate.getQueryParameterName()).append(")");
                } else {
                    sb.append(":").append(predicate.getQueryParameterName());
                }
            }
            return sb.toString();
        }
    }

    private static LinkedHashMap<String, Class> buildRelationPathClassMap(Class clazz, String path) {
        return buildRelationPathClassMap(clazz, path, 1);
    }

    private static LinkedHashMap<String, Class> buildRelationPathClassMap(Class clazz, String path, int depth) {
        try {
            String[] segments = path.split("\\.");
            Field field = clazz.getField(segments[depth - 1]);
            Class relationClass;
            if(Collection.class.isAssignableFrom(field.getType())) {
                relationClass = (Class) ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
            }
            else {
                relationClass = field.getType();
            }
            LinkedHashMap<String, Class> result = new LinkedHashMap<>();
            if(depth < segments.length) {
                result.put(path.substring(0, StringUtils.ordinalIndexOf(path, ".", depth)), relationClass);
                depth++;
                result = buildRelationPathClassMap(relationClass, path, depth);
            }
            else {
                result.put(path, relationClass);
            }
            return result;
        } catch (NoSuchFieldException e) {
            throw new InternalServerErrorException(String.format("Class %s property %s doesn't exist", clazz.getName(), path), e);
        }
    }

    private static Map<String, String> buildIdColumnNameAliasMap(String relation, Class clazz) {
        return buildColumnNameAliasMap(relation, clazz, JPAUtils::isIdColumn);
    }

    private static Map<String, String> buildColumnNameAliasMap(String relation, Class clazz) {
        return buildColumnNameAliasMap(relation, clazz, null);
    }

    private static Map<String, String> buildColumnNameAliasMap(String relation, Class clazz, java.util.function.Predicate<Field> predicate) {
        Set<String> properties = new HashSet<>();
        for(Field field: clazz.getFields()) {
            if(predicate != null && !predicate.test(field)) {
                continue;
            }
            if(isNotEmbeddedIdColumn(field)) {
                properties.add(field.getName());
            }
            else if(isEmbeddedIdColumn(field)) {
                for(Field idField: field.getType().getFields()) {
                    if(isNotEmbeddedIdColumn(idField)) {
                        properties.add(idField.getName());
                    }
                }
            }
        }
        return properties.stream().collect(Collectors.toMap(p -> getColumnName(relation, p), p -> getColumnAlias(relation, p)));
    }

    private static String getColumnName(String tableAlias, String property) {
        if(property.contains(".")) {
            return "`" + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,
                    property.substring(StringUtils.lastOrdinalIndexOf(property, ".", 2) + 1))
                    .replaceFirst("\\.", "`.");
        }
        else {
            return tableAlias == null ? CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, property) :
                    "`" + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,
                            tableAlias.contains(".") ? tableAlias.substring(tableAlias.lastIndexOf(".") + 1) : tableAlias) + "`"
                             + "." + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, property);
        }
    }

    private static String getColumnAlias(String tableAlias, String property) {
        return tableAlias == null ? "`" + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, property) + "`" :
                "`" + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, tableAlias) + "." +
                        CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, property) + "`";
    }

    private static boolean isColumn(Field field) {
        return field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(Basic.class)
                || field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class);
    }

    private static boolean isIdColumn(Field field) {
        return field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class);
    }

    private static boolean isEmbeddedIdColumn(Field field) {
        return field.isAnnotationPresent(EmbeddedId.class);
    }

    private static boolean isNotEmbeddedIdColumn(Field field) {
        return field.isAnnotationPresent(Basic.class) || field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(Id.class);
    }

    public static <T> List<T> toEntity(Class<T> clazz, List<Map<String, ? extends Object>> rows) {
        log.debug("rows = {}", rows);
        Set<T> entitySet = new LinkedHashSet<>();
        boolean hasRelation = false;
        for(Map<String, ? extends Object> row: rows) {
            boolean onlyEntity = row.keySet().stream().noneMatch(columnName -> columnName.split("\\.").length >= 2);
            if(onlyEntity) {
                T entity = newInstance(clazz);
                populateEntityProperties(entity, row);
                entitySet.add(entity);
            }
            else {
                if (entitySet.isEmpty()) {
                    hasRelation = row.size() > row.keySet().stream().filter(columnName -> columnName.split("\\.").length == 2).count();
                }
                // 注意: entity 一定要有 default constructor，否則須傳入 newInstance function
                T entity = newInstance(clazz);
                populateEntityProperties(entity, row.entrySet().stream().filter(entry -> entry.getKey().split("\\.").length == 2)
                        .collect(Collectors.toMap(entry -> entry.getKey().split("\\.")[1], Map.Entry::getValue)));
                entitySet.add(entity);
                if (hasRelation) {
                    Map<String, Object> relationMap = row.entrySet().stream().filter(entry -> entry.getKey().split("\\.").length >= 3)
                            .collect(Collectors.toMap(entry -> entry.getKey().substring(entry.getKey().indexOf(".") + 1), Map.Entry::getValue));
                    populateRelationEntities(entity, relationMap);
                }
            }
        }
        return new ArrayList<>(entitySet);
    }

    private static void populateEntityProperties(Object entity, Map<String, ? extends Object> valueMap) {
        try {
            BeanUtils.populate(entity, valueMap);
        } catch (Throwable e) {
            throw new InvalidEntityException(String.format("fail to populate entity %s", entity), e);
        }
    }

    //僅支援單向 binding
    private static void populateRelationEntities(Object entity, Map<String, Object> map){
        if(map.isEmpty())
            return;

        Map<String, Map<String, Object>> relationMap = map.entrySet().stream().filter(entry -> entry.getKey().split("\\.").length == 2)
                .collect(Collectors.groupingBy(entry -> entry.getKey().split("\\.")[0], Collectors.toMap(entry -> entry.getKey().split("\\.")[1], Map.Entry::getValue)));
        for(Map.Entry<String, Map<String, Object>> relationMapEntry: relationMap.entrySet()) {
            String relation = relationMapEntry.getKey();
            Map<String, Object> relationPropMap = relationMapEntry.getValue();
            try {
                Class relationClass = PropertyUtils.getPropertyType(entity, relation);
                //OneToOne, ManyToOne
                if (!Collection.class.isAssignableFrom(relationClass)) {
                    Object relationEntity = PropertyUtils.getProperty(entity, relation);
                    if (relationEntity == null) {
                        relationEntity = newInstance(relationClass);
                        populateEntityProperties(relationEntity, relationPropMap);
                        PropertyUtils.setProperty(entity, relation, relationEntity);
                    }
                    Map<String, Object> nestedRelationMap = map.entrySet().stream().filter(entry -> entry.getKey().split("\\.").length >= 3)
                            .collect(Collectors.toMap(entry -> entry.getKey().split("\\.")[2], Map.Entry::getValue));
                    populateRelationEntities(relationEntity, nestedRelationMap);
                } else {
                    Collection<Object> values = (Collection<Object>) PropertyUtils.getProperty(entity, relation);
                    if (values == null) {
                        if (List.class.isAssignableFrom(relationClass)) {
                            values = new ArrayList<>();
                        } else if (Set.class.isAssignableFrom(relationClass)) {
                            values = new HashSet<>();
                        } else {
                            throw new InvalidEntityException(String.format("Unsupported collection type %s", relationClass.getSimpleName()));
                        }
                    }
                    relationClass = (Class)((ParameterizedType)entity.getClass().getField(relation).getGenericType()).getActualTypeArguments()[0];
                    Object relationEntity = newInstance(relationClass);
                    populateEntityProperties(relationEntity, relationPropMap);
                    if (!values.contains(relationEntity)) {
                        values.add(relationEntity);
                        Map<String, Object> nestedRelationMap = map.entrySet().stream().filter(entry -> entry.getKey().split("\\.").length >= 3)
                                .collect(Collectors.toMap(entry -> entry.getKey().split("\\.")[2], Map.Entry::getValue));
                        populateRelationEntities(relationEntity, nestedRelationMap);
                    }
                }
            } catch (Throwable e) {
                throw new InvalidEntityException(String.format("fail to populate relation %s of entity %s", relation, entity), e);
            }
        }
    }

    private static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new InvalidEntityException(String.format("fail to initialize instance %s", clazz.getName()), e);
        }
    }
}
