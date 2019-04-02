package party.dao;

import base.dao.GenericDao;
import party.model.Party;

import java.util.Collection;
import java.util.UUID;

/**
 * @author csieflyman
 */
public interface PartyDao<T extends Party> extends GenericDao<T, UUID> {

    void addChild(Party parent, Party child);

    void removeChild(Party parent, Party child);

    void addChildren(Party parent, Collection<Party> children);

    void removeChildren(Party parent, Collection<Party> children);

    void addParents(Party child, Collection<Party> parents);

    void removeParents(Party child, Collection<Party> parents);
}
