package party.service;

import party.model.Organization;
import party.model.Party;

/**
 * @author csieflyman
 */
public interface OrganizationService extends PartyService<Organization> {

    void movePartyToOrganization(Party child, Organization organization);
}
