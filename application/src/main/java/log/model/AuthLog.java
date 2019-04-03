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
 * @author csieflyman
 */
@Setter
@Getter
@Entity
public class AuthLog extends BaseModel<Long> {

    private static final long serialVersionUID = -155256950440207989L;

    public AuthLog(String account) {
        this.account = account.length() > 30 ? account.substring(0, 30) : account;
    }

    @Id
    private Long id;

    @Column
    private String account;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeUtils.DATE_TIME_PATTERN, timezone = DateTimeUtils.LOCAL_ZONE_ID_STRING)
    @Column(updatable = false, insertable = false)
    private Date occurAt;

    @Column
    private boolean success = true;

    @Column
    private String errorMsg;

    @Column
    private String ip;
}
