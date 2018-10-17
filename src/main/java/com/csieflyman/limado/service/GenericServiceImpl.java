package com.csieflyman.limado.service;

import com.csieflyman.limado.dao.GenericDao;
import com.csieflyman.limado.exception.ObjectNotFoundException;
import com.csieflyman.limado.model.BaseModel;
import com.csieflyman.limado.util.query.QueryParams;
import com.google.common.base.Preconditions;

import java.util.List;
import java.util.Optional;

/**
 * @author James Lin
 */
public abstract class GenericServiceImpl<T extends BaseModel<ID>, ID> implements GenericService<T, ID> {

    protected GenericDao<T, ID> dao;

    protected GenericServiceImpl(GenericDao<T, ID> dao) {
        this.dao = dao;
    }

    @Override
    public T getById(ID id) {
        Preconditions.checkNotNull(id, "id can't be null");

        return dao.getById(id).orElseThrow(() -> new ObjectNotFoundException(String.format("entity %s doesn't exist.", id)));
    }

    @Override
    public Optional<T> findById(ID id) {
        Preconditions.checkNotNull(id, "id can't be null");

        return dao.getById(id);
    }

    @Override
    public Optional<T> findUnique(QueryParams params) {
        Preconditions.checkNotNull(params, "params can't be null");

        return dao.findOne(params);
    }

    @Override
    public List<T> find(QueryParams params) {
        Preconditions.checkNotNull(params, "params can't be null");

        return dao.find(params);
    }

    @Override
    public int findSize(QueryParams params) {
        Preconditions.checkNotNull(params, "params can't be null");

        return dao.findSize(params);
    }
}
