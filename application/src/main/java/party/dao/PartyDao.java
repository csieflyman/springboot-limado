package party.dao;

import base.dao.GenericDao;
import party.model.Party;

import java.util.Set;
import java.util.UUID;

/**
 * @author csieflyman
 */
public interface PartyDao<T extends Party> extends GenericDao<T, UUID> {

    void addChild(Party parent, Party child);

    void removeChild(Party parent, Party child);

    void addChildren(Party parent, Set<Party> children);

    void removeChildren(Party parent, Set<Party> children);

    void addParents(Party child, Set<Party> parents);

    void removeParents(Party child, Set<Party> parents);
}
