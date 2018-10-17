package com.csieflyman.limado.util.query;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * @author csieflyman
 */
public class Predicates extends LinkedHashSet<Predicate>{

    private boolean isDisjunction = false;

    private Predicates() {

    }

    public static Predicates and() {
        return new Predicates();
    }

    public static Predicates and(Predicate... ps){
        return and(Arrays.asList(ps));
    }

    public static Predicates and(Collection<Predicate> ps) {
        Predicates predicates = and();
        predicates.addAll(ps);
        return predicates;
    }

    public static Predicates or() {
        Predicates predicates = new Predicates();
        predicates.isDisjunction = true;
        return predicates;
    }

    public static Predicates or(Predicate... ps){
        return or(Arrays.asList(ps));
    }

    public static Predicates or(Collection<Predicate> ps) {
        Predicates predicates = or();
        predicates.addAll(ps);
        return predicates;
    }

    public boolean isDisjunction() {
        return isDisjunction;
    }

    public Predicates setDisjunction(boolean disjunction) {
        isDisjunction = disjunction;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("isDisjunction", isDisjunction)
                .append("predicates", super.toString()).toString();
    }
}
