package base.util.query;

import base.exception.InvalidQueryException;
import base.util.DateTimeUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.MultiValueMap;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author csieflyman
 */
public class Query {

    private int pageNo = -1;
    private int pageSize = 10;
    private boolean onlySize = false;
    private Set<OrderBy> orderByList = new LinkedHashSet<>();
    private Junction junction = new Conjunction();
    private Set<String> fetchRelations = new LinkedHashSet<>();
    private Set<String> fetchProperties = new LinkedHashSet<>();
    private Map<String, Object> paramMap = new HashMap<>(); //用來傳入自訂參數值

    public static final String Q_PAGE_NO = "pageNo";
    public static final String Q_PAGE_SIZE = "pageSize";
    public static final String Q_SORT = "sort";
    public static final String Q_ONLY_SIZE = "onlySize";
    public static final String Q_PREDICATES = "predicates";
    public static final String Q_PREDICATES_DISJUNCTION = "predicatesDisjunction";
    public static final String Q_FETCH_RELATIONS = "relations";
    public static final String Q_FETCH_FIELDS = "fields";

    private DateTimeFormatter dateTimeFormatter = DateTimeUtils.UTC_DATE_TIME_FORMATTER;

    private Query() {

    }

    public static Query create() {
        return new Query();
    }

    public static Query create(MultiValueMap<String, String> paramMap) {
        return create(paramMap, null);
    }

    public static Query create(MultiValueMap<String, String> paramMap, DateTimeFormatter dateTimeFormatter) {
        return create(paramMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toArray(new String[0]))), dateTimeFormatter);
    }

    public static Query create(Map<String, String[]> paramMap) {
        return create(paramMap, null);
        //return create(paramMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new String[]{e.getValue()})), null);
    }

    public static Query create(Map<String, String[]> paramMap, DateTimeFormatter dateTimeFormatter) {
        Preconditions.checkArgument(paramMap != null, "paramMap must not be null");

        Query params = new Query();
        if(dateTimeFormatter != null) {
            params.dateTimeFormatter = dateTimeFormatter;
        }
        paramMap.forEach((key, value) -> params.put(key, value[0]));
        return params;
    }

    private Query put(String param, String value) {
        Preconditions.checkArgument(param != null, "param must not be null");
        Preconditions.checkArgument(value != null, "value must not be null");

        switch (param) {
            case Q_PAGE_NO:
                pageNo(Integer.parseInt(value));
                break;
            case Q_PAGE_SIZE:
                pageSize(Integer.parseInt(value));
                break;
            case Q_SORT:
                parseSortString(value);
                break;
            case Q_ONLY_SIZE:
                onlySize = Boolean.parseBoolean(value);
                break;
            case Q_PREDICATES:
                parsePredicateString(value);
                break;
            case Q_PREDICATES_DISJUNCTION:
                junction = new Disjunction(junction.getPredicates());
                break;
            case Q_FETCH_RELATIONS:
                fetchRelations(Sets.newHashSet(Splitter.on(",").split(value))
                        .stream().map(String::trim).collect(Collectors.toSet()));
                break;
            case Q_FETCH_FIELDS:
                fetchProperties(Sets.newHashSet(Splitter.on(",").split(value))
                        .stream().map(String::trim).collect(Collectors.toSet()));
                break;
            default:
                paramMap.put(param, value);
                //throw new IllegalArgumentException("unknown query parameter: " + param);
        }
        return this;
    }

    private void parseSortString(String s) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(s), "sort string can't be empty");

        String[] orderByProps = s.split(",");
        for (String orderByProp : orderByProps) {
            orderByProp = orderByProp.trim();
            if (orderByProp.startsWith("+")) {
                orderByList.add(OrderBy.asc(orderByProp.substring(1)));
            } else if (orderByProp.startsWith("-")) {
                orderByList.add(OrderBy.desc(orderByProp.substring(1)));
            } else {
                orderByList.add(OrderBy.asc(orderByProp));
            }
        }
    }

    private void parsePredicateString(String s) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(s), "predicate string can't be empty");
        Preconditions.checkArgument(s.startsWith("[") && s.endsWith("]"), "invalid q_predicate format: " + s);

        s = s.substring(1, s.length() - 1).trim();
        try {
            s = URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(String.format("fail to decode predicateValue %s to UTF-8", s), e);
        }
        List<String> predicateStrings = Lists.newArrayList(Splitter.on(";").split(s));
        try {
            junction.add(predicateStrings.stream().map(String::trim).filter(StringUtils::isNotEmpty).map(ps -> Lists.newArrayList(Splitter.on(" ").split(ps)))
                    .map(psList -> new Predicate(psList.get(0), Operator.exprValueOf(psList.get(1)),
                            psList.size() == 2 ? null : tryToConvertToDate(String.join(" ", psList.subList(2, psList.size())))))
                    .collect(Collectors.toCollection(LinkedHashSet::new)));

            junction.getPredicates().stream().filter(ps -> ps.getOperator() == Operator.IN).forEach(predicate -> {
                String valueString = ((String) predicate.getValue()).trim();
                if (!(valueString.startsWith("(") && valueString.endsWith(")"))) {
                    throw new IllegalArgumentException(String.format("%s value string %s should be with the format: (e1, e2)", predicate.getProperty(), valueString));
                }
                valueString = valueString.substring(1, valueString.length() - 1);
                predicate.setValue(Sets.newHashSet(Splitter.on(",").split(valueString)));
            });
        } catch (Throwable e) {
            throw new IllegalArgumentException("invalid q_predicate format: " + s, e);
        }
    }

    private Object tryToConvertToDate(String s) {
        try {
            return Date.from(ZonedDateTime.parse(s, dateTimeFormatter).toInstant());
        } catch (DateTimeParseException e) {
            return s;
        }
    }

    public boolean isPagingQuery() {
        return pageNo > 0;
    }

    public int getPageNo() {
        return pageNo;
    }

    public Query pageNo(int pageNo) {
        Preconditions.checkArgument(pageNo >= 1);
        this.pageNo = pageNo;
        return this;
    }

    public int getPageSize() {
        return pageSize;
    }

    public Query pageSize(int pageSize) {
        Preconditions.checkArgument(pageSize > 0);
        this.pageSize = pageSize;
        return this;
    }

    public boolean isOnlySize() {
        return onlySize;
    }

    public Query setOnlySize(boolean onlySize) {
        this.onlySize = onlySize;
        return this;
    }

    public Set<OrderBy> getOrderByList() {
        return orderByList;
    }

    public boolean isOrderByEmpty() {
        return orderByList.isEmpty();
    }

    public Query orderByAsc(String property) {
        orderByList.add(OrderBy.asc(property));
        return this;
    }

    public Query orderByDesc(String property) {
        orderByList.add(OrderBy.desc(property));
        return this;
    }

    public Query orderBy(String property, boolean isAsc) {
        return isAsc ? orderByAsc(property) : orderByDesc(property);
    }

    public Junction getJunction() {return junction;}

    public boolean isConjunction() {
        return junction.isConjunction();
    }

    public Query conjunction(Conjunction conjunction) {
        junction.add(conjunction);
        return this;
    }

    public Query disjunction(Disjunction disjunction) {
        junction.add(disjunction);
        return this;
    }

    public Query conjunction(Predicate... predicates) {
        junction.add(new Conjunction(Arrays.stream(predicates).collect(Collectors.toSet())));
        return this;
    }

    public Query disjunction(Predicate... predicates) {
        junction.add(new Disjunction(Arrays.stream(predicates).collect(Collectors.toSet())));
        return this;
    }

    public Set<Predicate> getPredicates() {
        return junction.getPredicates();
    }

    public Query literal(String literalSql) {
        junction.add(Predicate.literal(literalSql));
        return this;
    }

    public Query eq(String property, Object value) {
        junction.add(Predicate.eq(property, value));
        return this;
    }

    public Query ne(String property, Object value) {
        junction.add(Predicate.ne(property, value));
        return this;
    }

    public Query lt(String property, Comparable value) {
        junction.add(Predicate.lt(property, value));
        return this;
    }

    public Query le(String property, Comparable value) {
        junction.add(Predicate.le(property, value));
        return this;
    }

    public Query ge(String property, Comparable value) {
        junction.add(Predicate.ge(property, value));
        return this;
    }

    public Query gt(String property, Comparable value) {
        junction.add(Predicate.gt(property, value));
        return this;
    }

    public <T> Query between(String property, Comparable<T> geValue, Comparable<T> ltValue) {
        junction.add(Predicate.ge(property, geValue));
        junction.add(Predicate.lt(property, ltValue));
        return this;
    }

    public Query in(String property, Set<?> values) {
        junction.add(Predicate.in(property, values));
        return this;
    }

    public Query like(String property, String value) {
        junction.add(Predicate.like(property, value));
        return this;
    }

    public Query isNull(String property) {
        junction.add(Predicate.isNull(property));
        return this;
    }

    public Query isNotNull(String property) {
        junction.add(Predicate.isNotNull(property));
        return this;
    }

    public Query isEmpty(String property) {
        junction.add(Predicate.isEmpty(property));
        return this;
    }

    public Query addParam(String property, Object value) {
        paramMap.put(property, value);
        return this;
    }

    public Object getParam(String property) {
        return paramMap.get(property);
    }

    public Map<String, Object> getParams() {
        return paramMap;
    }

    public Set<String> getFetchRelations() {
        return fetchRelations;
    }

    public Query fetchRelations(String... relations) {
        Preconditions.checkNotNull(relations);
        return fetchRelations(Sets.newHashSet(relations));
    }

    public Query fetchRelations(Set<String> relations) {
        Preconditions.checkNotNull(relations);
        this.fetchRelations.addAll(relations);
        return this;
    }

    public Set<String> getFetchProperties() {
        return fetchProperties;
    }

    public Query fetchProperties(String... properties) {
        Preconditions.checkNotNull(properties);
        return fetchProperties(Sets.newHashSet(properties));
    }

    public Query fetchProperties(Set<String> properties) {
        Preconditions.checkNotNull(properties);
        this.fetchProperties = properties;
        return this;
    }

    public String toQueryString() {
        Set<Predicate> predicates = getPredicates();
        StringBuilder sb = new StringBuilder();
        if(!predicates.isEmpty()) {
            StringBuilder sb2 = new StringBuilder();
            for(Predicate p: predicates) {
                if(p.isJunction()) {
                    throw new InvalidQueryException("junction is unsupported: " + p);
                }
                sb2.append(p.getProperty()).append(" ").append(Operator.getExprOfQueryString(p.getOperator())).append(" ");
                if(Operator.isNoValue(p.getOperator())) {
                }
                else if(p.getOperator() == Operator.IN) {
                    sb2.append("(").append(((Set<String>)p.getValue()).stream().map(this::convertValueToString).collect(Collectors.joining(","))).append(")");
                }
                else {
                    sb2.append(convertValueToString(p.getValue()));
                }
                sb2.append(" ; ");
            }
            sb2 = sb2.delete(sb2.lastIndexOf(";") - 1, sb2.length());
            String predicateValue = sb2.toString();
            try {
                predicateValue = URLEncoder.encode(predicateValue, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new UnsupportedOperationException(String.format("fail to encode predicateValue %s to UTF-8", predicateValue), e);
            }
            sb.append("&predicates=[").append(predicateValue).append("]");
        }
        if(pageNo != -1) {
            sb.append("&pageNo=").append(pageNo).append("&pageSize=").append(pageSize);
        }
        if(!orderByList.isEmpty()) {
            sb.append("&sort=");
            for(OrderBy orderBy: orderByList) {
                sb.append(orderBy.isAsc() ? "+" : "-").append(orderBy.getProperty()).append(",");
            }
            sb = sb.delete(sb.length() - 1, sb.length());
        }
        if(onlySize) {
            sb.append("&onlySize=true");
        }
        if(!isConjunction()) {
            sb.append("&predicatesDisjunction=true");
        }
        if(!fetchRelations.isEmpty()) {
            sb.append("&relations=").append(fetchRelations.stream().collect(Collectors.joining(",")));
        }
        if(!fetchProperties.isEmpty()) {
            sb.append("&fields=").append(fetchProperties.stream().collect(Collectors.joining(",")));
        }
        return sb.length() > 0 ? sb.substring(1) : null;
    }

    private String convertValueToString(Object o) {
        if (Date.class.isAssignableFrom(o.getClass())) {
            return dateTimeFormatter.format(((Date) o).toInstant());
        } else if (o.getClass() == ZonedDateTime.class) {
            return dateTimeFormatter.format((ZonedDateTime) o);
        } else {
            return o.toString();
        }
    }

    public void populatePredicate() {
        populateQueryParameterName(junction);
        populateQueryParameterValue(junction);
    }

    public static void populatePredicate(Junction junction) {
        populateQueryParameterName(junction);
        populateQueryParameterValue(junction);
    }

    private static void populateQueryParameterName(Junction junction) {
        Map<String, Integer> propertyCountMap = new HashMap<>();
        for (Predicate predicate : junction.getPredicates()) {
            if(predicate.isJunction()) {
                ((Junction)predicate).getPredicates().forEach(p -> populateQueryParameterName(propertyCountMap, p));
            }
            else {
                populateQueryParameterName(propertyCountMap, predicate);
            }
        }
    }

    private static void populateQueryParameterName(Map<String, Integer> propertyCountMap, Predicate predicate) {
        Preconditions.checkArgument(!predicate.isJunction());
        String property = predicate.getProperty();
        if (propertyCountMap.containsKey(property)) {
            propertyCountMap.put(property, (propertyCountMap.get(property) + 1));
            predicate.setQueryParameterName(property + propertyCountMap.get(property));
        } else {
            propertyCountMap.put(property, 1);
            predicate.setQueryParameterName(property);
        }
        if (predicate.isNestedProperty()) {
            predicate.setQueryParameterName(predicate.getQueryParameterName().replace(".", "_"));
        }
    }

    //假設不必型別轉換 value
    private static void populateQueryParameterValue(Junction junction) {
        for (Predicate predicate : junction.getPredicates()) {
            if(predicate.isJunction()) {
                ((Junction)predicate).getPredicates().forEach(Query::populateQueryParameterValue);
            }
            else {
                populateQueryParameterValue(predicate);
            }
        }
    }

    //假設不必型別轉換 value
    private static void populateQueryParameterValue(Predicate predicate) {
        Preconditions.checkArgument(!predicate.isJunction());
        if (Operator.isNoValue(predicate.getOperator())) {
            return;
        }
        predicate.setQueryParameterValue(predicate.getValue());
    }

    @Override
    public String toString() {
        return toQueryString();
    }
}
