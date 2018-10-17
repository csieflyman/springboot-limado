package com.csieflyman.limado.model;

import com.csieflyman.limado.util.DateTimeUtils;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.Date;

/**
 * @author James Lin
 */
@MappedSuperclass
public abstract class EntityModel<ID> extends BaseModel<ID> {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeUtils.DATE_TIME_PATTERN, timezone = DateTimeUtils.TIMEZONE_ID)
    @Column(updatable = false, insertable = false, columnDefinition = "timestamp NULL DEFAULT CURRENT_TIMESTAMP")
    public Date createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeUtils.DATE_TIME_PATTERN, timezone = DateTimeUtils.TIMEZONE_ID)
    @Column(updatable = false, insertable = false, columnDefinition = "timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    public Date updatedAt;
}
