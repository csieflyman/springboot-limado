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
import party.service.PartyService;

import java.util.List;
import java.util.Set;
import java.util.UUID;
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
        return fetchRelations != null ? partyService.getById(uuid, fetchRelations.split(",")) : partyService.getById(uuid);
    }

    @GetMapping({"/{id}/parents"})
    public Set<Party> getParents(@PathVariable String id) {
        Set<Party> parents = partyService.getParents(UUID.fromString(id));
        parents.forEach(Party::removeRelations); // do not serialize relations
        return parents;
    }

    @GetMapping({"/{id}/children"})
    public Set<Party> getChildren(@PathVariable String id) {
        Set<Party> children = partyService.getChildren(UUID.fromString(id));
        children.forEach(Party::removeRelations); // do not serialize relations
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
            return params.isOnlySize() ? parties.size() : parties;
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
            return params.isOnlySize() ? parties.size() : parties;
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
        partyService.enable(idList.stream().map(UUID::fromString).collect(Collectors.toSet()));
    }

    @PutMapping("/disable")
    public void disable(@RequestBody List<String> idList) {
        log.debug("disable: " + idList);
        partyService.disable(idList.stream().map(UUID::fromString).collect(Collectors.toSet()));
    }

    @DeleteMapping
    public void deleteByIds(@RequestBody List<String> idList) {
        log.debug("delete: " + idList);
        partyService.find(Query.create().where().in("id", idList.stream().map(UUID::fromString).collect(Collectors.toSet())).end())
            .forEach(party -> BeanFactoryAnnotationUtils.qualifiedBeanOfType(beanFactory, PartyService.class, party.getType().getId()).delete(party));
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
        Party parent = partyService.getById(UUID.fromString(parentId), Party.RELATION_PARENT);
        Party child = partyService.getById(UUID.fromString(childId));
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

        Party parent = partyService.getById(UUID.fromString(parentId), Party.RELATION_PARENT);
        List<Party> children = partyService.findParties(Query.create().where().in("id", childrenIds.stream().map(UUID::fromString).collect(Collectors.toSet())).end());
        PartyService partyService = BeanFactoryAnnotationUtils.qualifiedBeanOfType(beanFactory, PartyService.class, parent.getType().getId());
        if(isAdd)
            partyService.addChildren(parent, children);
        else
            partyService.removeChildren(parent, children);
    }

    @PostMapping(value = "/{childId}/parents", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void addParents(@PathVariable String childId, @RequestBody List<String> parentsIds) {
        updateParents(childId, parentsIds, true);
    }

    @DeleteMapping(value = "/{childId}/parents", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void removeParents(@PathVariable String childId, @RequestBody List<String> parentsIds) {
        updateParents(childId, parentsIds, false);
    }

    private void updateParents(String childId, List<String> parentsIds, boolean isAdd) {
        if (parentsIds.isEmpty())
            return;

        Party child = partyService.getById(UUID.fromString(childId));
        List<Party> parents = partyService.findParties(Query.create().where().in("id", parentsIds.stream().map(UUID::fromString).collect(Collectors.toSet())).end());
        PartyService partyService = BeanFactoryAnnotationUtils.qualifiedBeanOfType(beanFactory, PartyService.class, child.getType().getId());
        if(isAdd)
            partyService.addParents(child, parents);
        else
            partyService.removeParents(child, parents);
    }
}
