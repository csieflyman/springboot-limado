package base.dto.request;

/**
 * @author csieflyman
 */
public class FormRequest<T> extends Request {

    protected T form;

    public T getForm() {
        return form;
    }

    public void setForm(T form) {
        this.form = form;
    }
}
