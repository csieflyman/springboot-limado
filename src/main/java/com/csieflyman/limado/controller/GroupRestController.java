package com.csieflyman.limado.controller;

import com.csieflyman.limado.dto.party.PartyFormValidator;
import com.csieflyman.limado.exception.BadRequestException;
import com.csieflyman.limado.model.Group;
import com.csieflyman.limado.model.Party;
import com.csieflyman.limado.service.GroupService;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * author flyman
 */
@RestController
@RequestMapping("/api/v1/groups")
public class GroupRestController {

    private static final Logger logger = LoggerFactory.getLogger(GroupRestController.class);

    @Autowired
    private GroupService groupService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity create(@RequestBody GroupForm form, BindingResult result) {
        logger.debug("create groupForm: " + form);
        new PartyFormValidator().validate(form, result);
        if (result.hasErrors()) {
            logger.debug(ValidationUtils.buildErrorMessage(result));
            throw new BadRequestException("invalid group data.", null, ValidationUtils.buildErrorMessage(result));
        }
        Group group = form.toModel();
        group = groupService.create(group);
        group.setParents(null);
        group.setChildren(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }

    @PutMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void update(@PathVariable String id, @RequestBody GroupForm form, BindingResult result) {
        logger.debug("update groupForm: " + form);
        if (!form.getId().toString().equals(id)) {
            throw new BadRequestException("invalid uuid.", null, String.format("path uuid %s isn't the same as uuid %s in request body", id, form.getId()));
        }
        new PartyFormValidator().validate(form, result);
        if (result.hasErrors()) {
            logger.debug(ValidationUtils.buildErrorMessage(result));
            throw new BadRequestException("invalid group data.", null, ValidationUtils.buildErrorMessage(result));
        }
        Group group = form.toModel();
        groupService.update(group);
    }

    @PostMapping("{parentId}/child/{childId}")
    public void addChild(@PathVariable String parentId, @PathVariable String childId) {
        UUID parentUUID = UUID.fromString(parentId);
        UUID childUUID = UUID.fromString(childId);
        Party group = groupService.getById(parentUUID, Party.RELATION_PARENT);
        if (group != null && !group.getType().equals(Group.TYPE)) {
            throw new BadRequestException(String.format("%s is not a group", parentUUID));
        }
        Party child = groupService.getById(childUUID);
        groupService.addChild((Group) group, child);
    }

    @DeleteMapping("{parentId}/child/{childId}")
    public void removeChild(@PathVariable String parentId, @PathVariable String childId) {
        UUID parentUUID = UUID.fromString(parentId);
        UUID childUUID = UUID.fromString(childId);
        Party group = groupService.getById(parentUUID, Party.RELATION_PARENT);
        if (group != null && !group.getType().equals(Group.TYPE)) {
            throw new BadRequestException(String.format("%s is not a group", parentUUID));
        }
        Party child = groupService.getById(childUUID);
        groupService.removeChild((Group) group, child);
    }

    @PostMapping(value = "{parentId}/children", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void addChildren(@PathVariable String parentId, @RequestBody List<String> childrenIds) {
        if (childrenIds.isEmpty())
            return;

        UUID parentUUID = UUID.fromString(parentId);
        Set<UUID> childrenUUIDs = childrenIds.stream().map(UUID::fromString).collect(Collectors.toSet());
        Party group = groupService.getById(parentUUID, Party.RELATION_PARENT);
        if (group != null && !group.getType().equals(Group.TYPE)) {
            throw new BadRequestException(String.format("%s is not a group", group));
        }
        QueryParams params = new QueryParams();
        params.addPredicate(new Predicate("id", Operator.IN, childrenUUIDs));
        List<Party> children = groupService.find(params);
        groupService.addChildren((Group) group, new HashSet<>(children));
    }

    @DeleteMapping(value = "{parentId}/children", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void removeChildren(@PathVariable String parentId, @RequestBody List<String> childrenIds) {
        if (childrenIds.isEmpty())
            return;

        UUID parentUUID = UUID.fromString(parentId);
        Set<UUID> childrenUUIDs = childrenIds.stream().map(UUID::fromString).collect(Collectors.toSet());
        Party group = groupService.getById(parentUUID, Party.RELATION_PARENT);
        if (group != null && !group.getType().equals(Group.TYPE)) {
            throw new BadRequestException(String.format("%s is not a group", group));
        }
        QueryParams params = new QueryParams();
        params.addPredicate(new Predicate("id", Operator.IN, childrenUUIDs));
        List<Party> children = groupService.find(params);
        groupService.removeChildren((Group) group, new HashSet<>(children));
    }

    @PostMapping(value = "{childId}/parents", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void addParents(@PathVariable String childId, @RequestBody List<String> parentsIds) {
        if (parentsIds.isEmpty())
            return;

        UUID childUUID = UUID.fromString(childId);
        Set<UUID> parentsUUIDs = parentsIds.stream().map(UUID::fromString).collect(Collectors.toSet());
        Party group = groupService.getById(childUUID);
        if (group != null && !group.getType().equals(Group.TYPE)) {
            throw new BadRequestException(String.format("%s is not a group", group));
        }
        QueryParams params = new QueryParams();
        params.addPredicate(new Predicate("id", Operator.IN, parentsUUIDs));
        List<Party> parents = groupService.find(params);
        groupService.addParents((Group) group, new HashSet<>(parents));
    }

    @DeleteMapping(value = "{childId}/parents", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void removeParents(@PathVariable String childId, @RequestBody List<String> parentsIds) {
        if (parentsIds.isEmpty())
            return;

        UUID childUUID = UUID.fromString(childId);
        Set<UUID> parentsUUIDs = parentsIds.stream().map(UUID::fromString).collect(Collectors.toSet());
        Party group = groupService.getById(childUUID);
        if (group != null && !group.getType().equals(Group.TYPE)) {
            throw new BadRequestException(String.format("%s is not a group", group));
        }
        QueryParams params = new QueryParams();
        params.addPredicate(new Predicate("id", Operator.IN, parentsUUIDs));
        List<Party> parents = groupService.find(params);
        groupService.removeParents((Group) group, new HashSet<>(parents));
    }
}
