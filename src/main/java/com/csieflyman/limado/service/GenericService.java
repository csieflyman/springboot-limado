package com.csieflyman.limado.service;

import com.csieflyman.limado.model.BaseModel;
import com.csieflyman.limado.util.query.QueryParams;

import java.util.List;
import java.util.Optional;

/**
 * @author James Lin
 */
public interface GenericService<T extends BaseModel<ID>, ID> {

    T getById(ID id);

    Optional<T> findById(ID id);

    Optional<T> findUnique(QueryParams params);

    List<T> find(QueryParams params);

    int findSize(QueryParams params);
}
