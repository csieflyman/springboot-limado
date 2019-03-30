package base.util.query;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author csieflyman
 */
public class Junction extends Predicate{

    private Set<Predicate> predicates = new LinkedHashSet<>();

    private boolean isConjunction = true;

    Junction() { }

    Junction(boolean isConjunction, Collection<Predicate> predicates) {
        this.isConjunction = isConjunction;
        this.predicates.addAll(predicates);
    }

    public void add(Predicate... predicates) {
        add(Arrays.stream(predicates).collect(Collectors.toList()));
    }

    public void add(Collection<Predicate> predicates) {
        this.predicates.addAll(predicates);
    }

    public Set<Predicate> getPredicates() {
        return predicates;
    }

    public boolean isConjunction() {
        return isConjunction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Junction other = (Junction) o;
        return new EqualsBuilder().append(isConjunction, other.isConjunction).append(predicates, other.predicates).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(isConjunction).append(predicates).toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
