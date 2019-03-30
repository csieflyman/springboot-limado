package base.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

/**
 * @author csieflyman
 */
public class Request {

    @JsonIgnore
    private Date requestTime = new Date();

    public Date getRequestTime() {
        return requestTime;
    }

    @JsonIgnore
    private String body;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
