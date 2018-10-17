package com.csieflyman.limado.service;

import com.csieflyman.limado.model.Party;
import com.csieflyman.limado.util.query.QueryParams;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * author flyman
 */
public interface PartyService<T extends Party> extends GenericService<T, UUID>{

    T create(T party);

    void update(T party);

    T get(T party, String... relations);

    T get(T party, Set<String> relations);

    T getById(UUID id, String... relations);

    T getById(UUID id, Set<String> relations);

    boolean checkExist(String type, String identity);

    void delete(T party);

    List<T> find(QueryParams queryParams);

    int findSize(QueryParams queryParams);

    void enable(Set<UUID> ids);

    void disable(Set<UUID> ids);

    Set<T> getParents(UUID id);

    Set<T> getChildren(UUID id);

    Set<T> getAscendants(UUID id);

    Set<T> getDescendants(UUID id);

    void addChild(T parent, Party child);

    void removeChild(T parent, Party child);

    void addChildren(T parent, Set<Party> children);

    void removeChildren(T parent, Set<Party> children);

    void addParents(T child, Set<Party> parents);

    void removeParents(T child, Set<Party> parents);
}
