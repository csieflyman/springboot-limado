package base.service;

import base.dao.GenericDao;
import base.exception.ObjectNotFoundException;
import base.model.BaseModel;
import base.util.query.Query;
import com.google.common.base.Preconditions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author csieflyman
 */
public abstract class GenericServiceImpl<T extends BaseModel<ID>, ID> implements GenericService<T, ID> {

    protected GenericDao<T, ID> dao;

    protected GenericServiceImpl(GenericDao<T, ID> dao) {
        this.dao = dao;
    }

    @Transactional(readOnly = true)
    @Override
    public T getById(ID id) {
        Preconditions.checkNotNull(id, "id can't be null");

        return dao.getById(id).orElseThrow(() -> new ObjectNotFoundException(String.format("entity %s doesn't exist.", id)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<T> findById(ID id) {
        Preconditions.checkNotNull(id, "id can't be null");

        return dao.getById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<T> findOne(Query query) {
        Preconditions.checkNotNull(query, "query can't be null");

        return dao.findOne(query);
    }

    @Transactional(readOnly = true)
    @Override
    public List<T> find(Query query) {
        Preconditions.checkNotNull(query, "query can't be null");

        return dao.find(query);
    }

    @Transactional(readOnly = true)
    @Override
    public long findSize(Query query) {
        Preconditions.checkNotNull(query, "query can't be null");

        return dao.findSize(query);
    }
}
