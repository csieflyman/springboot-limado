package com.csieflyman.limado.util.query;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * author James Lin
 */
public class OrderBy {

    private boolean asc;

    private String property;

    private OrderBy(String property, boolean asc) {
        this.property = property;
        this.asc = asc;
    }

    static OrderBy asc(String property) {
        return new OrderBy(property, true);
    }

    static OrderBy desc(String property) {
        return new OrderBy(property, false);
    }

    public boolean isAsc() {
        return asc;
    }

    public String getProperty() {
        return property;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.getProperty()).append(this.isAsc()).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderBy orderBy = (OrderBy) o;
        return new EqualsBuilder().append(this.getProperty(), orderBy.getProperty())
                .append(this.isAsc(), orderBy.isAsc()).isEquals();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
