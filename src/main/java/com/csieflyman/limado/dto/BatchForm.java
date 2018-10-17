package com.csieflyman.limado.dto;

import com.csieflyman.limado.model.Identifiable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

/**
 * @author James Lin
 */
public abstract class BatchForm<T> extends Form<List<T>> implements Identifiable<T> {

    public abstract T getId();

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getId()).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        BatchForm other = (BatchForm) obj;
        return new EqualsBuilder().append(getId(), other.getId()).isEquals();
    }
}
