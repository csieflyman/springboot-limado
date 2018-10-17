package com.csieflyman.limado.controller;

import com.csieflyman.limado.dto.party.PartyForm;
import com.csieflyman.limado.exception.BadRequestException;
import com.csieflyman.limado.model.Party;
import com.csieflyman.limado.model.User;
import com.csieflyman.limado.service.UserService;
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
@RequestMapping(value = "/api/v1/users", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@ResponseBody
public class UserRestController extends AbstractController{

    private static final Logger logger = LoggerFactory.getLogger(UserRestController.class);

    @Autowired
    private UserService userService;



    @PostMapping("{childId}/parents")
    public void addParents(@PathVariable String childId, @RequestBody List<String> parentsIds) {
        if (parentsIds.isEmpty())
            return;

        UUID childUUID = UUID.fromString(childId);
        Set<UUID> parentsUUIDs = parentsIds.stream().map(UUID::fromString).collect(Collectors.toSet());
        Party user = userService.getById(childUUID);
        if (user != null && !user.getType().equals(User.TYPE)) {
            throw new BadRequestException(String.format("%s is not a user", user));
        }
        List<Party> parents = userService.find(QueryParams.create().in("id", parentsUUIDs));
        userService.addParents((User) user, new HashSet<>(parents));
    }

    @DeleteMapping("{childId}/parents")
    public void removeParents(@PathVariable String childId, @RequestBody List<String> parentsIds) {
        if (parentsIds.isEmpty())
            return;

        UUID childUUID = UUID.fromString(childId);
        Set<UUID> parentsUUIDs = parentsIds.stream().map(UUID::fromString).collect(Collectors.toSet());
        Party user = userService.getById(childUUID);
        if (user != null && !user.getType().equals(User.TYPE)) {
            throw new BadRequestException(String.format("%s is not a user", user));
        }
        List<Party> parents = userService.find(QueryParams.create().in("id", parentsUUIDs));
        userService.removeParents((User) user, new HashSet<>(parents));
    }
}
