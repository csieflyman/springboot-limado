package com.csieflyman.limado.util;

import com.csieflyman.limado.exception.InternalServerErrorException;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author James Lin
 */
public class MyBeanUtils {

    private MyBeanUtils() {

    }

    public static void copyPropertiesIgnoreNullValue(Object source, Object target) {
        copyPropertiesIgnoreNullValue(source, target, new HashSet<>());
    }

    public static void copyPropertiesIgnoreNullValue(Object source, Object target, Set<String> ignoreFields) {
        Set<String> fieldNames = new HashSet<>();
        String[] ignoreFieldNames = Stream.of(source.getClass().getFields()).filter(field -> {
            try {
                if(fieldNames.contains(field.getName())) // 略過 superclass 重複名稱的欄位
                    return false;
                boolean ignore = ignoreFields.contains(field.getName()) || (Modifier.isPublic(field.getModifiers()) && field.get(source) == null);
                fieldNames.add(field.getName());
                return ignore;
            } catch (IllegalAccessException e) {
                throw new InternalServerErrorException(String.format("fail to copy field %s",
                        source.getClass().getName() + "[" + field.getName() + "]"), e);
            }
        }).map(Field::getName).toArray(String[]::new);
        BeanUtils.copyProperties(source, target, ignoreFieldNames);
    }
}
