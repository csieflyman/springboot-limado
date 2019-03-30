package party.controller;

import base.controller.AbstractController;
import base.model.Identifiable;
import base.util.query.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import party.dto.PartyCreateForm;
import party.dto.PartyForm;
import party.model.Party;
import party.model.PartyType;
import party.service.PartyService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author csieflyman
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/parties", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@ResponseBody
public class PartyRestController extends AbstractController {
    
    @Autowired
    private PartyService<Party> partyService;
    @Autowired
    private BeanFactory beanFactory;

    @PostMapping
    public ResponseEntity create(@RequestBody PartyCreateForm form, BindingResult result) {
        log.debug("create partyForm: " + form);
        processBindingResult(result);
        Party party = form.toModel();
        PartyService partyService = BeanFactoryAnnotationUtils.qualifiedBeanOfType(beanFactory, PartyService.class, party.getType().getId());
        Identifiable entity = partyService.create(party);
        return ResponseEntity.status(HttpStatus.CREATED).body(entity.getId());
    }

    @PutMapping({"/{id}"})
    public void update(@PathVariable String id, @RequestBody PartyForm form, BindingResult result) {
        log.debug("update partyForm: " + form);
        processBindingResult(result);
        form.setId(UUID.fromString(id));
        Party party = form.toModel();
        PartyService partyService = BeanFactoryAnnotationUtils.qualifiedBeanOfType(beanFactory, PartyService.class, party.getType().getId());
        partyService.update(party);
    }

    @GetMapping({"/{id}"})
    public Party getById(@PathVariable String id, @RequestParam(name = Query.Q_FETCH_RELATIONS, required = false) String fetchRelations) {
        log.debug("getById: " + id + " , fetchRelations = " + fetchRelations);
        UUID uuid = UUID.fromString(id);
        Party party;
        if (fetchRelations != null) {
            party = partyService.getById(uuid, fetchRelations.split(","));
        } else {
            party = partyService.getById(uuid);
        }
        return party;
    }

    @GetMapping({"/{id}/parents"})
    public Set<Party> getParents(@PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        Set<Party> parents = partyService.getParents(uuid);
        removePartyRelations(parents);
        return parents;
    }

    @GetMapping({"/{id}/children"})
    public Set<Party> getChildren(@PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        Set<Party> children = partyService.getChildren(uuid);
        removePartyRelations(children);
        return children;
    }

    @GetMapping({"/{id}/ascendants"})
    public Object getAscendants(@PathVariable String id, @RequestParam() MultiValueMap<String, String> requestParam) {
        UUID uuid = UUID.fromString(id);
        Set<Party> ascendants = partyService.getAscendants(uuid);
        if (requestParam != null && !requestParam.isEmpty()) {
            Query params = Query.create(requestParam);
            List<Party> parties = partyService.find(params);
            parties.retainAll(ascendants);
            if (params.isOnlySize()) {
                return parties.size();
            } else {
                return parties;
            }
        }
        return ascendants;
    }

    @GetMapping({"/{id}/descendants"})
    public Object getDescendants(@PathVariable String id, @RequestParam() MultiValueMap<String, String> requestParam) {
        UUID uuid = UUID.fromString(id);
        Set<Party> descendants = partyService.getDescendants(uuid);
        if (requestParam != null && !requestParam.isEmpty()) {
            Query params = Query.create(requestParam);
            List<Party> parties = partyService.find(params);
            parties.retainAll(descendants);
            if (params.isOnlySize()) {
                return parties.size();
            } else {
                return parties;
            }
        }
        return descendants;
    }

    @GetMapping
    public Object find(@RequestParam() MultiValueMap<String, String> requestParam) {
        log.debug("find requestParam: " + requestParam);
        return findEntities(partyService, Query.create(requestParam));
    }

    @PutMapping("/enable")
    public void enable(@RequestBody List<String> idList) {
        log.debug("enable: " + idList);
        Set<UUID> uuids = idList.stream().map(UUID::fromString).collect(Collectors.toSet());
        partyService.enable(uuids);
    }

    @PutMapping("/disable")
    public void disable(@RequestBody List<String> idList) {
        log.debug("disable: " + idList);
        Set<UUID> uuids = idList.stream().map(UUID::fromString).collect(Collectors.toSet());
        partyService.disable(uuids);
    }

    @DeleteMapping
    public void deleteByIds(@RequestBody List<String> idList) {
        log.debug("delete: " + idList);
        Set<UUID> uuids = idList.stream().map(UUID::fromString).collect(Collectors.toSet());

        Query params = Query.create().in("id", uuids);
        List<Party> parties = partyService.find(params);

        Map<PartyType, List<Party>> partyTypeMap = parties.stream().collect(Collectors.groupingBy(Party::getType));
        for(PartyType type: partyTypeMap.keySet()) {
            PartyService<Party> partyService = BeanFactoryAnnotationUtils.qualifiedBeanOfType(beanFactory, PartyService.class, type.getId());
            partyTypeMap.get(type).forEach(partyService::delete);
        }
    }

    @PostMapping("/{parentId}/children/{childId}")
    public void addChild(@PathVariable String parentId, @PathVariable String childId) {
        updateChild(parentId, childId, true);
    }

    @DeleteMapping("/{parentId}/children/{childId}")
    public void removeChild(@PathVariable String parentId, @PathVariable String childId) {
        updateChild(parentId, childId, false);
    }

    private void updateChild(String parentId, String childId, boolean isAdd) {
        UUID parentUUID = UUID.fromString(parentId);
        UUID childUUID = UUID.fromString(childId);
        Party parent = partyService.getById(parentUUID, Party.RELATION_PARENT);
        Party child = partyService.getById(childUUID);
        PartyService partyService = BeanFactoryAnnotationUtils.qualifiedBeanOfType(beanFactory, PartyService.class, parent.getType().getId());
        if(isAdd)
            partyService.addChild(parent, child);
        else
            partyService.removeChild(parent, child);
    }

    @PostMapping(value = "/{parentId}/children", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void addChildren(@PathVariable String parentId, @RequestBody List<String> childrenIds) {
        updateChildren(parentId, childrenIds, true);
    }

    @DeleteMapping(value = "/{parentId}/children", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void removeChildren(@PathVariable String parentId, @RequestBody List<String> childrenIds) {
        updateChildren(parentId, childrenIds, false);
    }

    private void updateChildren(String parentId, List<String> childrenIds, boolean isAdd) {
        if (childrenIds.isEmpty())
            return;

        UUID parentUUID = UUID.fromString(parentId);
        Set<UUID> childrenUUIDs = childrenIds.stream().map(UUID::fromString).collect(Collectors.toSet());
        Party parent = partyService.getById(parentUUID, Party.RELATION_PARENT);
        List<Party> children = partyService.findParties(Query.create().in("id", childrenUUIDs));
        PartyService partyService = BeanFactoryAnnotationUtils.qualifiedBeanOfType(beanFactory, PartyService.class, parent.getType().getId());
        if(isAdd)
            partyService.addChildren(parent, new HashSet<>(children));
        else
            partyService.removeChildren(parent, new HashSet<>(children));
    }

    @PostMapping(value = "/{childId}/parents", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void addParents(@PathVariable String childId, @RequestBody List<String> parentsIds) {
        updateChildren(childId, parentsIds, true);
    }

    @DeleteMapping(value = "/{childId}/parents", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void removeParents(@PathVariable String childId, @RequestBody List<String> parentsIds) {
        updateChildren(childId, parentsIds, false);
    }

    private void updateParents(String childId, List<String> parentsIds, boolean isAdd) {
        if (parentsIds.isEmpty())
            return;

        UUID childUUID = UUID.fromString(childId);
        Set<UUID> parentsUUIDs = parentsIds.stream().map(UUID::fromString).collect(Collectors.toSet());
        Party child = partyService.getById(childUUID);
        List<Party> parents = partyService.findParties(Query.create().in("id", parentsUUIDs));
        PartyService partyService = BeanFactoryAnnotationUtils.qualifiedBeanOfType(beanFactory, PartyService.class, child.getType().getId());
        if(isAdd)
            partyService.addParents(child, new HashSet<>(parents));
        else
            partyService.removeParents(child, new HashSet<>(parents));
    }

    // do not serialize relations
    private void removePartyRelations(Collection<Party> parties) {
        if (parties != null) {
            parties.forEach(party -> party.setParents(null));
            parties.forEach(party -> party.setChildren(null));
        }
    }
}
