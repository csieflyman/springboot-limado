package log.model;

import base.model.BaseModel;
import base.util.DateTimeUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringExclude;
import util.Subject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Date;

/**
 * @author csieflyman
 */
@Getter
@Setter
@Entity
public class ErrorLog extends BaseModel<Long> {

    public ErrorLog(Subject subject) {
        this(subject, null);
    }

    public ErrorLog(Subject subject, Throwable ex) {
        if(subject != null) {
            if (subject.isAdmin()) {
            } else {
                identity = subject.getIdentity();
            }
        }
        if(ex != null) {
            this.ex = ex;
            errorMsg = ex.getMessage();
        }
    }

    @Id
    private Long id;

    @Column
    private String identity;

    @Column
    private String api;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeUtils.DATE_TIME_PATTERN, timezone = DateTimeUtils.LOCAL_ZONE_ID_STRING)
    @Column
    private Date occurAt = new Date();

    @Column
    private String errorMsg;

    @Column
    private String body;

    @JsonIgnore
    @ToStringExclude
    @Transient
    private Throwable ex;

    @Override
    public Long getId() {
        return id;
    }
}
