package party.service;

import base.service.GenericService;
import base.util.query.Query;
import party.model.Party;
import party.model.PartyType;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author csieflyman
 */
public interface PartyService<T extends Party> extends GenericService<T, UUID> {

    T get(T party, String... relations);

    T get(T party, Set<String> relations);

    T getById(UUID id, String... relations);

    T getById(UUID id, Set<String> relations);

    List<Party> findParties(Query query);

    boolean checkExist(PartyType type, String identity);

    void enable(Set<UUID> ids);

    void disable(Set<UUID> ids);

    Set<Party> getParents(UUID id);

    Set<Party> getChildren(UUID id);

    Set<Party> getAscendants(UUID id);

    Set<Party> getDescendants(UUID id);

    void addChild(T parent, Party child);

    void removeChild(T parent, Party child);

    void addChildren(T parent, Set<Party> children);

    void removeChildren(T parent, Set<Party> children);

    void addParents(T child, Set<Party> parents);

    void removeParents(T child, Set<Party> parents);
}
