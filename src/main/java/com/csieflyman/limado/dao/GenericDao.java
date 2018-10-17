package com.csieflyman.limado.dao;

import com.csieflyman.limado.model.Identifiable;
import com.csieflyman.limado.util.query.Predicates;
import com.csieflyman.limado.util.query.QueryParams;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * author James Lin
 */
public interface GenericDao<T extends Identifiable<ID>, ID> {

    T create(T entity);

    void update(T entity);

    void delete(T entity);

    int executeUpdate(Map<String, Object> valueMap, Predicates predicates);

    int executeDelete(Predicates predicates);

    Optional<T> getById(ID id);

    Optional<T> findOne(QueryParams params);

    List<T> find(QueryParams params);

    int findSize(QueryParams params);
}
