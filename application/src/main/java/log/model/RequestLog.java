package log.model;

import base.model.BaseModel;
import base.util.DateTimeUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * @author James Lin
 */
@Getter
@Setter
@Entity
public class RequestLog extends BaseModel<Long> {

    @Id
    private Long id;

    @Column
    private String identity;

    @Column
    private String api;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeUtils.DATE_TIME_PATTERN, timezone = DateTimeUtils.LOCAL_ZONE_ID_STRING)
    @Column
    private Date reqTime = new Date();

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeUtils.DATE_TIME_PATTERN, timezone = DateTimeUtils.LOCAL_ZONE_ID_STRING)
    @Column
    private Date rspTime;

    @Column
    private String rspStatus;

    @Column
    private String reqBody;

    @Column
    private String rspBody;

    @Column
    private String ip;

    @Override
    public Long getId() {
        return id;
    }
}
