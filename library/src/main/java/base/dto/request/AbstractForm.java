package base.dto.request;

import base.exception.InvalidEntityException;
import base.util.BeanUtils;

import java.lang.reflect.ParameterizedType;

/**
 * @author csieflyman
 */
public abstract class AbstractForm<T> {

    public T toModel() {
        Class<T> clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        T instance;
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new InvalidEntityException(String.format("fail to initialize instance %s", clazz.getName()), e);
        }
        BeanUtils.copyIgnoreNull(this, instance);
        return instance;
    }
}
