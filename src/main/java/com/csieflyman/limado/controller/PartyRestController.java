package com.csieflyman.limado.controller;

import com.csieflyman.limado.dto.party.PartyForm;
import com.csieflyman.limado.model.Group;
import com.csieflyman.limado.model.Party;
import com.csieflyman.limado.service.PartyService;
import com.csieflyman.limado.util.query.QueryParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * author flyman
 */
@RestController
@RequestMapping(value = "/api/v1", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class PartyRestController extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(PartyRestController.class);

    @Autowired
    private PartyService<Party> partyService;
    @Autowired
    private BeanFactory beanFactory;

    @PostMapping({"/parties", "/users", "/groups", "/organizations"})
    public ResponseEntity create(@RequestBody PartyForm<Party> form, BindingResult result) {
        logger.debug("create partyForm: " + form);
        processBindingResult(result);
        Party party = form.toModel();
        PartyService<Party> partyService = BeanFactoryAnnotationUtils.qualifiedBeanOfType(beanFactory, PartyService.class, party.getType());
        party = partyService.create(party);
        return ResponseEntity.status(HttpStatus.CREATED).body(party);
    }

    @PutMapping({"/parties/{id}", "/users/{id}", "/groups/{id}", "organizations/{id}"})
    public void update(@PathVariable String id, @RequestBody PartyForm<Party> form, BindingResult result) {
        logger.debug("update partyForm: " + form);
        form.id = UUID.fromString(id);
        processBindingResult(result);
        Party party = form.toModel();
        PartyService<Party> partyService = BeanFactoryAnnotationUtils.qualifiedBeanOfType(beanFactory, PartyService.class, party.getType());
        partyService.update(party);
    }

    @GetMapping({"/parties/{id}", "/users/{id}", "/groups/{id}", "organizations/{id}"})
    public Party getById(@PathVariable String id, @RequestParam(name = QueryParams.Q_FETCH_RELATIONS, required = false) String fetchRelations) {
        logger.debug("getById: " + id + " , fetchRelations = " + fetchRelations);
        UUID uuid = UUID.fromString(id);
        Party party;
        if (fetchRelations != null) {
            party = partyService.getById(uuid, fetchRelations.split(","));
        } else {
            party = partyService.getById(uuid);
        }
        return party;
    }

    @GetMapping({"/parties/{id}/parents", "/users/{id}/parents", "/groups/{id}/parents", "organizations/{id}/parents"})
    public Set<Party> getParents(@PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        Set<Party> parents = partyService.getParents(uuid);
        removePartyRelations(parents);
        return parents;
    }

    @GetMapping({"/parties/{id}/children", "/groups/{id}/children", "organizations/{id}/children"})
    public Set<Party> getChildren(@PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        Set<Party> children = partyService.getChildren(uuid);
        removePartyRelations(children);
        return children;
    }

    @GetMapping({"/parties/{id}/ascendants", "/groups/{id}/ascendants", "organizations/{id}/ascendants"})
    public Object getAscendants(@PathVariable String id, @RequestParam() MultiValueMap<String, String> requestParam) {
        UUID uuid = UUID.fromString(id);
        Set<Party> ascendants = partyService.getAscendants(uuid);
        if (requestParam != null && !requestParam.isEmpty()) {
            QueryParams params = QueryParams.create(requestParam);
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

    @GetMapping({"/parties/{id}/descendants", "/groups/{id}/descendants", "organizations/{id}/descendants"})
    public Object getDescendants(@PathVariable String id, @RequestParam() MultiValueMap<String, String> requestParam) {
        UUID uuid = UUID.fromString(id);
        Set<Party> descendants = partyService.getDescendants(uuid);
        if (requestParam != null && !requestParam.isEmpty()) {
            QueryParams params = QueryParams.create(requestParam);
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

    @GetMapping("/parties")
    @ResponseBody
    public Object find(@RequestParam() MultiValueMap<String, String> requestParam) {
        logger.debug("find requestParam: " + requestParam);
        QueryParams params = QueryParams.create(requestParam);
        findEntities(partyService, params);

        if (params.isOnlySize()) {
            return partyService.findSize(params);
        } else {
            List<Party> parties = partyService.find(params);
            return parties;
        }
    }

    @PutMapping("enable")
    public void enable(@RequestBody List<String> idList) {
        logger.debug("enable: " + idList);
        Set<UUID> uuids = idList.stream().map(UUID::fromString).collect(Collectors.toSet());
        partyService.enable(uuids);
    }

    @PutMapping("disable")
    public void disable(@RequestBody List<String> idList) {
        logger.debug("disable: " + idList);
        Set<UUID> uuids = idList.stream().map(UUID::fromString).collect(Collectors.toSet());
        partyService.disable(uuids);
    }

    @DeleteMapping
    public void deleteByIds(@RequestBody List<String> idList) {
        logger.debug("delete: " + idList);
        Set<UUID> uuids = idList.stream().map(UUID::fromString).collect(Collectors.toSet());

        QueryParams params = QueryParams.create().in("id", uuids);
        List<Party> parties = partyService.find(params);

        Map<String, List<Party>> partyTypeMap = parties.stream().collect(Collectors.groupingBy(Party::getType));
        for(String type: partyTypeMap.keySet()) {
            PartyService<Party> partyService = BeanFactoryAnnotationUtils.qualifiedBeanOfType(beanFactory, PartyService.class, type);
            partyTypeMap.get(Group.TYPE).forEach(partyService::delete);
        }
    }

    // do not serialize relations
    private void removePartyRelations(Collection<Party> parties) {
        if (parties != null) {
            parties.forEach(party -> party.setParents(null));
            parties.forEach(party -> party.setChildren(null));
        }
    }
}
