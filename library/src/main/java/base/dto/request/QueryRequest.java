package base.dto.request;

/**
 * @author csieflyman
 */
public class QueryRequest<T> extends Request {

    protected T queryObject;

    public T getQueryObject() {
        return queryObject;
    }

    public void setQueryObject(T queryObject) {
        this.queryObject = queryObject;
    }
}
