package com.csieflyman.limado.exception;

import com.csieflyman.limado.model.Identifiable;

import javax.validation.ConstraintViolation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author James Lin
 */
public class BadBatchRequestException extends BadRequestException {

    public String type;

    private Map<Identifiable, Set<ConstraintViolation<Identifiable>>> invalidObjectsMap = new HashMap<>();

    public BadBatchRequestException(String type, Map<Identifiable, Set<ConstraintViolation<Identifiable>>> invalidObjectsMap) {
        super(invalidObjectsMap.keySet().stream().map(entity -> getErrorMsg(type, entity, invalidObjectsMap.get(entity)))
                .collect(Collectors.joining(",")), null, invalidObjectsMap.keySet());
        this.invalidObjectsMap = invalidObjectsMap;
        this.type = type;
    }

    public Set<Identifiable> getInvalidObjects() {
        return invalidObjectsMap.keySet();
    }

    public String getErrorMsg(Identifiable form) {
        return getErrorMsg(type, form, invalidObjectsMap.get(form));
    }

    private static <T extends Identifiable> String getErrorMsg(String type, T form, Set<ConstraintViolation<T>> violations) {
        return "[" + (type == null ? "" : (type + "/")) + form.getId() + "] => " + violations
                .stream().map(violation -> violation.getPropertyPath() + ":" + violation.getMessage()).collect(Collectors.joining(","));
    }
}
