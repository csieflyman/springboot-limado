package com.csieflyman.limado.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import javax.persistence.MappedSuperclass;

/**
 * @author James Lin
 */
@MappedSuperclass
public abstract class BaseModel<ID> implements Identifiable<ID> {

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getId()).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        BaseModel model = (BaseModel) obj;
        return new EqualsBuilder().append(getId(), model.getId()).isEquals();
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
        //return this.getId().toString(); //方便 Debug
    }
}
