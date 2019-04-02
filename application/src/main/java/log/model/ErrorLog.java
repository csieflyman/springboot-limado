package log.model;

import base.model.BaseModel;
import base.util.DateTimeUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringExclude;
import util.Subject;

import javax.persistence.*;
import java.util.Date;

/**
 * @author csieflyman
 */
@Getter
@Setter
@Entity
@Table(name = "error_log")
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
    private String source;

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

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getId()).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ErrorLog other = (ErrorLog) obj;
        return new EqualsBuilder().append(getId(), other.getId()).isEquals();
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
