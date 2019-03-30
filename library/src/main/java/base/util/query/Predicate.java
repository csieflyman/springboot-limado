package base.util.query;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Set;

/**
 * @author csieflyman
 */
public class Predicate {

    private String property;

    private Operator operator;

    private Object value;

    private String queryParameterName;

    private Object queryParameterValue;

    private String literalSql;

    private enum Type {
        JUNCTION, LITERAL, NON_LITERAL
    }

    private Type type;

    Predicate() {
        this.type = Type.JUNCTION;
    }

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
        this.type = Type.NON_LITERAL;
    }

    private Predicate(String literalSql) {
        this.type = Type.LITERAL;
        this.literalSql = literalSql;
    }

    public String getLiteralValue() {
        Preconditions.checkArgument(type == Type.LITERAL);
        return literalSql;
    }

    public boolean isLiteralSql() {
        return type == Type.LITERAL;
    }

    public boolean isJunction() {
        return type == Type.JUNCTION;
    }

    public String getProperty() {
        Preconditions.checkArgument(type == Type.NON_LITERAL);
        return property;
    }

    public Operator getOperator() {
        Preconditions.checkArgument(type == Type.NON_LITERAL);
        return operator;
    }

    public Object getValue() {
        Preconditions.checkArgument(type == Type.NON_LITERAL);
        return value;
    }

    void setValue(Object value) {
        Preconditions.checkArgument(type == Type.NON_LITERAL);
        this.value = value;
    }

    public String getQueryParameterName() {
        Preconditions.checkArgument(type == Type.NON_LITERAL);
        return queryParameterName;
    }

    void setQueryParameterName(String queryParameterName) {
        Preconditions.checkArgument(type == Type.NON_LITERAL);
        Preconditions.checkNotNull(queryParameterName, "null value of predicate queryParameterName " + this);
        this.queryParameterName = queryParameterName;
    }

    public Object getQueryParameterValue() {
        Preconditions.checkArgument(type == Type.NON_LITERAL);
        return queryParameterValue;
    }

    void setQueryParameterValue(Object queryParameterValue) {
        Preconditions.checkArgument(type == Type.NON_LITERAL);
        Preconditions.checkNotNull(queryParameterValue, "null value of predicate queryParameterValue " + this);
        this.queryParameterValue = queryParameterValue;
    }

    boolean isNestedProperty() {
        Preconditions.checkArgument(type == Type.NON_LITERAL);
        return property.contains(".");
    }

    public static Predicate literal(String literalSql) {
        return new Predicate(literalSql);
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

        Predicate other = (Predicate) o;
        if(type != other.type) return false;
        if(type == Type.LITERAL) {
            return new EqualsBuilder().append(this.literalSql, other.literalSql).isEquals();
        }
        else {
            return new EqualsBuilder().append(this.getProperty(), other.getProperty())
                    .append(this.getOperator(), other.getOperator()).isEquals();
        }
    }

    @Override
    public int hashCode() {
        if(type == Type.LITERAL) {
            return new HashCodeBuilder().append(this.literalSql).toHashCode();
        }
        else {
            return new HashCodeBuilder().append(this.getProperty()).append(this.getOperator()).toHashCode();
        }
    }

    @Override
    public String toString() {
        if(type == Type.LITERAL) {
            return literalSql;
        }
        else {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}
