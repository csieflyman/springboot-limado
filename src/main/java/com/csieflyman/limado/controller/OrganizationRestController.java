package com.csieflyman.limado.controller;

import com.csieflyman.limado.dto.party.PartyFormValidator;
import com.csieflyman.limado.exception.BadRequestException;
import com.csieflyman.limado.model.Organization;
import com.csieflyman.limado.model.Party;
import com.csieflyman.limado.service.OrganizationService;
import com.csieflyman.limado.util.query.Operator;
import com.csieflyman.limado.util.query.Predicate;
import com.csieflyman.limado.util.query.QueryParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * author flyman
 */
@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationRestController {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationRestController.class);

    @Autowired
    private OrganizationService organizationService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity create(@RequestBody OrganizationForm form, BindingResult result) {
        logger.debug("create orgForm: " + form);
        new PartyFormValidator().validate(form, result);
        if (result.hasErrors()) {
            logger.debug(ValidationUtils.buildErrorMessage(result));
            throw new BadRequestException("invalid org data.", null, ValidationUtils.buildErrorMessage(result));
        }
        Organization org = form.toModel();
        org = organizationService.create(org);
        org.setParents(null);
        org.setChildren(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(org);
    }

    @PutMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void update(@PathVariable String id, @RequestBody OrganizationForm form, BindingResult result) {
        logger.debug("update orgForm: " + form);
        if (!form.getId().toString().equals(id)) {
            throw new BadRequestException("invalid uuid.", null, String.format("path uuid %s isn't the same as uuid %s in request body", id, form.getId()));
        }
        new PartyFormValidator().validate(form, result);
        if (result.hasErrors()) {
            logger.debug(ValidationUtils.buildErrorMessage(result));
            throw new BadRequestException("invalid org data.", null, ValidationUtils.buildErrorMessage(result));
        }
        Organization org = form.toModel();
        organizationService.update(org);
    }

    @PutMapping("{parentId}/child/{childId}")
    public void movePartyToOrganization(@PathVariable String parentId, @PathVariable String childId) {
        UUID parentUUID = UUID.fromString(parentId);
        UUID childUUID = UUID.fromString(childId);
        Party organization = organizationService.getById(parentUUID, Party.RELATION_PARENT);
        if (organization != null && !organization.getType().equals(Organization.TYPE)) {
            throw new BadRequestException(String.format("%s is not a organization", parentUUID));
        }
        Party child = organizationService.getById(childUUID);
        organizationService.movePartyToOrganization(child, (Organization) organization);
    }

    @PostMapping("{parentId}/child/{childId}")
    public void addChild(@PathVariable String parentId, @PathVariable String childId) {
        UUID parentUUID = UUID.fromString(parentId);
        UUID childUUID = UUID.fromString(childId);
        Party organization = organizationService.getById(parentUUID, Party.RELATION_PARENT);
        if (organization != null && !organization.getType().equals(Organization.TYPE)) {
            throw new BadRequestException(String.format("%s is not a organization", parentUUID));
        }
        Party child = organizationService.getById(childUUID);
        organizationService.addChild((Organization) organization, child);
    }

    @DeleteMapping("{parentId}/child/{childId}")
    public void removeChild(@PathVariable String parentId, @PathVariable String childId) {
        UUID parentUUID = UUID.fromString(parentId);
        UUID childUUID = UUID.fromString(childId);
        Party organization = organizationService.getById(parentUUID, Party.RELATION_PARENT);
        if (organization != null && !organization.getType().equals(Organization.TYPE)) {
            throw new BadRequestException(String.format("%s is not a organization", parentUUID));
        }
        Party child = organizationService.getById(childUUID);
        organizationService.removeChild((Organization) organization, child);
    }

    @PostMapping(value = "{parentId}/children", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void addChildren(@PathVariable String parentId, @RequestBody List<String> childrenIds) {
        if (childrenIds.isEmpty())
            return;

        UUID parentUUID = UUID.fromString(parentId);
        Set<UUID> childrenUUIDs = childrenIds.stream().map(UUID::fromString).collect(Collectors.toSet());
        Party organization = organizationService.getById(parentUUID, Party.RELATION_PARENT);
        if (organization != null && !organization.getType().equals(Organization.TYPE)) {
            throw new BadRequestException(String.format("%s is not a organization", organization));
        }
        QueryParams params = new QueryParams();
        params.addPredicate(new Predicate("id", Operator.IN, childrenUUIDs));
        List<Party> children = organizationService.find(params);
        organizationService.addChildren((Organization) organization, new HashSet<>(children));
    }

    @DeleteMapping(value = "{parentId}/children", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void removeChildren(@PathVariable String parentId, @RequestBody List<String> childrenIds) {
        if (childrenIds.isEmpty())
            return;

        UUID parentUUID = UUID.fromString(parentId);
        Set<UUID> childrenUUIDs = childrenIds.stream().map(UUID::fromString).collect(Collectors.toSet());
        Party organization = organizationService.getById(parentUUID, Party.RELATION_PARENT);
        if (organization != null && !organization.getType().equals(Organization.TYPE)) {
            throw new BadRequestException(String.format("%s is not a organization", organization));
        }
        QueryParams params = new QueryParams();
        params.addPredicate(new Predicate("id", Operator.IN, childrenUUIDs));
        List<Party> children = organizationService.find(params);
        organizationService.removeChildren((Organization) organization, new HashSet<>(children));
    }

    @PostMapping(value = "{childId}/parents", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void addParents(@PathVariable String childId, @RequestBody List<String> parentsIds) {
        if (parentsIds.isEmpty())
            return;

        UUID childUUID = UUID.fromString(childId);
        Set<UUID> parentsUUIDs = parentsIds.stream().map(UUID::fromString).collect(Collectors.toSet());
        Party organization = organizationService.getById(childUUID);
        if (organization != null && !organization.getType().equals(Organization.TYPE)) {
            throw new BadRequestException(String.format("%s is not a organization", organization));
        }
        QueryParams params = new QueryParams();
        params.addPredicate(new Predicate("id", Operator.IN, parentsUUIDs));
        List<Party> parents = organizationService.find(params);
        organizationService.addParents((Organization) organization, new HashSet<>(parents));
    }

    @DeleteMapping(value = "{childId}/parents", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void removeParents(@PathVariable String childId, @RequestBody List<String> parentsIds) {
        if (parentsIds.isEmpty())
            return;

        UUID childUUID = UUID.fromString(childId);
        Set<UUID> parentsUUIDs = parentsIds.stream().map(UUID::fromString).collect(Collectors.toSet());
        Party organization = organizationService.getById(childUUID);
        if (organization != null && !organization.getType().equals(Organization.TYPE)) {
            throw new BadRequestException(String.format("%s is not a organization", organization));
        }
        QueryParams params = new QueryParams();
        params.addPredicate(new Predicate("id", Operator.IN, parentsUUIDs));
        List<Party> parents = organizationService.find(params);
        organizationService.removeParents((Organization) organization, new HashSet<>(parents));
    }

    @GetMapping(value = "{id}/descendants", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object getDescendants(@PathVariable String id, @RequestParam() Map<String, String> requestParam) {
        UUID uuid = UUID.fromString(id);
        Set<Party> descendants = organizationService.getDescendants(uuid);
        if (requestParam != null && !requestParam.isEmpty()) {
            QueryParams params = new QueryParams();
            params.putAll(requestParam);
            List<Party> parties = organizationService.find(params);
            parties.retainAll(descendants);
            if (params.isOnlySize()) {
                return parties.size();
            } else {
                return parties;
            }
        }
        return descendants;
    }
}
