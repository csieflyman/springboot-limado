package com.csieflyman.limado.dto;

import javax.validation.GroupSequence;
import javax.validation.groups.Default;

/**
 * @author James Lin
 */
@GroupSequence({ Default.class, OrderedChecks.First.class, OrderedChecks.Second.class })
public interface OrderedChecks {

    interface First{}
    interface Second{}
}
