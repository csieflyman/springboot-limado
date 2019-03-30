package base.dto;

import javax.validation.GroupSequence;
import javax.validation.groups.Default;

/**
 * @author csieflyman
 */
@GroupSequence({ Default.class, OrderedChecks.First.class, OrderedChecks.Second.class, OrderedChecks.LAST.class})
public interface OrderedChecks {

    interface First{}
    interface Second{}
    interface LAST{}
}
