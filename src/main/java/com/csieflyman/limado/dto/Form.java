package com.csieflyman.limado.dto;

import com.csieflyman.limado.exception.DataBindingException;
import com.csieflyman.limado.exception.InvalidEntityException;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.lang.reflect.ParameterizedType;

/**
 * @author csieflyman
 */
public abstract class Form<T> {

    protected Class<T> modelClass;

    public Form() {
        modelClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public T toModel() {
        T model;
        try {
            model = modelClass.newInstance();
        } catch (Exception e) {
            throw new InvalidEntityException(String.format("model class %s should have default constructor", modelClass.getName()), e);
        }
        try {
            BeanUtils.copyProperties(model, this);
        } catch (Exception e) {
            throw new DataBindingException(String.format("fail to bind form %s to model %s", this.getClass().getName(), modelClass.getName()), e);
        }
        return model;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
