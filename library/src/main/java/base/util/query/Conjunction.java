package base.util.query;

import java.util.Collection;

/**
 * @author csieflyman
 */
public class Conjunction extends Junction{

    Conjunction() { }

    Conjunction(Collection<Predicate> predicates) {
        super(true, predicates);
    }
}
