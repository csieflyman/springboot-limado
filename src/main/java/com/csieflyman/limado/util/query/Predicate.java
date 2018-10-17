package com.csieflyman.limado.util.query;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Set;

/**
 * author James Lin
 */
public class Predicate {

    private String property;

    private Operator operator;

    private Object value;

    private String queryParameterName;

    private Object queryParameterValue;

    Predicate(String property, Operator operator, Object value) {
        Preconditions.checkArgument(property != null, "property is null");
        Preconditions.checkArgument(operator != null, "operator is null");
        if (!Operator.isNoValue(operator)) {
            Preconditions.checkArgument(value != null, "value is null");
        }
        if (operator == Operator.IN && Set.class.isAssignableFrom(value.getClass())) {
            Preconditions.checkArgument(!((Set) value).isEmpty(), property + " with in operator can't has empty collection value");
        }

        this.property = property;
        this.operator = operator;
        this.value = value;
    }

    public String getProperty() {
        return property;
    }

    public Operator getOperator() {
        return operator;
    }

    public Object getValue() {
        return value;
    }

    void setValue(Object value) {
        this.value = value;
    }

    public String getQueryParameterName() {
        return queryParameterName;
    }

    public void setQueryParameterName(String queryParameterName) {
        Preconditions.checkNotNull(queryParameterName, "null value of predicate queryParameterName " + this);
        this.queryParameterName = queryParameterName;
    }

    public Object getQueryParameterValue() {
        return queryParameterValue;
    }

    public void setQueryParameterValue(Object queryParameterValue) {
        Preconditions.checkNotNull(queryParameterValue, "null value of predicate queryParameterValue " + this);
        this.queryParameterValue = queryParameterValue;
    }

    public boolean isNestedProperty() {
        return property.contains(".");
    }

    public String getNestedProperty() {
        return property.split("\\.")[1];
    }

    public String getTopProperty() {
        return property.split("\\.")[0];
    }

    public static Predicate eq(String property, Object value) {
        return new Predicate(property, Operator.EQ, value);
    }

    public static Predicate ne(String property, Object value) {
        return new Predicate(property, Operator.NE, value);
    }

    public static Predicate lt(String property, Comparable value) {
        return new Predicate(property, Operator.LT, value);
    }

    public static Predicate le(String property, Comparable value) {
        return new Predicate(property, Operator.LE, value);
    }

    public static Predicate gt(String property, Comparable value) {
        return new Predicate(property, Operator.GT, value);
    }

    public static Predicate ge(String property, Comparable value) {
        return new Predicate(property, Operator.GE, value);
    }

    public static Predicate in(String property, Set<?> values) {
        return new Predicate(property, Operator.IN, values);
    }

    public static Predicate like(String property, String value) {
        return new Predicate(property, Operator.LIKE, value);
    }

    public static Predicate isNull(String property) {
        return new Predicate(property, Operator.IS_NULL, null);
    }

    public static Predicate isNotNull(String property) {
        return new Predicate(property, Operator.IS_NOT_NULL, null);
    }

    public static Predicate isEmpty(String property) {
        return new Predicate(property, Operator.IS_EMPTY, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Predicate predicate = (Predicate) o;
        return new EqualsBuilder().append(this.getProperty(), predicate.getProperty())
                .append(this.getOperator(), predicate.getOperator()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.getProperty()).append(this.getOperator()).toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
