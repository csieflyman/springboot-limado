package party.controller;

import base.controller.AbstractController;
import base.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import party.model.Organization;
import party.model.Party;
import party.model.PartyType;
import party.service.OrganizationService;

import java.util.UUID;

/**
 * @author csieflyman
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/organizations", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@ResponseBody
public class OrganizationRestController extends AbstractController {

    @Autowired
    private OrganizationService organizationService;

    @PutMapping("{parentId}/children/{childId}")
    public void movePartyToOrganization(@PathVariable String parentId, @PathVariable String childId) {
        UUID parentUUID = UUID.fromString(parentId);
        UUID childUUID = UUID.fromString(childId);
        Organization organization = organizationService.getById(parentUUID, Party.RELATION_PARENT);
        if (organization != null && organization.getType() != PartyType.OU) {
            throw new BadRequestException(String.format("%s is not a organization", parentUUID));
        }
        Party child = organizationService.getById(childUUID);
        organizationService.movePartyToOrganization(child, organization);
    }
}
