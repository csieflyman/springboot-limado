package base.util.query;

import java.util.Collection;

/**
 * @author csieflyman
 */
public class Disjunction extends Junction{

    Disjunction() { }

    Disjunction(Collection<Predicate> predicates) {
        super(false, predicates);
    }
}
