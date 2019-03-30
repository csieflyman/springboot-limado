package auth.model;

import base.model.BaseModel;
import base.model.Identifiable;
import base.util.DateTimeUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

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

    public AuthLog(String brandName, String account) {
        this.brandName = brandName.length() > 64 ? brandName.substring(0, 64) : brandName;
        this.account = account.length() > 255 ? account.substring(0, 255) : account;
    }

    @Id
    private Long id;

    @Column
    private String account;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeUtils.DATE_TIME_PATTERN)
    @Column(updatable = false, insertable = false)
    private Date occurAt;

    @Column
    private boolean success = true;

    @Column
    private String errorMsg;

    @Column
    private String source;

    @Column
    private String ip;
}
